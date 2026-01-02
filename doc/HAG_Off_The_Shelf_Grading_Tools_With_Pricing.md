
# Off-the-Shelf Grading & Vision Tools for HAG MVP (with Pricing)

This document lists **off-the-shelf tools you can immediately use** to build a
**POC / MVP Pokémon grading service**, including:
- What each tool **can do**
- What it **cannot do**
- **APIs available**
- **Pricing (monthly or usage-based where public)**

All tools are suitable for integration with a **Java Spring Boot backend** hosted on **Azure**.

---

## 1. Ximilar – AI Card Grading API (RECOMMENDED FOR MVP)

**Type:** SaaS API  
**Use:** Automated condition grading

### What it CAN do
- Accepts front + back card images
- Returns condition subgrades:
  - Centering
  - Corners
  - Edges
  - Surface
- Provides:
  - Final grade
  - Defect list (scratches, whitening, etc.)
  - Confidence score
- REST API (easy Spring Boot integration)

### What it CANNOT do
- Pokémon-specific authenticity checks
- UV / ink / trim verification
- Perfect PSA-equivalent grading

### API
- REST (JSON over HTTPS)
- Token-based auth

### Pricing (public)
- Free tier (testing)
- Paid (credit-based, approx):
  - €59 / month (~100k credits)
  - €175 / month (~300k credits)
  - €285 / month (~500k credits)
- Enterprise tiers scale higher

**Best for:** Replacing custom grading AI during MVP

---

## 2. Nyckel – Pretrained Condition Classifiers

**Type:** SaaS API  
**Use:** Simple condition / defect classification

### What it CAN do
- Classify images into conditions:
  - Mint
  - Corner wear
  - Edge wear
  - Damaged
- Very fast setup
- REST API

### What it CANNOT do
- Numeric grading (PSA-style)
- Full subgrades
- Authenticity checks

### API
- REST (JSON)

### Pricing
- Free / low-cost tiers
- Paid plans available (contact sales)

**Best for:** Backup signal or quick defect detection

---

## 3. Azure AI Vision (OCR & Image Analysis)

**Type:** Cloud API (Azure Cognitive Services)

### What it CAN do
- OCR:
  - Card name
  - Card number (e.g. 4/102)
- Text bounding boxes + confidence
- Image tagging & layout analysis

### What it CANNOT do
- Condition grading
- Authenticity verification

### API
- REST
- Azure SDKs for Java

### Pricing (Pay-as-you-go)
- ~ $1.00 – $1.50 per 1,000 OCR transactions
- Free tier available

**Best for:** Card identification (name, number)

---

## 4. Azure Custom Vision

**Type:** Cloud ML service

### What it CAN do
- Train custom image classifiers:
  - Corner whitening
  - Edge chipping
  - Surface scratches
- Deploy as prediction endpoint

### What it CANNOT do
- Prebuilt grading models
- Accurate results without labeled data

### API
- REST
- Azure SDKs

### Pricing (approx)
- Predictions: ~$2 per 1,000
- Training compute: ~$10 per hour
- Image storage costs apply

**Best for:** Replacing Ximilar later with your own models

---

## 5. OpenAI Vision (Optional)

**Type:** API (Vision + reasoning)

### What it CAN do
- Visual reasoning on card images
- Natural-language explanations of defects
- Cross-check AI grading outputs

### What it CANNOT do
- Deterministic grading
- High-volume low-cost grading

### API
- REST

### Pricing
- Usage-based (image + tokens)
- Not ideal for bulk grading

**Best for:** QA explanations, not primary grading

---

## 6. Pokémon TCG API

**Type:** Public API

### What it CAN do
- Resolve card metadata:
  - Name
  - Set
  - Number
  - Rarity
  - Variant

### What it CANNOT do
- Grading
- Authenticity verification

### API
- REST
- Free tier available

**Best for:** Canonical card identification

---

## 7. What to Use for a FAST MVP

### Recommended Stack
- Ximilar → grading
- Azure AI Vision → OCR
- Pokémon TCG API → card metadata
- Manual authenticity checks
- Human QA on all cards initially

### Skip for MVP
- Custom ML training
- UV / IR automation
- Full trim detection

---

## 8. MVP Monthly Cost (Rough Order of Magnitude)

| Tool | Approx Monthly Cost |
|-----|---------------------|
| Ximilar | €59 – €175 |
| Azure AI Vision OCR | $1–$10 (low volume) |
| Azure Custom Vision | $0–$50 |
| Pokémon TCG API | Free |
| Azure Hosting | $50–$150 |

---

## 9. Recommended MVP Positioning (Important)

Market as:
> **“AI-assisted grading with human verification”**

This avoids legal and trust issues while you collect real data.

---

© HAG Grading – Off-the-Shelf MVP Tooling Guide
