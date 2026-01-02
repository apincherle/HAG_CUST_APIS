# Cross‑Polarized Multi‑Light Capture (4 Lights)  
## ESP32 → MOSFET → LED Panels • USB‑Tethered Sony • One‑Click Capture

This doc includes:

- **Wiring diagrams** (ESP32 → MOSFET → LEDs)
- **Ready‑to‑run Python control code** (ESP32 serial + capture orchestration)
- **Calibration + QA checklist** (repeatability, exposure, polarization, alignment)

> **Assumptions:** You have **4 LED panels** (e.g., Godox LP600BI), a **Sony A7 IV**, and you want deterministic light switching via **ESP32**.

---

## 1) Hardware: What you need to add

### 1.1 ESP32 + Switching
- 1× ESP32 dev board (USB‑C or micro‑USB)
- 4× **logic‑level N‑channel MOSFET modules** (preferred)  
  *or* discrete MOSFETs + resistors + terminal blocks
- Wires, terminal blocks, heatshrink
- **Common ground** between ESP32 and MOSFET/source ground

#### Note about switching studio LED panels
Some studio panels aren’t designed for rapid power‑cycling. Two approaches:

**A) Switch each panel’s DC power** (simplest wiring; test reliability)  
**B) Best long‑term:** use lights with **DMX** or an external LED emitter designed for strobing

If you switch panel power, use longer settle time (200–500 ms) and verify stability.

---

## 2) Wiring diagrams

### 2.1 Single channel (one light) — low‑side MOSFET switch

```
ESP32 GPIOx ──[100Ω]── Gate (G)     N‑MOSFET     Drain (D) ───── LED(−)
                       |           ┌─────────┐
ESP32 GND ─────────────┴───────────┤ MOSFET  │
                                   └─────────┘
                                      Source (S) ─────────────── PSU GND (−)

PSU +V  ──────────────────────────────────────────────────────── LED(+)
PSU GND ──────────────────────────────────────────────────────── Source (S) / Common GND
```

**Recommended extras**
- Gate pulldown: **10kΩ** Gate → Source (keeps OFF at boot)
- Flyback diode: only for inductive loads (not usually needed for LEDs)

---

### 2.2 Four channel overview

```
Mini PC (USB) ────────────────┐
                              │
                              ▼
                           ┌───────┐
                           │ ESP32  │
                           │GND USB │
                           └─┬─┬─┬─┬┘
                             │ │ │ │
                           GPIO18/19/21/23
                             │ │ │ │
                      ┌──────▼┐│┌──▼─────┐
                      │MOSFET1 │││ MOSFET2│
                      └──┬─────┘│└──┬─────┘
                         │       │   │
                      LED1(−)  LED2(−)  ... (repeat for 3 & 4)
                         │       │
PSU GND (−) ─────────────┴───────┴─────────────── Common GND ─── ESP32 GND

PSU +V  ──────────────────────── LED1(+)   (repeat for each light)
PSU +V  ──────────────────────── LED2(+)
```

**Key rule:** ESP32 GND must be tied to the same ground as the MOSFET source and LED PSU ground.

---

### 2.3 Suggested ESP32 pin map

| Light | Meaning | ESP32 GPIO |
|---|---|---:|
| L | Left | 18 |
| R | Right | 19 |
| U | Up/Top | 21 |
| D | Down/Bottom | 23 |

Avoid boot strap pins unless you know their constraints.

---

## 3) ESP32 firmware (Arduino) — serial protocol

Commands:
- `ON L`, `OFF L`
- `ON R`, `OFF R`
- `ON U`, `OFF U`
- `ON D`, `OFF D`
- `ALL OFF`
- `PING`

