# Photometric‑Style Surface Processing for Card Grading (Option 1: Python + OpenCV)

This guide shows a **complete, practical pipeline** for grading collectible/trading cards using **4 directional images** captured from the **same side** with **fixed camera geometry**, using **photometric‑style surface cues** (scratches, dents, pressure marks).

> **Goal:** Convert 4 lighting-direction images → normalized response → surface/contrast cues → defect maps → a numeric **grade score** + optional visual overlays.

---

## Table of contents
1. [What you need](#what-you-need)  
2. [Capture setup](#capture-setup)  
3. [Folder structure](#folder-structure)  
4. [Environment setup](#environment-setup)  
5. [End-to-end pipeline overview](#end-to-end-pipeline-overview)  
6. [Step-by-step implementation](#step-by-step-implementation)  
7. [Defect detection recipes](#defect-detection-recipes)  
8. [Scoring & grading](#scoring--grading)  
9. [Calibration & quality checks](#calibration--quality-checks)  
10. [Batch processing](#batch-processing)  
11. [Common issues & fixes](#common-issues--fixes)  
12. [Next upgrades](#next-upgrades)

---

## What you need

### Hardware
- Camera (DSLR, mirrorless, or machine vision camera) with **manual exposure**
- Stable mount (tripod / copy stand)
- **4 directional lights** (or one light moved to 4 known directions)
- Diffusers (optional but helps reduce hotspots)
- A flat, non-reflective background
- Card holder / jig to keep the card in the same position

### Software
- Python 3.10+  
- OpenCV  
- NumPy  
- scikit-image (optional, but helpful)  
- PyTorch (optional, for ML later)

---

## Capture setup

### Lighting directions
Pick 4 directions around the camera axis, for example:
- `L`: light from left
- `R`: light from right
- `U`: light from top
- `D`: light from bottom

Keep:
- **Camera fixed**
- **Card fixed**
- **Exposure fixed**
- **White balance fixed** (manual WB recommended)
- Use **RAW** if possible, convert to 16-bit TIFF or PNG after

### Naming convention
Use consistent filenames:
- `card_001_L.png`
- `card_001_R.png`
- `card_001_U.png`
- `card_001_D.png`

---

## Folder structure

```
project/
  data/
    card_001/
      L.png
      R.png
      U.png
      D.png
  output/
    card_001/
      aligned/
      maps/
      overlays/
      metrics.json
  src/
    grade.py
```

---

## Environment setup

### Install dependencies
```bash
python -m venv .venv
# mac/linux
source .venv/bin/activate
# windows
# .venv\Scripts\activate

pip install opencv-python numpy scikit-image
```

If you want fast math or ML later:
```bash
pip install scipy torch torchvision
```

---

## End-to-end pipeline overview

1. **Load** 4 directional images  
2. **Convert** to linear grayscale (or use luminance)  
3. **Register/align** (small shifts happen even in rigid setups)  
4. **Mask** the card area (crop/segment)  
5. **Normalize brightness** across images (flat-field / histogram / percentile scaling)  
6. Build **photometric cues**:
   - gradient / contrast maps
   - “specular-reduced” maps (optional)
   - pseudo-normal cues (approx)
7. **Detect defects**:
   - scratches: thin, high-frequency lines
   - dents: smooth concave/convex patches
   - pressure marks: broad low-frequency variation
8. Compute **metrics** + combine into a **score**
9. Generate **overlays** for human review
10. Batch across many cards

---

## Step-by-step implementation

Below is a single-file script outline (`src/grade.py`) you can adapt.

### 1) Load images and convert to grayscale
- Use grayscale to simplify, or keep RGB if needed for color-specific grading.
- Keep as **float** for math.

```python
import cv2
import numpy as np

def load_gray(path: str) -> np.ndarray:
    img = cv2.imread(path, cv2.IMREAD_UNCHANGED)
    if img is None:
        raise FileNotFoundError(path)

    # Convert to float32
    img = img.astype(np.float32)

    # If color, convert to grayscale luminance
    if img.ndim == 3:
        img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Normalize to 0..1 if 8-bit/16-bit
    maxv = np.max(img)
    if maxv > 1.0:
        img = img / maxv

    return img
```

### 2) Align the images (registration)
Even tiny shifts will break pixel-wise comparisons.

Use **ECC alignment** (OpenCV) for small translations/affine:
```python
def align_to(reference: np.ndarray, moving: np.ndarray) -> np.ndarray:
    ref8 = (reference * 255).astype(np.uint8)
    mov8 = (moving * 255).astype(np.uint8)

    warp = np.eye(2, 3, dtype=np.float32)  # affine
    criteria = (cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 100, 1e-6)

    try:
        cc, warp = cv2.findTransformECC(ref8, mov8, warp, cv2.MOTION_AFFINE, criteria)
        aligned = cv2.warpAffine(
            moving, warp, (reference.shape[1], reference.shape[0]),
            flags=cv2.INTER_LINEAR + cv2.WARP_INVERSE_MAP,
            borderMode=cv2.BORDER_REPLICATE
        )
        return aligned
    except cv2.error:
        # Fallback: return original if alignment fails
        return moving
```

### 3) Segment or mask the card region
Simplest approach: crop by known ROI.
Better approach: detect the card contour.

```python
def find_card_mask(img: np.ndarray) -> np.ndarray:
    # Basic threshold + largest contour; adapt to your background
    im8 = (img * 255).astype(np.uint8)
    blur = cv2.GaussianBlur(im8, (9, 9), 0)
    _, th = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # Ensure card is white in mask
    if np.mean(th) < 127:
        th = cv2.bitwise_not(th)

    cnts, _ = cv2.findContours(th, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    mask = np.zeros_like(im8, dtype=np.uint8)

    if not cnts:
        return mask

    c = max(cnts, key=cv2.contourArea)
    cv2.drawContours(mask, [c], -1, 255, thickness=-1)

    # Slight erosion to avoid edges
    mask = cv2.erode(mask, np.ones((5,5), np.uint8), iterations=1)
    return (mask > 0).astype(np.uint8)
```

### 4) Normalize brightness across directions
Directional lights differ in intensity. Normalize per-image using robust statistics inside the mask.

```python
def normalize(img: np.ndarray, mask: np.ndarray) -> np.ndarray:
    pixels = img[mask > 0]
    lo, hi = np.percentile(pixels, [5, 95])
    if hi - lo < 1e-6:
        return img.copy()
    out = (img - lo) / (hi - lo)
    return np.clip(out, 0, 1)
```

---

## Photometric cues (without full physical calibration)

With 4 directional lights, you can compute useful maps even without perfect light vectors.

### A) Directional difference maps
These are extremely effective for scratches/dents:
- Horizontal cue: `R - L`
- Vertical cue: `D - U`
- Aggregate slope magnitude: `sqrt(dx^2 + dy^2)`

```python
def directional_maps(L, R, U, D, mask):
    dx = (R - L) * mask
    dy = (D - U) * mask
    mag = np.sqrt(dx*dx + dy*dy)
    return dx, dy, mag
```

### B) High-frequency “scratchness” map
Scratches often appear as **thin bright/dark lines**.

```python
def highfreq(img: np.ndarray, mask: np.ndarray) -> np.ndarray:
    blur = cv2.GaussianBlur(img, (0,0), 5)
    hp = (img - blur) * mask
    hp = np.abs(hp)
    return hp
```

---

## Defect detection recipes

### 1) Scratches
Best signals:
- High frequency response
- Thin elongated edges
- Strong in one or more directional maps

```python
def detect_scratches(mag: np.ndarray, hp: np.ndarray, mask: np.ndarray):
    combined = (0.6 * mag + 0.4 * hp) * mask

    vals = combined[mask > 0]
    t = np.percentile(vals, 99.0)  # tune
    binary = (combined > t).astype(np.uint8)

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3,3))
    binary = cv2.morphologyEx(binary, cv2.MORPH_CLOSE, kernel, iterations=1)
    return combined, binary
```

Metrics:
- scratch_area_ratio = scratch_pixels / card_pixels
- scratch_count (connected components)
- mean scratch intensity

---

### 2) Dents / pressure marks
These tend to be lower-frequency, smoother features.

```python
def detect_dents(mag: np.ndarray, mask: np.ndarray):
    sm = cv2.GaussianBlur(mag, (0,0), 7) * mask

    vals = sm[mask > 0]
    t = np.percentile(vals, 98.5)  # tune
    binary = (sm > t).astype(np.uint8)

    binary = cv2.morphologyEx(
        binary, cv2.MORPH_OPEN,
        cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5,5)),
        iterations=1
    )
    return sm, binary
```

---

## Scoring & grading

Example scoring:
- Start with 10.0
- Subtract weighted penalties

```python
def score_from_metrics(metrics: dict) -> float:
    score = 10.0
    score -= 40.0 * metrics["scratch_area_ratio"]
    score -= 25.0 * metrics["dent_area_ratio"]
    score -= 0.2  * metrics["scratch_components"]
    return float(np.clip(score, 0, 10))
```

**Important:** tune weights using a labeled dataset of graded cards.

---

## Calibration & quality checks

- Lock exposure + white balance
- Check blur (variance of Laplacian)
- Check clipping (too many pixels at 0 or 1)
- Use a reference target/card for consistency

---

## Batch processing

Suggested outputs per card:
- `maps/mag.png`
- `maps/highfreq.png`
- `maps/scratch_binary.png`
- `maps/dent_binary.png`
- `overlays/defects_overlay.png`
- `metrics.json`

---

## Common issues & fixes

### Hotspots / specular glare
- Use diffusion
- Increase distance from lights
- Cross-polarization (very effective)
- Ignore top 0.5% highlights when normalizing

### Card edges dominate
- Erode mask inward
- Or ignore an outer border band (20–40 px)

---

## Next upgrades

1. True photometric stereo with measured light vectors  
2. Cross-polarized capture  
3. ML scoring (regression/classification on defect maps)  
4. Region-weighted grading (corners/borders/center)

---

If you share one example set of 4 images, I can suggest **better thresholds and a more robust scoring model** for your exact surface + lighting.
