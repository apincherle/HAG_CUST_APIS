# Tier C Lane – Operational & Software Specifications

This document contains **four standalone Markdown files** combined into a single reference for easy download and version control.

Each section is designed to be copied into its own `.md` file without modification.

---

## FILE 1 — `tier_c_lane_purchase_order.md`

# Tier C Capture Lane – Purchase Order (UK Reference)

## Assumptions
- Sony ecosystem (swap Canon/Nikon equivalents freely)
- One Tier C lane
- Prices are reference-level, not quotes

## Per-Lane Hardware

| Category | Item | Qty | Unit £ | Subtotal £ |
|---|---|---:|---:|---:|
| Camera | Sony A7 IV body | 1 | 1,324 | 1,324 |
| Lens | Sony FE 90mm f/2.8 Macro | 1 | 500 | 500 |
| Stand | Kaiser RS 2 XA copy stand | 1 | 304 | 304 |
| Lighting | Godox LP600BI LED panels | 4 | 95 | 380 |
| Control | Smart plugs (4-pack) | 1 | 28 | 28 |
| Optics | Polarizing film (lights) | 1 | 64 | 64 |
| Optics | CPL filter (lens) | 1 | 24 | 24 |
| Control | USB foot pedal | 1 | 79 | 79 |
| Compute | Mini PC (Intel N100 class) | 1 | 156 | 156 |
| Cabling | USB-C tether cable | 1 | 24 | 24 |
| Cabling | Powered USB hub | 1 | 27 | 27 |
| Mounting | Clamp/arm mounts | 4 | 16 | 64 |
| Fixtures | Card jig + ESD mat | 1 | 78 | 78 |
| Misc | Fasteners, cable mgmt | — | 50 | 50 |

**Per-lane total:** ~£3,050

## Shared (One-Time)
- Barcode scanner
- Label printer
- Spare CPL + polar film

## Optional Station
- Epson V850 Pro flatbed (archival / dispute use only)

---

## FILE 2 — `edge_agent_light_control_pseudocode.md`

# Edge Agent – Light Control & Capture Logic

## Purpose
Coordinate **lights + camera + metadata + upload** deterministically.

## Core Loop (Pseudo-code)

```python
on_card_present():
    for side in ['front', 'back']:
        for light in ['A', 'B', 'C', 'D']:
            lights.on(light)
            wait(100ms)
            image = camera.capture()
            lights.off(light)
            metadata = {
                'job_id': current_job,
                'card_id': current_card,
                'side': side,
                'light': light,
                'lane_id': LANE_ID,
                'timestamp': now(),
                'hash': sha256(image)
            }
            save_local(image, metadata)
            enqueue_upload(image, metadata)
        prompt_operator('Flip card')
```

## Design Rules
- Camera settings locked
- No retries without logging
- Upload async, never blocks capture

---

## FILE 3 — `operator_one_page_sop.md`

# Tier C Lane – Operator SOP (One Page)

## Start of Shift
- Clean lens + card jig
- Verify lights respond
- Confirm test capture uploads

## Per Card
1. Scan job barcode
2. Place card in jig
3. Press pedal
4. Wait for prompt
5. Flip card
6. Remove card when done

## Do NOT
- Adjust camera
- Adjust lights
- Rename files
- Retry captures manually

## If Something Fails
- Re-seat card
- Re-run capture
- Flag job if unsure

---

## FILE 4 — `photometric_surface_processing.md`

# Photometric-Style Surface Processing

## Inputs
- 4 directional images (same side)
- Fixed camera geometry

## Method
- Normalize brightness
- Compare pixel response across light directions
- Infer surface normal / contrast cues

## What It Detects Well
- Dents
- Scratches
- Pressure marks

## What It Does NOT Replace
- Human judgment
- Final grading authority

## Output
- Surface subscore
- Defect heatmaps
- Evidence tiles

---

*End of Tier C operational specifications.*

