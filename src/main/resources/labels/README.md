# Hags slab label templates (v1)

Full implementation plan: [doc/hags_label_batch_plan.md](../../../doc/hags_label_batch_plan.md)

Design reference: white navy + gold variant (70 mm × 20 mm). Black variant mock-up saved for a future `hags_slab_label_*_black_v1.html` theme.

## Files

| File | Purpose |
|------|---------|
| `label_theme.css` | Shared colours, borders, typography |
| `hags_slab_label_front_v1.html` | Front: logo, Hags, grade, card name, set, card #, cert # |
| `hags_slab_label_back_v1.html` | Back: logo, Hags, tagline, verify URL, QR |
| `assets/hags_logo_gold.png` | **Add your gold witch logo here** (from design files) |

## Placeholders (filled by label batch PDF engine)

| Placeholder | Example |
|-------------|---------|
| `{{companyName}}` | Hags |
| `{{logoImage}}` | file path or data URI to `hags_logo_gold.png` |
| `{{cardName}}` | Umbreon ex |
| `{{setName}}` | Prismatic Evolutions |
| `{{cardNumber}}` | 161/131 |
| `{{certificateNumber}}` | HAGS-2026-000123 |
| `{{gradeNumeric}}` | 9.5 |
| `{{gradeSuffix}}` | GEM MT |
| `{{certificateUrlDisplay}}` | `hags.co/cert/HAGS-2026-000123` (short text; QR uses full Azure URL) |
| `{{qrImage}}` | Path to QR PNG (`{publicId}.png`) |

## Grade label style

Numeric grade from DB is formatted for the slab as **`{grade} GEM MT`** (e.g. `9.5 GEM MT`, `10 GEM MT`).

Java: `com.example.qrcert.util.GradeLabelFormatter`

## Regenerate sample PDFs (after template edits)

Run this **after every template change** — it rebuilds **all** sample PDFs from the current HTML/CSS:

```bash
mvn test -Dtest=LabelPdfRendererPrintTest
```

| Output file | Contents |
|-------------|----------|
| `target/sample_labels.pdf` | Single label (2 pages) |
| `target/sample_labels_batch_20.pdf` | 20 labels on A4 |
| `target/sample_labels_batch_80.pdf` | 80 labels on A4 |

Close any open PDF in your viewer before reopening — Windows often caches the old file.

## Print test (sample PDF)

## Print

- Page size: **70 mm × 20 mm** per side
- Order per card: **front page**, then **back page**
- Scale: 100 % — no fit-to-page
