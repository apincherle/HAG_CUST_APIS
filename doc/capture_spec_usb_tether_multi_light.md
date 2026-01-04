# Capture Spec: One-Command Multi-Light Set (L/R/U/D)
## USB Tethered Sony + Arduino/ESP32 Lights • Cross-Polarized Photometric Capture

This spec describes how to capture the required multi-directional images (**L/R/U/D**) with **one command**, using:
- **Sony A7 IV over USB tether** (recommended, deterministic)
- **Arduino/ESP32** controlling 4 lights via serial commands
- A simple orchestration script that produces:
  - `L.jpg`, `R.jpg`, `U.jpg`, `D.jpg` + `capture_meta.json`

> Note: `sonypy` is for Sony’s Wi‑Fi Camera Remote API, not USB tether. For USB tether, the recommended path is a small capture CLI built on **Sony Camera Remote SDK**, then called from Python.

---

## 1) System overview

### Goal
From one trigger (CLI command, UI button, or foot pedal), the system will:

1. Turn on **Light L**
2. Capture still image via USB tether → save `L.jpg`
3. Turn off Light L
4. Repeat for **R, U, D**
5. Save `capture_meta.json`

### Output folder structure
```
data/
  card_001/
    L.jpg
    R.jpg
    U.jpg
    D.jpg
    capture_meta.json
```

---

## 2) Hardware components

### 2.1 Camera
- Sony A7 IV (ILCE‑7M4)
- Lens: macro recommended
- USB tether to Mini PC (powered hub ok)

### 2.2 Lighting
- 4 directional lights (Left, Right, Up, Down)
- Cross-polarization recommended (polarizing film on lights + CPL on lens)

### 2.3 Light controller
- Arduino or ESP32 connected via USB serial
- 4 MOSFET channels (or SSR) driving the lights (or their power/control input)

---

## 3) Light control “API” (serial protocol)

Controller must accept newline-terminated commands and reply with `OK`:

Commands:
- `ON L` / `OFF L`
- `ON R` / `OFF R`
- `ON U` / `OFF U`
- `ON D` / `OFF D`
- `ALL OFF`
- `PING` (optional)

Example session:
```
PING        -> PONG
ALL OFF     -> OK
ON L        -> OK
OFF L       -> OK
...
```

---

## 4) Camera capture over USB tether

### Recommended capture engine
**Sony Camera Remote SDK** (Camera Remote Toolkit).  
Implement a tiny CLI (command-line tool) that captures one photo and saves it to a specific file path.