### 3.1 Firmware (`esp32_lights.ino`)
```cpp
#include <Arduino.h>

static const int PIN_L = 18;
static const int PIN_R = 19;
static const int PIN_U = 21;
static const int PIN_D = 23;

void allOff() {
  digitalWrite(PIN_L, LOW);
  digitalWrite(PIN_R, LOW);
  digitalWrite(PIN_U, LOW);
  digitalWrite(PIN_D, LOW);
}

int pinFor(char ch) {
  switch (ch) {
    case 'L': return PIN_L;
    case 'R': return PIN_R;
    case 'U': return PIN_U;
    case 'D': return PIN_D;
    default: return -1;
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(PIN_L, OUTPUT);
  pinMode(PIN_R, OUTPUT);
  pinMode(PIN_U, OUTPUT);
  pinMode(PIN_D, OUTPUT);
  allOff();
  Serial.println("READY");
}

void loop() {
  if (!Serial.available()) return;

  String line = Serial.readStringUntil('\n');
  line.trim();
  line.toUpperCase();

  if (line == "PING") { Serial.println("PONG"); return; }
  if (line == "ALL OFF") { allOff(); Serial.println("OK"); return; }

  bool turnOn = line.startsWith("ON ");
  bool turnOff = line.startsWith("OFF ");
  if (!turnOn && !turnOff) { Serial.println("ERR"); return; }

  char ch = line.charAt(turnOn ? 3 : 4);
  int pin = pinFor(ch);
  if (pin < 0) { Serial.println("ERR"); return; }

  digitalWrite(pin, turnOn ? HIGH : LOW);
  Serial.println("OK");
}
```

---

## 4) Ready‑to‑run Python control code (one‑click)

This script:
- connects to ESP32 over serial
- sequences lights **L → R → U → D**
- triggers camera capture via a **pluggable command template**
- saves output into a new `card_###` folder

### 4.1 Install
```bash
pip install pyserial
```

### 4.2 Script (`capture_one_click.py`)
```python
import time
import json
import argparse
import subprocess
from pathlib import Path

import serial

LIGHTS = ["L", "R", "U", "D"]

def send_cmd(ser: serial.Serial, cmd: str, timeout_s: float = 1.0) -> str:
    ser.reset_input_buffer()
    ser.write((cmd.strip() + "\n").encode("utf-8"))
    ser.flush()

    t0 = time.time()
    while time.time() - t0 < timeout_s:
        line = ser.readline().decode("utf-8", errors="ignore").strip()
        if line:
            return line
    raise TimeoutError(f"No response for command: {cmd}")

def ensure_ok(resp: str, cmd: str) -> None:
    if resp not in ("OK", "PONG", "READY"):
        raise RuntimeError(f"ESP32 bad response for {cmd!r}: {resp!r}")

def all_off(ser: serial.Serial) -> None:
    ensure_ok(send_cmd(ser, "ALL OFF"), "ALL OFF")

def light_on(ser: serial.Serial, light: str) -> None:
    ensure_ok(send_cmd(ser, f"ON {light}"), f"ON {light}")

def light_off(ser: serial.Serial, light: str) -> None:
    ensure_ok(send_cmd(ser, f"OFF {light}"), f"OFF {light}")

def capture_image(output_path: Path, capture_cmd: str) -> None:
    """Capture one frame and save to output_path.

    capture_cmd is a command template containing {out}.
    Example:
      --capture-cmd "my_sony_capture_tool --output {out}"
    """
    cmd = capture_cmd.format(out=str(output_path))
    completed = subprocess.run(cmd, shell=True)
    if completed.returncode != 0:
        raise RuntimeError(f"Camera capture failed (rc={completed.returncode}): {cmd}")

def next_card_folder(base: Path) -> Path:
    existing = [p for p in base.glob("card_*") if p.is_dir()]
    nums = []
    for p in existing:
        try:
            nums.append(int(p.name.split("_")[1]))
        except Exception:
            pass
    n = (max(nums) + 1) if nums else 1
    return base / f"card_{n:03d}"

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--port", required=True, help="ESP32 serial port (e.g. COM4 or /dev/ttyUSB0)")
    ap.add_argument("--baud", type=int, default=115200)
    ap.add_argument("--outdir", default="data", help="Base output directory")
    ap.add_argument("--card-id", default=None, help="Optional card id (e.g. card_001). If omitted, auto-increment.")
    ap.add_argument("--settle-ms", type=int, default=80, help="Light settle time before capture")
    ap.add_argument("--capture-cmd", required=True, help="Camera capture command template containing {out}")
    args = ap.parse_args()

    base = Path(args.outdir)
    base.mkdir(parents=True, exist_ok=True)

    card_folder = (base / args.card_id) if args.card_id else next_card_folder(base)
    card_folder.mkdir(parents=True, exist_ok=True)

    meta = {
        "card_folder": str(card_folder),
        "lights": LIGHTS,
        "settle_ms": args.settle_ms,
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "capture_cmd": args.capture_cmd,
    }

    with serial.Serial(args.port, args.baud, timeout=0.5) as ser:
        time.sleep(0.8)  # allow board to boot after opening port

        # Optional ping
        try:
            ensure_ok(send_cmd(ser, "PING", timeout_s=1.5), "PING")
        except Exception:
            pass

        try:
            all_off(ser)

            for light in LIGHTS:
                light_on(ser, light)
                time.sleep(args.settle_ms / 1000.0)

                out_path = card_folder / f"{light}.jpg"
                capture_image(out_path, args.capture_cmd)

                light_off(ser, light)
                time.sleep(0.03)

            all_off(ser)

        except Exception:
            # Safety: ensure lights off on any failure
            try:
                all_off(ser)
            except Exception:
                pass
            raise

    (card_folder / "capture_meta.json").write_text(json.dumps(meta, indent=2), encoding="utf-8")
    print(f"Done: {card_folder}")

if __name__ == "__main__":
    main()
```

