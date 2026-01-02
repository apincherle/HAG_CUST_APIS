# QR-Based Card Certification System (End-to-End)
## Database Schema • FastAPI/Flask Backend • QR + Label Layouts • Anti-Tamper

This document gives you a complete blueprint to:
- create a **unique, scannable QR** per graded card
- link scans to a **public certificate page**
- store grading/authenticity data in a **database**
- generate **print-ready labels** (QR + serial + branding)
- add **anti-tamper** protections (from simple to strong)

---

## 0) Recommended approach (high level)

### QR content
Put a **URL** in the QR code (not the card data). Example:

```
https://yourdomain.com/c/8F3A9C72
```

Where `8F3A9C72` is a **non-guessable public ID** that maps to your database.

### When you grade a card
1. Create DB record (grade, images, etc.)
2. Generate `public_id` + `serial_number`
3. Create public URL
4. Generate QR image
5. Print a label (QR + serial + grade)
6. Attach label to slab/cert/sleeve

---

## 1) Data model & database schema

You can start with **SQLite** (simple) and migrate to Postgres later with minimal changes.

### 1.1 Entities
- **Company** (branding/about/legal links)
- **CardCertificate** (the thing your QR resolves to)
- **CardImage** (front/back/surface maps)
- **ScanEvent** (optional analytics, fraud detection)
- **AuditLog** (optional, internal actions)

### 1.2 Minimal schema (SQL)

> Works for SQLite and Postgres.

```sql
-- COMPANY (single row is fine at first)
CREATE TABLE company (
  id              INTEGER PRIMARY KEY,
  name            TEXT NOT NULL,
  website         TEXT,
  support_email   TEXT,
  about_url       TEXT,
  logo_url        TEXT,
  created_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

-- CARD CERTIFICATE (public-facing record)
CREATE TABLE card_certificate (
  id                 INTEGER PRIMARY KEY,
  public_id          TEXT NOT NULL UNIQUE,      -- non-guessable ID used in URL
  serial_number      TEXT NOT NULL UNIQUE,      -- human-readable (e.g., YCG-2026-000812)
  status             TEXT NOT NULL DEFAULT 'VERIFIED', -- VERIFIED | FLAGGED | REVOKED
  card_name          TEXT NOT NULL,
  set_name           TEXT,
  year               INTEGER,
  card_number        TEXT,
  variant            TEXT,                      -- holo / reverse / etc.
  grade              REAL NOT NULL,             -- numeric grade or adapt to your scale
  grader_version     TEXT,                      -- grading algorithm version
  graded_at          TEXT NOT NULL,             -- ISO string
  notes_public       TEXT,                      -- what customers can see
  notes_internal     TEXT,                      -- internal only
  checksum_sha256    TEXT,                      -- optional: hash of key artifacts
  created_at         TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at         TEXT NOT NULL DEFAULT (datetime('now'))
);

-- IMAGES (front/back/surface)
CREATE TABLE card_image (
  id              INTEGER PRIMARY KEY,
  certificate_id  INTEGER NOT NULL,
  kind            TEXT NOT NULL,                -- front | back | surface_L | surface_R | etc.
  url             TEXT NOT NULL,                -- CDN or local/static URL
  width           INTEGER,
  height          INTEGER,
  created_at      TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (certificate_id) REFERENCES card_certificate(id) ON DELETE CASCADE
);

-- OPTIONAL: SCAN EVENTS (analytics + anti-fraud)
CREATE TABLE scan_event (
  id              INTEGER PRIMARY KEY,
  certificate_id  INTEGER NOT NULL,
  scanned_at      TEXT NOT NULL DEFAULT (datetime('now')),
  user_agent      TEXT,
  ip_hash         TEXT,                         -- hash IP for privacy, not raw IP
  country         TEXT,
  referrer        TEXT,
  FOREIGN KEY (certificate_id) REFERENCES card_certificate(id) ON DELETE CASCADE
);

-- OPTIONAL: index for fast lookup
CREATE INDEX idx_card_certificate_public_id ON card_certificate(public_id);
CREATE INDEX idx_card_image_certificate_id ON card_image(certificate_id);
CREATE INDEX idx_scan_event_certificate_id ON scan_event(certificate_id);
```