### Why a dedicated CLI
Sony’s USB tether control is exposed through their SDK (C++/C#), not a pure Python module.
So Python orchestrates the workflow and calls the CLI.

### Capture CLI contract
Your `sony_capture_cli` should support:

- `--out <filepath>` (required)
- optional:
  - `--camera <index|serial>`
  - `--timeout-ms <n>`
  - `--format jpg|arw` (if supported)
  - `--download yes|no`

Example:
```bash
sony_capture_cli --out "C:\captures\card_001\L.jpg"
```

The CLI must exit:
- `0` on success
- non-zero on failure (Python will abort and turn lights off)

---

## 5) Orchestration script (ready-to-run)

This Python script:
- connects to Arduino/ESP32 via serial
- cycles through lights L/R/U/D
- calls the USB capture CLI with the correct output filename
- writes capture metadata

### 5.1 Install
```bash
pip install pyserial
```

### 5.2 Script: `capture_photometric_usb.py`
```python
import argparse
import subprocess
import time
import json
from pathlib import Path

import serial

LIGHTS = ["L", "R", "U", "D"]

def send_cmd(ser: serial.Serial, cmd: str, timeout_s: float = 1.5) -> str:
    ser.reset_input_buffer()
    ser.write((cmd.strip() + "\n").encode("utf-8"))
    ser.flush()

    t0 = time.time()
    while time.time() - t0 < timeout_s:
        line = ser.readline().decode("utf-8", errors="ignore").strip()
        if line:
            return line
    raise TimeoutError(f"No response for command: {cmd!r}")

def ensure_ok(resp: str, cmd: str) -> None:
    if resp not in ("OK", "READY", "PONG"):
        raise RuntimeError(f"Controller response not OK for {cmd!r}: {resp!r}")

def all_off(ser: serial.Serial) -> None:
    ensure_ok(send_cmd(ser, "ALL OFF"), "ALL OFF")

def light_on(ser: serial.Serial, light: str) -> None:
    ensure_ok(send_cmd(ser, f"ON {light}"), f"ON {light}")

def light_off(ser: serial.Serial, light: str) -> None:
    ensure_ok(send_cmd(ser, f"OFF {light}"), f"OFF {light}")

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

def run_capture(capture_cmd_template: str, out_path: Path) -> None:
    cmd = capture_cmd_template.format(out=str(out_path))
    p = subprocess.run(cmd, shell=True)
    if p.returncode != 0:
        raise RuntimeError(f"Capture command failed (rc={p.returncode}): {cmd}")

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--port", required=True, help="Arduino/ESP32 serial port (COM4 or /dev/ttyUSB0)")
    ap.add_argument("--baud", type=int, default=115200)
    ap.add_argument("--outdir", default="data")
    ap.add_argument("--card-id", default=None, help="e.g., card_001 (optional). If omitted, auto-increment.")
    ap.add_argument("--settle-ms", type=int, default=120, help="Light settle time before triggering capture")
    ap.add_argument("--capture-cmd", required=True,
                    help='USB capture CLI template with {out}, e.g. "sony_capture_cli --out {out}"')
    ap.add_argument("--intershot-ms", type=int, default=80, help="Delay between shots")
    args = ap.parse_args()

    base = Path(args.outdir)
    base.mkdir(parents=True, exist_ok=True)
    card_folder = (base / args.card_id) if args.card_id else next_card_folder(base)
    card_folder.mkdir(parents=True, exist_ok=True)

    meta = {
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "lights": LIGHTS,
        "settle_ms": args.settle_ms,
        "intershot_ms": args.intershot_ms,
        "card_folder": str(card_folder),
        "capture_cmd": args.capture_cmd,
    }

    with serial.Serial(args.port, args.baud, timeout=0.5) as ser:
        time.sleep(0.8)  # allow MCU reboot on serial open
        try:
            # optional ping
            try:
                ensure_ok(send_cmd(ser, "PING"), "PING")
            except Exception:
                pass

            all_off(ser)

            for light in LIGHTS:
                light_on(ser, light)
                time.sleep(args.settle_ms / 1000.0)

                out_path = card_folder / f"{light}.jpg"
                run_capture(args.capture_cmd, out_path)

                light_off(ser, light)
                time.sleep(args.intershot_ms / 1000.0)

            all_off(ser)

        except Exception:
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

### 5.3 Run example
```bash
python capture_photometric_usb.py --port COM4 --outdir data --capture-cmd "sony_capture_cli --out {out}"
```

---

## 6) Timing guidance (starting values)
- `--settle-ms`: 120 ms (increase to 200–500 ms if your panels ramp up slowly)
- `--intershot-ms`: 80 ms

If you are power-cycling studio panels, start at 300–500 ms settle and test.

---

## 7) Error handling & safety requirements
- Lights must be forced **OFF** on any failure.
- Capture CLI must return non-zero on error so Python can abort.
- Save `capture_meta.json` for traceability.

---

## 8) Notes on using `sonypy`
If you want the same workflow over **Wi‑Fi Remote API**, `sonypy` is suitable and you can:
- `Discoverer().discover()`
- `cam.act_take_picture()`
- download returned image URLs

But for the **most deterministic** capture, USB + Sony SDK is recommended.

---

## 9) Next steps (optional)
If you tell me your OS (Windows/Linux) and your preferred language (C++ vs C#) for the capture CLI,
I can draft a **CLI skeleton** for `sony_capture_cli` matching this contract.
