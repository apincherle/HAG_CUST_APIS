# Enrichment Service Spec (Pokémon) — OCR + PokemonTCG Data Matching

This is a **single-purpose internal API** intended to be called by the **Orchestrator**.

It:
1) Accepts **card image(s)** (front + optional macro crops) and optional **text hints**
2) Runs **OCR** to extract identification fields (name, collector number, HP, etc.)
3) Matches against the **PokemonTCG `pokemon-tcg-data` dataset** (local copy in DB/index) to identify the card
4) Returns a **matched card id + confidence + evidence** (plus optional ranked candidates)

Dataset reference:
- PokemonTCG/pokemon-tcg-data (“data found within the Pokémon TCG API”) citeturn0search0

---

## 1) Scope / Non-goals

### In scope
- Pokémon cards only (for this service)
- OCR-driven identification using the `pokemon-tcg-data` dataset
- Deterministic matching + scored result
- Evidence returned for auditability (what OCR read)

### Not in scope (for MVP)
- Image similarity / visual matching beyond OCR
- Pricing enrichment (TCGplayer, etc.)
- Multi-language OCR tuning beyond basic support
- Full grading (this is **identification only**)

---

## 2) Inputs

### Images (supported modes)
The service must accept images in one of three ways:

**A) Direct upload (multipart) — easiest for MVP**
- `front` (required)
- `macro_number` (optional but strongly recommended)
- `macro_name` (optional)
- `back` (optional)

**B) Blob URLs (SAS)**
- Provide `front_url`, `macro_number_url`, etc.

**C) Capture IDs (if enrichment service can resolve images via SQL)**
- Provide `front_capture_id`, `macro_number_capture_id`, etc.

> MVP recommendation: implement **A** first, then add **B**.

### Optional text hints
- `description` (customer one-liner, e.g. “Charizard 4/102 Base Set Holo”)
- `language` (e.g., `en`)
- `expected_set` (string hint)
- `expected_number` (string hint)

---

## 3) Outputs

Minimum output (required):
```json
{
  "matched_card_id": "base1-4",
  "confidence": 0.95,
  "method": "OCR+TCG",
  "evidence": {
    "name": "Charizard",
    "number": "4/102"
  }
}
```

Optional extended output (recommended for debugging + review):
- `status`: MATCHED | AMBIGUOUS | NOT_FOUND
- `candidates[]`: top N candidates with scores + reasons
- OCR blocks with confidence and crop metadata

---

## 4) Dataset (PokemonTCG/pokemon-tcg-data)

### Source of truth
Use a pinned snapshot (commit SHA or release tag) of `pokemon-tcg-data`. citeturn0search0

### Minimum fields needed for matching
From the dataset `cards` JSON:
- `id` (e.g., `base1-4`)
- `name` (e.g., `Charizard`)
- `number` (collector number as a string, e.g., `4`)
- `set.id`, `set.name` (or join to sets)
- optional: `hp`, `rarity`, `types` (helpful for tie-breaking)

### Recommended local storage
Store in a local DB table/index for fast lookup:
- `tcg_cards(id, name, name_norm, number, set_id, hp, json_payload, updated_at)`
Indexes:
- `(number)`
- `(set_id, number)`
- `name_norm` (and trigram/FTS if available)

---

## 5) OCR Extraction Requirements

### Required extracted fields (MVP)
- **collector_number_raw**: e.g., `"4/102"` or `"023/198"` or `"TG10/TG30"`
- **name_raw**: `"Charizard"`
- Optional: **hp_raw**: `"120"`

### Normalization rules
- `name_norm`: uppercase, strip punctuation, collapse whitespace
- `number_norm`:
  - If `collector_number_raw` includes `/`, take left side (e.g., `"4/102" -> "4"`)
  - Strip spaces and non-alphanumerics except letters for special numbering (e.g. `"H32"`, `"SV107"`)
- `hp_norm`: integer if parseable

### OCR cropping (configurable)
Crops should be defined in a config file as normalized coordinates `[x1, y1, x2, y2]` relative to the image.

Suggested defaults (tune on your rig):
- `name_crop`: top area
- `hp_crop`: top-right
- `number_crop`: bottom area near collector number

The service must return crop metadata in evidence.

---

## 6) Matching & Scoring Rules

### Candidate retrieval
1) If `number_norm` exists:
   - Query `tcg_cards.number == number_norm`
2) If no candidates OR too many:
   - Query by `name_norm` similarity