### 4.3 Run examples

**Windows example (replace with your real tether capture tool/command):**
```bash
python capture_one_click.py --port COM4 --capture-cmd "YOUR_SONY_CAPTURE_TOOL --output {out}"
```

**If your capture tool saves to a folder instead of a filename**, modify the command to support `{out}` or change `capture_image()` to move/rename the latest file.

---

## 5) Calibration + QA checklist

### 5.1 Before every session (fast)
- [ ] Copy stand locked; camera mount tight; no wobble
- [ ] Camera: **M mode**, ISO/shutter/aperture fixed
- [ ] Focus fixed (tape the focus ring if needed)
- [ ] White balance fixed (Kelvin or custom)
- [ ] Lens + CPL clean; no fingerprints
- [ ] Card jig clean; no dust/fibers

### 5.2 Polarization verification (critical for foil/holo)
- [ ] Turn on **one** light
- [ ] Rotate **lens CPL** until glare drops sharply (minimum specular)
- [ ] Mark CPL position (tape/marker)
- [ ] Repeat quickly for other lights: glare suppression should be consistent

### 5.3 Light geometry & intensity matching
- [ ] All 4 lights same distance to card plane (±5 mm)
- [ ] Similar incidence angles aimed at card center
- [ ] Dimmer settings recorded and fixed
- [ ] Test capture: mean brightness across L/R/U/D is similar (within ~5–10%)

### 5.4 Exposure & clipping checks (per direction)
Capture a test set and verify:
- [ ] Highlights not clipped (no large blown regions)
- [ ] Shadows not crushed (texture visible)
- [ ] Histograms are broadly similar across directions

### 5.5 Alignment & stability checks
- [ ] Capture **two sets** back-to-back without touching rig
- [ ] Compare: edges align; no drift
- [ ] If drift: tighten mounts, reduce cable pull, increase settle-ms

### 5.6 Batch QA (during production)
- [ ] Every N cards (e.g., 25), capture a **reference card**
- [ ] Track stats per light:
  - mean intensity
  - std dev
  - % saturated pixels
- [ ] If drift appears: re-check CPL position, panel dimmer, camera settings

### 5.7 File integrity
- [ ] Each card folder contains exactly: `L.jpg, R.jpg, U.jpg, D.jpg`
- [ ] Metadata saved: `capture_meta.json`
- [ ] No overwrites; card ids increment correctly

---

## 6) Starting timing values
- Light settle time: **80 ms** (increase to 150–250 ms if panels ramp)
- Between channels: **30 ms**

If you are power‑cycling panels: start at **300–500 ms settle** and test.

---

## 7) Practical notes for shiny / holographic cards
- Cross‑polarization is your main tool for reliable captures.
- Avoid coherent light sources (lasers) during this capture stage (speckle/diffraction).
- Use black flags/hoods to prevent stray light entering the lens.

---

## 8) Optional next improvements
- Add a neutral gray patch in frame for normalization
- Add a blur check (variance of Laplacian) after each shot
- Add a small GUI button (Tkinter) or foot‑pedal hotkey wrapper
- Add automated card masking/cropping for downstream processing
