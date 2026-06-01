# Certificate QR + public lookup (HAG_CUST_APIS)

## Where QR codes are generated

| Service | Role |
|---------|------|
| **HAG_CUST_APIS** | **Production** — create certificate, store `inspectionId`, generate slab QR |
| HAGS_qr_gen_cert | Generic URL QR tool only (no certificate registry) |
| HAGS_qr_gen_ui | UI client for HAGS_qr_gen_cert (not for production slabs) |

## End-to-end flow

```text
1. HAGS_ximilar_ai   POST /v1/cards/inspect     → inspectionId + grade data
2. HAG_CUST_APIS     POST /api/qr-certificate/generate
                       body: inspectionId, itemId (cardId), customerId, submissionId, card fields…
                     → serialNumber (certificateId on slab)
                     → QR PNG encodes https://site/cert/{serialNumber}
                     → DB row: certificateId ↔ inspectionId ↔ cardId (item_id)
3. User scans QR     → hags_certificate_lookup_ui /cert/{certificateId}
4. Lookup UI         GET /api/certificates/{certificateId}  → card + grade from DB
```

## Key APIs (HAG_CUST_APIS)

### Create certificate + QR (grading desk)

```http
POST /api/qr-certificate/generate
Content-Type: application/json

{
  "inspectionId": "6b314d52-f3cc-41dc-9f81-7c848c286d64",
  "itemId": "card-uuid-from-submission",
  "customerId": "...",
  "submissionId": "...",
  "cardName": "Charizard",
  "setName": "Base Set",
  "year": 1999,
  "grade": 9.5
}
```

Response includes `certificateNumber` / `serialNumber`, `gradedDate` (yyyy-MM-dd), `inspectionId`, `certificateUrl`, `qrImageUrl`.

### Public lookup (verify site)

```http
GET /api/certificates/HAGS-2026-000123
GET /api/certificates/HAGS-2026-000123/inspection
GET /api/certificates/by-card/{cardId}
```

### Configuration

```properties
qr.certificate.base-url=https://your-verify-site.azurestaticapps.net
qr.certificate.verification-path=/cert
```

## ID glossary

| Name | Field | Example |
|------|--------|---------|
| certificateId | `serial_number` | `HAGS-2026-000123` |
| inspectionId | `inspection_id` | Ximilar UUID |
| cardId | `item_id` | submission item UUID |
| publicId | `public_id` | opaque hex (legacy `/c/{publicId}` URLs) |
