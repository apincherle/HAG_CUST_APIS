# HAGS End-of-Day Label Batch — Plan

Plan for producing **print-ready slab labels** from graded certificates, using the existing HAGS cert format and **PDF templates** (70 mm × 20 mm, front + back).

Related:

- [certificate_qr_lookup_flow.md](./certificate_qr_lookup_flow.md) — cert + QR creation
- [../src/main/resources/labels/README.md](../src/main/resources/labels/README.md) — template placeholders

---

## Current status

| Item | Status |
|------|--------|
| Label design (HTML/CSS) | Done — `src/main/resources/labels/` |
| Grade label format (`9.5 GEM MT`) | Done — `GradeLabelFormatter` |
| Logo asset | Done — `src/main/resources/labels/assets/hags_logo_gold.png` |
| PDF renderer (single/batch pages) | Done — `LabelPdfRenderer` |
| **A4 batch sheet layout** | Done — `renderBatchOnSheets()` (3×12 grid, 2 mm row gap) |
| **Print test PDF (single)** | `mvn test -Dtest=LabelPdfRendererPrintTest` → `target/sample_labels.pdf` |
| **Print test PDF (20 batch)** | `mvn test -Dtest=LabelPdfRendererBatchPrintTest` → `target/sample_labels_batch_20.pdf` |
| SQL batch tables + EOD export | Not started |
| REST API `/api/label-batches` | Not started |
| Blob upload to `grading-exports` | Not started |
| Bench scan / encapsulation workflow | Not started |

---

## 1. Goal

At end of day (or on demand), export every card that is **graded, certificated, and ready for slab** into:

| Output | Purpose |
|--------|---------|
| Batch manifest JSON | Audit, encapsulation bench, reprints |
| Print pack PDF | Physical labels (front + back per card) |
| QR PNGs | Copied into batch folder for self-contained print |

Flow: **Grade → create cert + QR → EOD batch → print PDF → encapsulate → ship**.

---

## 2. HAGS certificate format (unchanged)

| Concept | Value |
|---------|--------|
| Slab cert number | `serial_number` → `HAGS-2026-000123` |
| QR URL | `{baseUrl}/cert/{serialNumber}?sig={signature}` |
| QR PNG file | `{publicId}.png` in `qr.certificate.qr-storage-path` |
| Create at grading | `POST /api/qr-certificate/generate` |
| Public lookup | `GET /api/certificates/{certificateId}` |

Certs are created **during grading**, not at EOD. The label batch only **reads** existing `card_certificate` rows.

---

## 3. Label physical spec

| Property | Value |
|----------|--------|
| Size | **70 mm × 20 mm** per side |
| Front | Hags, grade (`9.5` + `GEM MT`), card name, set, card #, `#HAGS-…` |
| Back | Hags + logo, tagline, verify URL, QR + “SCAN TO VERIFY” |
| Print order | Front page, then back page, per card |

Templates: `hags_slab_label_front_v1.html`, `hags_slab_label_back_v1.html`.

---

## 4. Batch manifest JSON (target shape)

```json
{
  "batchId": "LBL-2026-07-06-001",
  "batchDate": "2026-07-06",
  "labelTemplateVersion": "v1",
  "cards": [
    {
      "certificateNumber": "HAGS-2026-000123",
      "publicId": "…",
      "cardName": "Umbreon ex",
      "setName": "Prismatic Evolutions",
      "cardNumber": "161/131",
      "grade": 9.5,
      "gradeLabel": "9.5 GEM MT",
      "qrCodeUrl": "https://www.hags-grading.co.uk/cert/HAGS-2026-000123?sig=…",
      "qrCodeImagePath": "qr_codes/{publicId}.png"
    }
  ]
}
```

---

## 5. Implementation phases

### Phase A — Print proof (now)

**Goal:** Open a PDF and print on 70×20 mm stock to validate layout and QR scan.

1. Run the print test (see below).
2. Open `target/sample_labels.pdf`.
3. Print at **100 % scale** (no fit-to-page).
4. Confirm front text readable; back QR scans to cert URL.

**Command:**

```bash
mvn test -Dtest=LabelPdfRendererPrintTest
```

**Output:** `target/sample_labels.pdf` (2 pages: front, back).

### Phase B — Wire to real certificates

1. Add `label_status` column on `card_certificate` (`PENDING` / `PRINTED` / `APPLIED`).
2. Add `label_batches` + `label_batch_items` tables.
3. `LabelBatchService`: query eligible certs → build `LabelPrintData` list → call `LabelPdfRenderer`.
4. Copy QR PNGs from cert storage into batch folder.
5. Write `hags_label_batch.json` alongside PDF.

### Phase C — API + automation

1. `POST /api/label-batches` — create batch (optional `date`, `submissionId`).
2. `GET /api/label-batches/{batchCode}/pdf` — download print pack.
3. `POST /api/label-batches/{batchCode}/reprint` — single label reprint.
4. Scheduled EOD job; upload to Azure blob `grading-exports/label-batches/{batch_code}/`.

### Phase D — Bench + shipping

1. Scan `HAGS-2026-000123` at encapsulation bench.
2. Mark `label_status = APPLIED`.
3. Submission-level “ready to ship” when all items applied.

---

## 6. Eligibility (for Phase B)

Include a cert when:

- Row exists in `card_certificate`
- `status = VERIFIED`
- `grade` not null
- QR PNG exists (or regenerable)
- `label_status = PENDING`
- Not already in an open batch

---

## 7. Blob layout (Phase C)

```text
grading-exports/label-batches/{batch_code}/
  hags_label_batch.json
  qr_codes/{publicId}.png
  labels/{batch_code}_labels.pdf
```

---

## 8. Key code locations

| Component | Path |
|-----------|------|
| HTML templates | `src/main/resources/labels/` |
| PDF renderer | `src/main/java/com/example/qrcert/label/LabelPdfRenderer.java` |
| Grade label text | `src/main/java/com/example/qrcert/util/GradeLabelFormatter.java` |
| Print test | `src/test/java/com/example/qrcert/label/LabelPdfRendererPrintTest.java` |
| Cert + QR creation | `src/main/java/com/example/qrcert/service/CertificateService.java`, `QrCodeService.java` |

---

## 9. Configuration (future)

```properties
label.batch.template-front=classpath:labels/hags_slab_label_front_v1.html
label.batch.template-back=classpath:labels/hags_slab_label_back_v1.html
label.batch.label-width-mm=70
label.batch.label-height-mm=20
label.batch.blob-container=grading-exports
label.batch.blob-prefix=label-batches
```

---

## 10. Next actions

1. **You:** run `mvn test -Dtest=LabelPdfRendererPrintTest` and print `target/sample_labels.pdf`.
2. **You:** confirm typography/truncation on real stock; share tweaks.
3. **Dev:** Phase B — SQL + `LabelBatchService` from live `card_certificate` rows.
4. **Dev:** Phase C — REST API and EOD automation.