3) Apply hints (if provided):
   - `expected_set` boosts candidates whose set name matches
   - `expected_number` boosts exact number match

### Scoring (recommended baseline)
- `number exact match`: **+100**
- `name exact match`: **+60**
- `name similarity`: **+60 * similarity(0..1)** (if not exact)
- `hp match`: **+20**
- `set hint match`: **+30**
- small penalties:
  - hp mismatch: **-10** (if both known)
  - name mismatch below threshold: candidate dropped

### Decision thresholds
- Accept MATCHED if:
  - `top_score >= 140`
  - and `top_score - second_score >= 25`
- Otherwise:
  - `status = AMBIGUOUS` if there are reasonable candidates
  - `status = NOT_FOUND` if no candidates

### Confidence mapping
Map score to `confidence` (0..1). For MVP:
- `confidence = min(1.0, top_score / 200.0)` then adjust with margin:
- `confidence += min(0.1, (margin/100.0))`
Clamp to [0,1].

---

## 7) API Definition

### 7.1 Endpoint: Identify (sync)

`POST /internal/v1/enrichment/pokemon/identify`

#### Content-Type options
- `multipart/form-data` (recommended)
- `application/json` (if using URLs/capture IDs)

#### Request (multipart) — MVP
Form fields:
- `front` (file, required)
- `macro_number` (file, optional)
- `back` (file, optional)
- `description` (string, optional)
- `language` (string, optional)
- `expected_set` (string, optional)

#### Response (minimum)
HTTP 200
```json
{
  "matched_card_id": "base1-4",
  "confidence": 0.95,
  "method": "OCR+TCG",
  "evidence": {
    "name": "Charizard",
    "number": "4/102"
  }
}
```

#### Response (extended)
HTTP 200
```json
{
  "status": "MATCHED",
  "matched_card_id": "base1-4",
  "confidence": 0.95,
  "method": "OCR+TCG",
  "evidence": {
    "name": "Charizard",
    "number": "4/102",
    "hp": "120"
  },
  "extracted_fields": {
    "name_raw": "Charizard",
    "name_norm": "CHARIZARD",
    "collector_number_raw": "4/102",
    "number_norm": "4",
    "hp_raw": "120",
    "hp_norm": 120
  },
  "candidates": [
    {
      "card_id": "base1-4",
      "score": 186.4,
      "reasons": [
        "number_exact:+100",
        "name_similarity:0.97->+58.2",
        "hp_match:+20"
      ],
      "card_snapshot_min": {
        "id": "base1-4",
        "name": "Charizard",
        "number": "4",
        "set": {"id": "base1", "name": "Base"},
        "rarity": "Rare Holo"
      }
    }
  ],
  "ocr_evidence": {
    "crops": [
      {"name": "number_crop", "box_norm": [0.05, 0.83, 0.95, 0.98]},
      {"name": "name_crop", "box_norm": [0.05, 0.05, 0.85, 0.18]}
    ],
    "blocks": [
      {"crop": "number_crop", "text": "4/102", "confidence": 0.92}
    ]
  }
}
```

### 7.2 Error responses
- `400` invalid request / missing required image
- `422` unreadable OCR / insufficient fields extracted
- `500` internal errors

---

## 8) Orchestrator Integration Contract

### Call pattern
1) Orchestrator calls `POST /identify` with `front` + `macro_number` (preferred)
2) Service returns result
3) Orchestrator persists:
   - `matched_card_id`
   - `confidence`
   - `method`
   - `evidence` (plus extended debug output if stored)

### Idempotency (recommended)
Support `Idempotency-Key` header:
- If the same key is reused, return the same result.

---

## 9) Implementation Notes (MVP)

### Tech recommendations
- Language: Python (FastAPI)
- OCR: PaddleOCR (fallback: Tesseract)
- Image preprocessing:
  - deskew (optional)
  - contrast normalization on number crop
- Dataset ingestion:
  - ingest `pokemon-tcg-data` JSON into DB on startup or CI pipeline
  - keep `dataset_version` pinned and recorded in responses

### Performance targets (per card)
- OCR + match < 1–3 seconds typical (depending on hardware)
- Use `macro_number` to dramatically reduce ambiguity

---

## 10) Minimal Output (required by spec)

The service MUST be able to return *at least* this JSON shape:

```json
{
  "matched_card_id": "base1-4",
  "confidence": 0.95,
  "method": "OCR+TCG",
  "evidence": {
    "name": "Charizard",
    "number": "4/102"
  }
}
```

---