### 1.3 ID strategy (important)
Use **two IDs**:
- `public_id`: random, non-guessable (used in URL)
- `serial_number`: human-readable (printed on label)

Recommended:
- `public_id`: 10–16 chars Base32/hex, randomly generated
- `serial_number`: `YOURCO-YYYY-######`

---

## 2) Backend: FastAPI (recommended) + SQLite/Postgres

FastAPI is great for:
- clean API design
- auto docs
- quick UI templating
- easy scaling

### 2.1 Project structure

```
cert-server/
  app.py
  db.py
  models.py
  templates/
    certificate.html
  static/
    labels/
    qrs/
```

### 2.2 Install dependencies

```bash
pip install fastapi uvicorn sqlalchemy jinja2 python-multipart qrcode pillow reportlab
```

- `sqlalchemy`: database ORM
- `jinja2`: HTML templates
- `qrcode`, `pillow`: QR generation
- `reportlab`: PDF label output

### 2.3 FastAPI backend code (single-file starter)

Create `app.py`:

```python
import os
import hmac
import json
import base64
import hashlib
from datetime import datetime
from pathlib import Path

from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import HTMLResponse, FileResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

from sqlalchemy import create_engine, Column, Integer, String, Text, Float, ForeignKey
from sqlalchemy.orm import declarative_base, sessionmaker, relationship

import qrcode
from PIL import Image

# -----------------------------
# Config
# -----------------------------
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./certs.sqlite3")
BASE_URL = os.getenv("BASE_URL", "http://localhost:8000")
SECRET = os.getenv("CERT_SECRET", "change-me-please")  # used for signed URLs (anti-tamper)
OUT_DIR = Path("static")
QR_DIR = OUT_DIR / "qrs"
LABEL_DIR = OUT_DIR / "labels"

QR_DIR.mkdir(parents=True, exist_ok=True)
LABEL_DIR.mkdir(parents=True, exist_ok=True)

# -----------------------------
# DB setup
# -----------------------------
engine = create_engine(
    DATABASE_URL,
    connect_args={"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}
)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
Base = declarative_base()

class CardCertificate(Base):
    __tablename__ = "card_certificate"

    id = Column(Integer, primary_key=True)
    public_id = Column(String, unique=True, index=True, nullable=False)
    serial_number = Column(String, unique=True, nullable=False)
    status = Column(String, nullable=False, default="VERIFIED")

    card_name = Column(String, nullable=False)
    set_name = Column(String, nullable=True)
    year = Column(Integer, nullable=True)
    card_number = Column(String, nullable=True)
    variant = Column(String, nullable=True)

    grade = Column(Float, nullable=False)
    grader_version = Column(String, nullable=True)
    graded_at = Column(String, nullable=False)

    notes_public = Column(Text, nullable=True)
    notes_internal = Column(Text, nullable=True)

    checksum_sha256 = Column(String, nullable=True)

    images = relationship("CardImage", cascade="all, delete-orphan", back_populates="cert")

class CardImage(Base):
    __tablename__ = "card_image"

    id = Column(Integer, primary_key=True)
    certificate_id = Column(Integer, ForeignKey("card_certificate.id"), nullable=False)
    kind = Column(String, nullable=False)     # front/back/surface_L/etc.
    url = Column(String, nullable=False)

    cert = relationship("CardCertificate", back_populates="images")

Base.metadata.create_all(bind=engine)

# -----------------------------
# Helpers: IDs, signing, QR
# -----------------------------
def new_public_id(nbytes: int = 8) -> str:
    # 8 bytes => 16 hex chars
    return os.urandom(nbytes).hex().upper()

def new_serial(prefix="YCG", year=None, seq=1) -> str:
    year = year or datetime.utcnow().year
    return f"{prefix}-{year}-{seq:06d}"

def sign_public_id(public_id: str) -> str:
    mac = hmac.new(SECRET.encode("utf-8"), public_id.encode("utf-8"), hashlib.sha256).digest()
    # URL-safe, short-ish signature
    return base64.urlsafe_b64encode(mac[:16]).decode("utf-8").rstrip("=")

def verify_signature(public_id: str, sig: str) -> bool:
    return hmac.compare_digest(sign_public_id(public_id), sig)

def certificate_url(public_id: str, signed: bool = True) -> str:
    if not signed:
        return f"{BASE_URL}/c/{public_id}"
    sig = sign_public_id(public_id)
    return f"{BASE_URL}/c/{public_id}?sig={sig}"

def make_qr_png(url: str, out_path: Path, box_size: int = 10, border: int = 2) -> None:
    qr = qrcode.QRCode(
        version=None,
        error_correction=qrcode.constants.ERROR_CORRECT_M,
        box_size=box_size,
        border=border
    )
    qr.add_data(url)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    img.save(out_path)

# -----------------------------
# FastAPI app
# -----------------------------
app = FastAPI()
templates = Jinja2Templates(directory="templates")
app.mount("/static", StaticFiles(directory="static"), name="static")

@app.get("/c/{public_id}", response_class=HTMLResponse)
def public_certificate(request: Request, public_id: str, sig: str | None = None):
    # If you want signed URLs enforced, require sig:
    if sig is not None and not verify_signature(public_id, sig):
        raise HTTPException(status_code=403, detail="Invalid signature")
    # If you want to require signature always:
    # if sig is None or not verify_signature(public_id, sig): ...

    db = SessionLocal()
    try:
        cert = db.query(CardCertificate).filter(CardCertificate.public_id == public_id).first()
        if not cert:
            raise HTTPException(status_code=404, detail="Certificate not found")
        images = {im.kind: im.url for im in cert.images}
        return templates.TemplateResponse(
            "certificate.html",
            {
                "request": request,
                "cert": cert,
                "images": images,
                "public_url": certificate_url(public_id, signed=True),
            }
        )
    finally:
        db.close()

@app.get("/api/cert/{public_id}")
def cert_json(public_id: str):
    db = SessionLocal()
    try:
        cert = db.query(CardCertificate).filter(CardCertificate.public_id == public_id).first()
        if not cert:
            raise HTTPException(status_code=404, detail="Not found")
        return {
            "public_id": cert.public_id,
            "serial_number": cert.serial_number,
            "status": cert.status,
            "card_name": cert.card_name,
            "set_name": cert.set_name,
            "year": cert.year,
            "card_number": cert.card_number,
            "variant": cert.variant,
            "grade": cert.grade,
            "grader_version": cert.grader_version,
            "graded_at": cert.graded_at,
            "notes_public": cert.notes_public,
            "images": [{ "kind": im.kind, "url": im.url } for im in cert.images],
        }
    finally:
        db.close()

@app.post("/admin/issue-demo")
def issue_demo_cert():
    """Demo endpoint: creates one certificate, generates QR, returns IDs.
    Replace with your real grading pipeline integration / admin auth.
    """
    db = SessionLocal()
    try:
        # naive demo sequence:
        count = db.query(CardCertificate).count()
        public_id = new_public_id()
        serial = new_serial(prefix="YCG", year=datetime.utcnow().year, seq=count + 1)

        cert = CardCertificate(
            public_id=public_id,
            serial_number=serial,
            status="VERIFIED",
            card_name="DEMO CARD",
            set_name="Demo Set",
            year=2026,
            card_number="001",
            variant="Holo",
            grade=9.5,
            grader_version="v1.0",
            graded_at=datetime.utcnow().isoformat(timespec="seconds"),
            notes_public="Demo certificate record."
        )
        db.add(cert)
        db.commit()
        db.refresh(cert)

        # Generate QR
        url = certificate_url(public_id, signed=True)
        qr_path = QR_DIR / f"{public_id}.png"
        make_qr_png(url, qr_path)

        return {
            "public_id": public_id,
            "serial_number": serial,
            "public_url": url,
            "qr_png": f"/static/qrs/{public_id}.png",
        }
    finally:
        db.close()
```

