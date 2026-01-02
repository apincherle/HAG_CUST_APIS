# Automated Card Grading System – Master Specification

> This document consolidates **all requested Markdown files** into a single master `.md` file for ease of review and version control. Each section below is designed to be **copy‑pasted into its own standalone `.md` file** if desired.

---

## FILE 1 — `lane_hardware_bom.md`

# Capture Lane Hardware – Bill of Materials (BOM)

## Objective
Create a **fully automated, repeatable capture lane** for Pokémon, TCG, and future sports cards with minimal operator input and maximum AI consistency.

## Core Principles
- Camera never moves
- Lighting states are deterministic
- No per-card adjustments
- Identical lanes = scalable throughput

## Capture Lane Components

### Camera Body (choose ecosystem)
- Mirrorless body with reliable USB-C tethering
- 24–45MP preferred (detail > speed)

Examples:
- Sony a7R IV / a7 IV
- Canon R5 / R6 II
- Nikon Z7 II / Z8

### Macro Lens (critical)
- True macro (1:1)
- Flat field, minimal distortion

Examples:
- 90–105mm macro (preferred)
- 60mm macro (space-saving)

### Copy Stand
- Rigid vertical column
- Heavy base (vibration kills sharpness)
- Fixed height once calibrated

### Lighting (software-controlled)
**Diffuse / Cross‑Polarized**
- 2× high-CRI LED panels (95+ CRI)
- Polarizing film on lights
- Circular polarizer (CPL) on lens

**Raking Light**
- 2× directional LED bars (low angle)
- Left + right independent control

### Control & Automation
- USB relay or DMX controller (lights)
- Foot pedal or IR break-beam trigger
- Mini PC / Mac mini (per 2–4 lanes)

### Physical Fixtures
- Card placement jig (XY + rotation stops)
- Anti-static mat
- Bubble level (setup only)

---

## FILE 2 — `cloud_pipeline_architecture.md`

# Cloud Upload & Processing Architecture

## Design Goals
- Zero human file handling
- Immediate upload
- Immutable originals
- Full audit trail

## Edge Agent Responsibilities
Runs locally per workstation:
1. Camera tether capture
2. Light-state orchestration
3. Image hashing (SHA-256)
4. Metadata injection
5. Buffered cloud upload

## Required Metadata
```json
{
  "job_id": "UUID",
  "card_id": "UUID",
  "side": "front | back",
  "light_mode": "diffuse_xpol | rake_left | rake_right",
  "lane_id": "lane_01",
  "timestamp": "ISO-8601",
  "hash": "sha256"
}
```

## Object Storage Layout
```
/raw/{job_id}/{card_id}/{side}/{light_mode}.dng
/processed/{job_id}/{card_id}/analysis.json
/evidence/{job_id}/{card_id}/tiles/
```

## Fail-Safes
- Local disk queue if internet drops
- Hash verification on upload
- No overwrite allowed

---

## FILE 3 — `ai_grading_services.md`

# AI Grading Services Architecture

## Philosophy
AI produces **subscores + evidence**, not unquestionable final grades.

## Modular Scorers

### 1. Card Detection & Alignment
- Card mask
- Perspective correction
- Orientation normalization

### 2. Centering Scorer
- Border detection
- L/R and T/B ratios
- Game-specific tolerance profiles

### 3. Corner Scorer
- Individual corner crops
- Whitening, rounding, dents

### 4. Edge Scorer
- Perimeter strip analysis
- Chipping and edge wear

### 5. Surface Scorers
**Diffuse / Cross-Polarized**
- Print lines
- Scuffs
- Ink defects

**Raking Light**
- Dents
- Scratches
- Texture anomalies

## Outputs Per Module
```json
{
  "subscore": 8.5,
  "confidence": 0.93,
  "evidence": ["tile_01.png", "heatmap.png"]
}
```

## Grade Policy Engine
- Converts subscores → final grade
- Encodes company grading philosophy
- Fully versioned

---

## FILE 4 — `dispute_and_review_workflow.md`

# Human Review & Dispute Workflow

## Normal Flow
1. AI pre-grade
2. Human approval (seconds, not minutes)
3. Grade locked

## Exception Review
Triggered when:
- Low AI confidence
- Borderline grade thresholds
- Surface ambiguity

Reviewer sees:
- Original RAWs
- Evidence tiles
- AI reasoning

## Customer Disputes
- Immutable originals retrieved
- Evidence re-presented
- Optional premium surface scan
- Decision logged (no silent changes)

## Audit Requirements
- No image deletion
- No grade edits without reason
- Full version history

---

## FILE 5 — `discussion_summary.md`

# Project Summary – Automated Card Grading System

## Scope
- Pokémon, TCGs initially
- Sports cards later
- High throughput
- Maximum automation

## Key Decisions
- Camera-based capture (not scanners)
- Cross-polarized + raking light
- AI pre-grade + human verification
- Cloud-first architecture
- Laser scanning = niche / premium only

## Recommended Roadmap
1. Launch with 2–4 identical capture lanes
2. Add photometric stereo for surface confidence
3. Introduce laser/structured-light only for arbitration

## Guiding Principle
> Consistency beats cleverness. Automation beats artistry. Evidence beats opinion.

---

*End of master Markdown specification.*

