# Cross-Polarized Structured Light Capture System  
## Option: 4 Lights + ESP32 Control + USB Tethered Camera (One-Click Capture)

This document describes a **complete, buildable system** for capturing **cross‑polarized, multi‑directional images** of trading cards using:

- **4 LED light sources**
- **ESP32 microcontroller for deterministic light control**
- **USB‑tethered camera capture**
- **Single‑click / single‑trigger operation**

Designed for **shiny, foil, and holographic cards**, with no contact and no consumables.

---

## 1. System Goal

From one user action (button / foot pedal / UI click):

1. Lights fire **one at a time** (L / R / U / D)
2. Camera captures an image for each light
3. Images are saved with deterministic naming
4. Output is ready for photometric / surface processing

---

## 2. Hardware Overview

### 2.1 Camera & Optics
- **Camera:** Sony A7 IV  
- **Lens:** 90mm Macro  
- **Connection:** USB tether to PC / Mini‑PC  
- **Mode:** Manual exposure, manual focus, fixed WB  
- **Lens filter:** Circular Polarizer (CPL)

---

### 2.2 Lighting (Cross‑Polarized)

| Item | Qty | Notes |
|----|----:|-----|
| Godox LP600BI LED panels | 4 | Continuous, dimmable |
| Linear polarizing film | 4 | Mounted in front of each panel |
| Mounting arms / clamps | 4 | Fixed, repeatable angles |

**Lighting geometry**
- Lights placed at ~45° incidence
- Even spacing around camera
- Same distance from card plane

**Polarization rule**
- All LED polarizers aligned **same direction**
- Camera CPL rotated **90° relative to lights**

This suppresses glare and holographic specular reflection.

---

### 2.3 Light Control (ESP32)

**Controller**
- ESP32 Dev Board (USB powered)

**Switching**
- 4 × logic‑level MOSFETs (preferred)  
  *or* solid‑state relays (SSR)

**Why ESP32**
- Fast (<1 ms) switching
- Deterministic timing
- USB serial control
- No Wi‑Fi latency

**Basic wiring**
```
ESP32 GPIO → MOSFET gate
MOSFET drain → LED negative
LED positive → power supply
Common ground shared
```

---

### 2.4 Computer & IO
- Mini PC (Intel N100 class)
- USB hub (powered)
- USB tether cable (camera)
- USB cable (ESP32)
- Optional USB foot pedal (mapped to trigger script)

---

## 3. Software Stack

### 3.1 Components
- Python 3.10+
- Sony Imaging Edge / Sony Remote SDK
- pySerial (ESP32 control)
- OS: Windows or Linux

---

### 3.2 Folder Structure

```
project/
  capture.py
  data/
    card_001/
      L.jpg
      R.jpg
      U.jpg
      D.jpg
```

---

## 4. One‑Click Capture Sequence

### 4.1 Logical Flow

```
START
│
├─ Create new card folder
│
├─ Light L ON
│   └─ wait 50 ms
│   └─ capture image → L.jpg
│   └─ Light L OFF
│
├─ Light R ON
│   └─ capture → R.jpg
│   └─ Light R OFF
│
├─ Light U ON
│   └─ capture → U.jpg
│   └─ Light U OFF
│
├─ Light D ON
│   └─ capture → D.jpg
│   └─ Light D OFF
│
└─ END
```

Total time: ~1–2 seconds per card.

---

## 5. ESP32 Firmware (Concept)

ESP32 listens for simple serial commands:

```
ON L
OFF L
ON R
OFF R
ON U
OFF U
ON D
OFF D
```

Each command toggles one GPIO.

---

## 6. Python Control Script (Concept)

```python
import serial, time

ser = serial.Serial("COM4", 115200)

lights = ["L", "R", "U", "D"]

for light in lights:
    ser.write(f"ON {light}\n".encode())
    time.sleep(0.05)

    camera.capture(f"{light}.jpg")

    ser.write(f"OFF {light}\n".encode())
    time.sleep(0.05)
```

Camera capture is handled via Sony SDK or Imaging Edge tether API.

---

## 7. Triggering (One‑Click)

You can trigger `capture.py` using:
- Desktop button
- Keyboard shortcut
- USB foot pedal (HID → hotkey)
- Simple GUI button

The entire sequence runs automatically.

---

## 8. Output Quality & Use

This system produces:
- Glare‑suppressed images
- Consistent directional lighting
- High repeatability
- Ideal input for:
  - Photometric stereo
  - Scratch detection
  - Pressure / dent analysis
  - ML grading models

---

## 9. Why This Works for Holographic Cards

- No coherent light (no speckle)
- Polarization removes specular glare
- Multiple angles reveal real surface structure
- No contact, no gel, no risk

This is **the most reliable practical method** for collectible cards.

---

## 10. Expansion (Optional Later)

- Structured light projection
- Automated focus stacking
- ML‑based grading
- Region‑weighted scoring
- Laser triangulation for non‑foil areas

---

## 11. Summary

✔ 4 cross‑polarized LED lights  
✔ ESP32 deterministic control  
✔ USB‑tethered Sony camera  
✔ One‑click capture  
✔ Safe for foil & holographic cards  

This is a **production‑grade capture architecture** without industrial vendor lock‑in.