### 2.4 Certificate page template

Create `templates/certificate.html`:

```html
<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>{{ cert.serial_number }} • Certificate</title>
  <style>
    body { font-family: system-ui, Arial; margin: 24px; }
    .card { max-width: 900px; margin: 0 auto; }
    .row { display: flex; gap: 18px; flex-wrap: wrap; }
    .panel { border: 1px solid #e5e5e5; border-radius: 12px; padding: 16px; }
    img { max-width: 100%; border-radius: 10px; }
    .badge { display: inline-block; padding: 6px 10px; border-radius: 999px; background: #f3f3f3; }
    .status-VERIFIED { background: #e7f7ea; }
    .status-FLAGGED { background: #fff4d6; }
    .status-REVOKED { background: #ffe3e3; }
    .muted { color: #666; }
    code { background: #f6f6f6; padding: 2px 6px; border-radius: 6px; }
  </style>
</head>
<body>
  <div class="card">
    <h1>Certificate</h1>
    <p class="muted">Scan URL: <code>{{ public_url }}</code></p>

    <div class="panel">
      <div class="row">
        <div>
          <div class="badge status-{{ cert.status }}">{{ cert.status }}</div>
          <h2 style="margin: 10px 0 6px;">{{ cert.card_name }}</h2>
          <div class="muted">{{ cert.set_name }} • {{ cert.year }} • #{{ cert.card_number }} • {{ cert.variant }}</div>
          <h3 style="margin: 14px 0 6px;">Grade: {{ cert.grade }}</h3>
          <div class="muted">Serial: {{ cert.serial_number }}</div>
          <div class="muted">Graded: {{ cert.graded_at }}</div>
          {% if cert.notes_public %}
            <p style="margin-top: 12px;">{{ cert.notes_public }}</p>
          {% endif %}
        </div>
      </div>
    </div>

    <h3 style="margin-top: 18px;">Images</h3>
    <div class="row">
      {% if images.get('front') %}
        <div class="panel" style="flex: 1; min-width: 260px;">
          <h4>Front</h4>
          <img src="{{ images.get('front') }}" alt="Front">
        </div>
      {% endif %}
      {% if images.get('back') %}
        <div class="panel" style="flex: 1; min-width: 260px;">
          <h4>Back</h4>
          <img src="{{ images.get('back') }}" alt="Back">
        </div>
      {% endif %}
    </div>

    <h3 style="margin-top: 18px;">About</h3>
    <div class="panel">
      <p>Add your company story, verification process, and support details here.</p>
    </div>
  </div>
</body>
</html>
```

