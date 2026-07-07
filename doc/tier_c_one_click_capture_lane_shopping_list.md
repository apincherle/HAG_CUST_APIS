# Tier C Capture Lane – Shopping List & One-Click Automation Spec

## Purpose

This document defines a practical single-lane capture setup for professional trading-card imaging using:

- Sony A7 IV USB-tethered camera
- Sony 90mm macro lens
- Four independently controlled SmallRig-style LED lights
- Cross-polarised lighting
- ESP32/Arduino light control
- One-click / one-pedal automated capture per card

The goal is that an operator can place a card in the jig, press one button or foot pedal, and automatically capture all required images for professional grading evidence.

---

## Core Requirement

Each card should be captured with **one operator action**.

The automation must:

1. Detect or receive a card trigger.
2. Capture one full set of images for the front.
3. Prompt the operator to flip the card.
4. Capture one full set of images for the back.
5. Save all images and metadata into a structured card folder.
6. Force all lights off after capture or on any failure.

This follows the File 2 approach: one command controls L/R/U/D lighting, USB-tethered Sony capture, and metadata generation.

---

## Recommended Shopping List

| Category | Item | Qty | Est. Unit £ | Est. Total £ | Suggested Vendor |
|---|---|---:|---:|---:|---|
| Camera | Sony A7 IV body, used | 1 | 1,250 | 1,250 | MPB UK |
| Lens | Sony FE 90mm f/2.8 Macro G OSS, used | 1 | 550 | 550 | MPB UK, Wex Photo Video |
| Copy Stand | Kaiser RS 2 XA copy stand | 1 | 300 | 300 | Photospecialist UK |
| Lighting | SmallRig RM75 / similar compact LED video light | 4 | 60 | 240 | Amazon UK, Wex, SmallRig UK |
| Polarisation | Linear polarising film sheets for lights | 1 set | 40 | 40 | Edmund Optics UK, Amazon UK |
| Polarisation | CPL filter for lens | 1 | 20 | 20 | Amazon UK, Wex Photo Video |
| Control | ESP32 DevKit | 1 | 10 | 10 | The Pi Hut, Amazon UK |
| Control | 4-channel MOSFET board or relay module | 1 | 12 | 12 | The Pi Hut, Amazon UK |
| Control | Wiring, connectors, terminal blocks | 1 set | 15 | 15 | RS Components, Amazon UK |
| Control | Project enclosure | 1 | 10 | 10 | RS Components, Amazon UK |
| Trigger | USB foot pedal | 1 | 20 | 20 | Amazon UK |
| USB | USB-C tether cable | 1 | 25 | 25 | Wex Photo Video, Tether Tools, Amazon UK |
| USB | Powered USB hub | 1 | 20 | 20 | Amazon UK, Scan UK |
| Fixture | ESD mat | 1 | 30 | 30 | RS Components, Amazon UK |
| Fixture | Card jig, 3D printed or custom | 1 | 35 | 35 | Local 3D print service |
| Mounting | Magic arms / clamps for lights | 4 | 10 | 40 | Amazon UK |
| Misc | Cable management, labels, fasteners | 1 set | 50 | 50 | Amazon UK, RS Components |

## Estimated Total

| Build Type | Estimated Cost |
|---|---:|
| Used camera + used lens | ~£2,657 |
| New camera + new lens | ~£3,300–£3,500 |

The Mini PC has been removed because an existing PC will be used.

---

## Why Four Small LED Lights Instead of Four Large Panels?

Four Godox LP600Bi panels are probably oversized for macro trading-card capture.

For a card-sized object, the main requirement is not raw light output. The important requirements are:

- Four consistent directional lights: Left, Right, Up, Down
- Stable repeatable geometry
- Independent electronic control
- Polarising film mounted over each light
- Reliable timing for automated capture

Small fixed LED lights are easier to mount, easier to polarise, and better suited to a compact grading lane.

---

## Polarisation Lighting Setup

Each LED light should have linear polarising film mounted over the front.

The camera lens should use a circular polarising filter.

The lens CPL is rotated until glare and surface reflections are reduced. This creates a cross-polarised lighting setup.

### Layout

```text
             Up Light
                |
                |
Left Light ---- Card ---- Right Light
                |
                |
            Down Light
```

Each light is fixed in position and controlled independently.

---

## Automation Compatibility

This setup is compatible with the File 2 automation approach.

The ESP32 or Arduino controls each light channel using a simple serial protocol:

```text
ON L
OFF L
ON R
OFF R
ON U
OFF U
ON D
OFF D
ALL OFF
```

The PC runs the orchestration script.

The script talks to:

- The ESP32/Arduino over USB serial
- The Sony A7 IV over USB tether
- The local file system for saving images and metadata

---

## One-Click Capture Workflow

The operator should only need to press the foot pedal once per side, or once per full card if the system includes a flip prompt.

Recommended first implementation:

1. Operator scans or enters card/job ID.
2. Operator places card front-side-up in jig.
3. Operator presses foot pedal.
4. System captures front images under L/R/U/D lighting.
5. System prompts: `Flip card`.
6. Operator flips card.
7. Operator presses foot pedal again.
8. System captures back images under L/R/U/D lighting.
9. System saves metadata and confirms completion.

This gives full controlled capture with very low operator complexity.

---

## Required Images Per Card

For professional grading evidence, the minimum automated capture set should be:

| Side | Image | Purpose |
|---|---|---|
| Front | `front_L.jpg` | Left directional surface evidence |
| Front | `front_R.jpg` | Right directional surface evidence |
| Front | `front_U.jpg` | Up directional surface evidence |
| Front | `front_D.jpg` | Down directional surface evidence |
| Back | `back_L.jpg` | Left directional surface evidence |
| Back | `back_R.jpg` | Right directional surface evidence |
| Back | `back_U.jpg` | Up directional surface evidence |
| Back | `back_D.jpg` | Down directional surface evidence |
| Metadata | `capture_meta.json` | Traceability and audit record |

Optional additions for a more complete grading archive:

| Image | Purpose |
|---|---|
| `front_even.jpg` | Normal reference image with even lighting |
| `back_even.jpg` | Normal reference image with even lighting |
| `front_raw.arw` | Archival raw capture, if required |
| `back_raw.arw` | Archival raw capture, if required |

---

## Recommended Folder Structure

```text
data/
  job_0001/
    card_0001/
      front_L.jpg
      front_R.jpg
      front_U.jpg
      front_D.jpg
      back_L.jpg
      back_R.jpg
      back_U.jpg
      back_D.jpg
      capture_meta.json
```

---

## Metadata Requirements

Each capture should record:

- Job ID
- Card ID
- Lane ID
- Operator ID, if available
- Camera serial number, if available
- Lens used
- Side: front/back
- Light direction: L/R/U/D
- Timestamp
- File hash
- Camera settings
- Light settle time
- Capture software version

This is important for traceability and dispute handling.

---

## Automation Logic

The capture script should follow this structure:

```text
on_capture_trigger(card_id):
    for side in [front, back]:
        prompt_operator_if_needed(side)
        for light in [L, R, U, D]:
            all_lights_off()
            light_on(light)
            wait(settle_ms)
            capture_image(side, light)
            light_off(light)
            save_image_and_metadata(side, light)
        if side == front:
            prompt_operator("Flip card")
    all_lights_off()
    mark_card_complete()
```

Failure rules:

- If capture fails, turn all lights off.
- If the ESP32 does not respond, abort capture.
- If the Sony capture CLI fails, abort capture.
- Never silently retry without logging.
- Never overwrite an existing card folder without confirmation.

---

## Recommended Camera Settings to Start

These should be locked and not adjusted by the operator.

| Setting | Starting Value |
|---|---|
| Mode | Manual |
| Aperture | f/8 to f/11 |
| ISO | 100 |
| Shutter | Start around 1/30 to 1/4 sec, adjust after testing |
| White Balance | Fixed/manual |
| Focus | Manual focus |
| File Type | JPEG for workflow, RAW optional for archive |
| Stabilisation | Off if mounted rigidly |

Because the card is stationary, longer exposures are acceptable. The priority is consistency, sharpness, and low noise.

---

## Recommended Build Decision

Use the ready-made SmallRig-style LED light approach for V1.

It is simpler than building custom LED bars, while still supporting:

- Independent L/R/U/D automation
- Cross-polarisation
- One-click capture
- Repeatable photometric-style surface imaging
- Upgrade path to custom LED bars later

---

## Final Notes

This shopping list is intended for one capture lane.

For multiple lanes, standardise every component, especially:

- Camera model
- Lens model
- Light model
- ESP32 firmware
- Folder structure
- Capture metadata schema
- Camera settings

That will make calibration, operator training, debugging, and scaling much easier.
