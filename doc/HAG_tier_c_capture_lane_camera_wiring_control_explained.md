# Tier C Capture Lane – Camera, Wiring & Control (Clear Reference)

This document is written to be **directly downloadable as a `.md` file** and understandable by:
- engineers
- photographers
- investors
- operators

No prior context required.

---

## 1. What a Tier C Lane Is (Plain English)

A **Tier C capture lane** is a **fixed, machine-like imaging station** designed to:

- photograph a trading card **the same way every time**
- capture **surface detail** (scratches, dents, scuffs)
- run **automatically** with minimal operator input
- upload images instantly to the cloud with metadata

It uses **one camera**, **multiple fixed lights**, and **software-controlled sequencing** instead of manual photography.

---

## 2. Physical Camera Setup (What You See on the Table)

### Top-down view (physical layout)

```
        [ Light A – Top / Diffuse ]
                 ↓

   [ Light B – Left ]     [ Light C – Right ]
          →                     ←

                 [ CAMERA ]
                 (macro lens)
                     │
                     │  fixed height
                     │
              ┌───────────────┐
              │   COPY STAND  │
              └───────────────┘
                     │
              ┌───────────────┐
              │  CARD JIG     │  ← card drops here
              │ (fixed XY)    │
              └───────────────┘
                     │
        [ Light D – Rear / Raking ]
                     ↑
```

### What each part does

| Part | Purpose |
|---|---|
| Camera + macro lens | Captures high-resolution images with zero distortion |
| Copy stand | Keeps camera **perfectly parallel** to the card |
| Card jig | Forces every card into the **same position + rotation** |
| Light A (top) | Even illumination for centering + print inspection |
| Light B/C (sides) | Raking light to reveal scratches and edge wear |
| Light D (rear) | Opposing raking light to expose dents/texture |

Once calibrated, **nothing moves**.

---

## 3. Electrical & Control Diagram (How Everything Connects)

This design avoids dangerous mains wiring and is safe for early-stage deployment.

### System wiring overview

```
┌──────────────────────────────┐
│          EDGE PC             │
│  - Camera tether control     │
│  - Light on/off control      │
│  - Metadata + hashing        │
│  - Cloud upload              │
└──────────────┬───────────────┘
               │ USB-C
        ┌──────▼──────┐
        │   CAMERA    │
        │ (manual)    │
        └─────────────┘
               │
               │ mounted
        ┌──────▼──────┐
        │ COPY STAND  │
        └─────────────┘

        ┌─────────────────────────────────────┐
        │ SMART PLUG STRIP (Wi-Fi / LAN)       │
        │ Plug 1 → Light A (Top)               │
        │ Plug 2 → Light B (Left)              │
        │ Plug 3 → Light C (Right)             │
        │ Plug 4 → Light D (Rear)              │
        └─────────────────────────────────────┘

        ┌──────────────────────────────┐
        │ FOOT PEDAL / BUTTON           │
        │ (USB HID device)              │
        └──────────────────────────────┘
```

### Why smart plugs are used

- No DIY mains wiring
- Lights switch via software
- Each light = one known direction
- Easy to replace or expand

The edge PC simply sends: **ON → capture → OFF**.

---

## 4. Capture Sequence (What Happens Per Card)

### Operator actions
1. Scan job/card barcode (or select job)
2. Place card in jig
3. Press pedal (or card presence triggers automatically)
4. Flip card when prompted

### Automated sequence (front side)

```
Light A ON → Capture → OFF
Light B ON → Capture → OFF
Light C ON → Capture → OFF
Light D ON → Capture → OFF
```

### Then repeat for the back side

Total images per card (Tier C):
- 4 images (front)
- 4 images (back)

These 8 images enable **photometric-style surface analysis**.

---

## 5. Why This Reveals Surface Damage

Each light angle creates **shadows** from micro-height changes:

- scratches
- dents
- indentations
- surface waviness

By comparing the same pixel across multiple lighting directions, software can infer **surface normals** (shape cues) without lasers.

This is **faster, cheaper, and more reliable** than laser scanning for glossy cards.

---

## 6. Cross-Polarization (Optional but Strongly Recommended)

### What it is
- Polarizing film on lights
- Circular polarizer on lens

### What it does
- Suppresses glare from gloss/holo foil
- Makes print defects visible
- Improves AI repeatability

Cross-polarization is applied to **diffuse shots** (usually Light A).

---

## 7. Where the Epson V850 Pro Fits (Not in the Lane)

The **Epson V850 Pro is NOT part of the Tier C capture lane**.

It is a **separate station** used for:
- archival scans
- print line inspection
- dispute arbitration
- ID/reference capture

It should not slow down your production lanes.

---

## 8. Mental Model (If You Remember One Thing)

> Treat the capture lane like a CNC machine, not a camera.

- Fixed geometry
- Fixed lighting
- Deterministic sequence
- Software decides everything

This is how you get **speed, consistency, and defensibility**.

---

## 9. Next Optional Files I Can Generate

- `tier_c_lane_purchase_order.md`
- `edge_agent_light_control_pseudocode.md`
- `operator_one_page_sop.md`
- `photometric_surface_processing.md`

Just tell me which one you want next.