### 2.5 Run the server
```bash
uvicorn app:app --reload --port 8000
```

Create a demo record:
```bash
curl -X POST http://localhost:8000/admin/issue-demo
```

Then open the returned `public_url` in a browser (or scan the QR).

---

## 3) Flask alternative (if you prefer)

Flask is perfectly fine too; FastAPI tends to be cleaner for APIs. If you want Flask, the same schema/QR concepts apply—ask and I’ll generate a matching Flask file set.

---

## 4) QR generation details (best practices)

### 4.1 Error correction level
Use **M** or **Q**:
- M: good balance
- Q: more robust (bigger QR)

### 4.2 Quiet zone (border)
Keep at least **2–4 modules** border.

### 4.3 Print sizing
- Minimum: **20 mm** square (for reliable scans)
- Safer: **25–30 mm** square
- Use 300 DPI+ printing

---

## 5) Label layouts (QR + serial + grade)

### 5.1 What to print on the label
Recommended fields:
- QR code
- **Serial number** (big)
- Grade (big)
- Short URL domain (for manual entry)
- Tiny “Verify at …” line
- Optional: microtext / holographic seal

### 5.2 Label size suggestions
- Small: **40 × 20 mm**
- Medium: **50 × 25 mm**
- Premium: **60 × 30 mm**

### 5.3 Generate a print-ready PDF label (Python)

Create `make_label.py`:

```python
from pathlib import Path
from reportlab.pdfgen import canvas
from reportlab.lib.units import mm
from reportlab.lib.utils import ImageReader

def make_label_pdf(qr_png: Path, out_pdf: Path, serial: str, grade: str, domain: str,
                   w_mm=50, h_mm=25):
    c = canvas.Canvas(str(out_pdf), pagesize=(w_mm*mm, h_mm*mm))

    # QR block (left)
    qr_size = min(h_mm*mm - 4*mm, 20*mm)
    c.drawImage(ImageReader(str(qr_png)), 2*mm, 2*mm, qr_size, qr_size, mask='auto')

    # Text block (right)
    x = 2*mm + qr_size + 2*mm
    c.setFont("Helvetica-Bold", 10)
    c.drawString(x, h_mm*mm - 6*mm, "CERTIFIED GRADE")
    c.setFont("Helvetica-Bold", 14)
    c.drawString(x, h_mm*mm - 12*mm, f"{grade}")
    c.setFont("Helvetica", 9)
    c.drawString(x, h_mm*mm - 17*mm, f"Serial: {serial}")
    c.setFont("Helvetica", 7)
    c.drawString(x, 4*mm, f"Verify: {domain}")

    c.showPage()
    c.save()

if __name__ == "__main__":
    # Example usage:
    make_label_pdf(
        qr_png=Path("static/qrs/8F3A9C72.png"),
        out_pdf=Path("static/labels/8F3A9C72_label.pdf"),
        serial="YCG-2026-000812",
        grade="9.5",
        domain="yourdomain.com"
    )
```

This produces a **PDF** you can send to:
- a label printer (Brother/Zebra)
- a print sheet workflow
- a slab insert workflow

---

## 6) Anti-tamper techniques (practical, ranked)

### Level 0 — Basic (cheap, “good enough” to start)
- Non-guessable `public_id`
- Public page shows:
  - images
  - graded timestamp
  - grade
  - serial
- If someone copies a QR, it points to the same public page (clone is detectable only if you track scans)

**Cost:** essentially free.

---

### Level 1 — Signed URLs (recommended baseline)
Put a signature in the URL:

```
https://yourdomain.com/c/8F3A9C72?sig=AbCDef...
```

- Server verifies signature (HMAC)
- Prevents attackers from inventing valid URLs
- Doesn’t prevent copying an existing QR, but stops easy “make up an ID”

**Cost:** free (dev time only)

> Included in the FastAPI code above (`CERT_SECRET`, `sign_public_id()`).

---

### Level 2 — Tamper-evident physical labels
- **Destructible vinyl** (breaks when removed)
- **Void** labels (“VOID” pattern appears when peeled)
- UV ink / microtext / guilloche background

**Cost per label:** typically **£0.10–£0.80** depending on material and volume.

---

### Level 3 — Scan analytics & anomaly detection
Store scan events (privacy-safe):
- hash IP (not raw)
- timestamp
- country
- user agent

Flag:
- impossible travel (UK → US in 5 minutes)
- 500 scans/day on one cert
- simultaneous scans in many countries

**Cost:** small (database + minimal backend work)

---

### Level 4 — Challenge-response (stronger anti-clone)
QR opens page that displays a **dynamic challenge**:
- “Tap to verify” generates a one-time token
- Token is checked server-side
- Useful if you also have a second factor (like NFC or printed scratch-off)

**Cost:** moderate dev, minimal per-unit cost

---

### Level 5 — NFC with cryptographic tags (strongest)
Use NFC tags that support cryptographic authentication (or store signed payloads):
- tap verifies authenticity
- harder to clone than QR-only

**Cost:** higher tags + dev + (optional) app support

---

## 7) Cost breakdown (typical)

### Per-card costs (QR only)
- QR generation: **free**
- Printing label (basic): **£0.05–£0.30**
- Tamper-evident label: **£0.20–£0.80**

### Infrastructure
- Domain: **~£10/year**
- Hosting: **~£5–£25/month** (small traffic)
- Database: included in hosting (SQLite) or managed Postgres (extra)
- Dev time: biggest cost (but you can build MVP quickly)

---

## 8) Implementation checklist (MVP → production)

### MVP (weekend build)
- [ ] SQLite DB + FastAPI
- [ ] Issue cert record + public page
- [ ] Generate QR PNG
- [ ] Print simple label
- [ ] Use non-guessable public_id

### Production hardening
- [ ] Signed URLs enforced
- [ ] Admin auth for issuing certs
- [ ] CDN for images
- [ ] Scan logging + anomaly flags
- [ ] Tamper-evident label stock

---

## 9) Next step I can generate for you
If you want, I can produce:
- a **ready-to-run repo zip** structure (files laid out, requirements.txt)
- an **admin page** to create/search/edit certificates
- a **batch label generator** (PDF sheet of 30 labels)
- a **verification badge** design and UI flow
