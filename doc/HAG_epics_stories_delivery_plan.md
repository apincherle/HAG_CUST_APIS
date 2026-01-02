# Delivery Plan — Epics, Features & Stories
## Card Grading Platform (Azure, Event‑Driven)

This document breaks the system into **Epics → Features → User Stories → Key Tasks**.
It is intended for **Jira / Azure DevOps / Linear** import or roadmap planning.

---

## Program Phases (Recommended)

1. Foundation & Infrastructure  
2. Submissions & Intake  
3. Imaging & Evidence  
4. AI Grading & Policy  
5. Human Review & QA  
6. Slabbing, QR & Verification  
7. Shipping & Customer Access  
8. Reliability, Security & Scale  

---

# EPIC 1 — Platform Foundation & Infrastructure

**Goal:** Secure, observable Azure backbone with event‑driven orchestration.

## Feature 1.1 — Core Azure Infrastructure
**Story:** As a platform, I need core Azure services provisioned so all systems can communicate securely.

**Tasks**
- Provision Azure SQL (prod/staging)
- Provision Blob Storage containers (raw, jpg, evidence)
- Provision Service Bus topic + subscriptions
- Provision Azure Key Vault
- Configure Managed Identities
- Enable App Insights + Log Analytics

---

## Feature 1.2 — Event Backbone & Standards
**Story:** As a developer, I want a standard event envelope so services integrate consistently.

**Tasks**
- Define canonical event envelope
- Create `outbox_events` table
- Create `event_consumption` idempotency table
- Implement outbox publisher worker
- Base consumer SDK/helpers

---

# EPIC 2 — Submission & Intake System

**Goal:** Convert customer orders into traceable internal card records.

## Feature 2.1 — Submission APIs
**Story:** As a customer, I want to create a submission and track its status.

**Tasks**
- POST /submissions API
- Write customers, submissions, submission_items
- Emit `submission.created`
- Status lifecycle handling

---

## Feature 2.2 — Physical Intake & Barcoding
**Story:** As an operator, I want to register each card with a unique intake barcode.

**Tasks**
- Intake UI
- Barcode generation & printing
- Create cards records
- Emit `card.intake_registered`

---

# EPIC 3 — Imaging & Capture (Tier C)

**Goal:** Deterministic, automated capture with zero manual file handling.

## Feature 3.1 — Edge Capture Agent
**Story:** As an operator, I want the lane to capture and upload images automatically.

**Tasks**
- Camera tether control
- Multi‑light sequencing
- Upload to Blob Storage
- Insert captures rows
- Emit `capture.created`

---

## Feature 3.2 — Capture QC
**Story:** As the system, I must detect unusable images early.

**Tasks**
- Blur detection
- Exposure checks
- QC flags storage
- Emit `capture.qc_failed`

---

## Feature 3.3 — Capture Set Aggregation
**Story:** As the grading system, I need to know when a card is fully imaged.

**Tasks**
- Capture Aggregator Function
- Tier‑based capture requirements
- Emit `card.capture_set_completed`

---

# EPIC 4 — AI Grading & Evidence

**Goal:** Produce explainable subscores and defect evidence.

## Feature 4.1 — Grading Orchestrator
**Story:** As the system, I want grading to start automatically once imaging completes.

**Tasks**
- Create grading_runs
- Emit `grading.started`
- Fan‑out to scoring modules

---

## Feature 4.2 — Scoring Modules
**Story:** As the system, I want independent grading modules per category.

**Tasks**
- Centering scorer
- Corners scorer
- Edges scorer
- Surface (diffuse + multi‑light)
- Write subscores, defects, evidence_assets
- Emit `grading.module_completed`

---

## Feature 4.3 — Grade Aggregation
**Story:** As the system, I want a recommended grade and confidence.

**Tasks**
- Aggregate subscores
- Apply grading policy version
- Update grading_runs
- Emit `grading.completed`

---

# EPIC 5 — Human Review & QA

**Goal:** Human judgment only when needed, with full auditability.

## Feature 5.1 — Review Routing
**Story:** As the system, I want to auto‑approve high‑confidence grades.

**Tasks**
- Confidence thresholds
- Boundary detection
- Emit `review.required` when needed

---

## Feature 5.2 — Review UI
**Story:** As a grader, I want to review evidence and approve or override grades.

**Tasks**
- Evidence viewer
- Override controls
- Reason capture
- Emit `review.completed`

---

## Feature 5.3 — Final Grade & Audit
**Story:** As the system, I must create an official grade with history.

**Tasks**
- Upsert final_grades
- Insert grade_audits
- Emit `grade.finalized`

---

# EPIC 6 — Slabbing, QR & Verification

**Goal:** Physical encapsulation cryptographically linked to digital truth.

## Feature 6.1 — Slab Orchestration
**Story:** As the system, I want to prepare cards for encapsulation.

**Tasks**
- Generate slab serials
- Create slabs records
- Emit `slab.requested`

---

## Feature 6.2 — QR Signing
**Story:** As the system, I want a tamper‑proof QR code.

**Tasks**
- Build canonical payload
- Sign with Ed25519 via Key Vault
- Store signature
- Emit `slab.qr_signed`

---

## Feature 6.3 — Public Verification
**Story:** As a collector, I want to verify a card by scanning its QR.

**Tasks**
- Verify API endpoint
- Signature validation
- Read‑only grade response

---

# EPIC 7 — Shipping & Customer Visibility

**Goal:** Close the loop with transparency.

## Feature 7.1 — Shipping Workflow
**Story:** As ops, I want to ship graded cards.

**Tasks**
- Shipment creation
- Tracking numbers
- Emit `shipment.created`

---

## Feature 7.2 — Submission Closeout
**Story:** As the system, I want to close submissions cleanly.

**Tasks**
- Update submission status
- Emit `submission.closed`

---

# EPIC 8 — Reliability, Security & Scale

**Goal:** Survive real‑world failures.

## Feature 8.1 — Idempotency
**Story:** As the system, I must handle duplicate events safely.

**Tasks**
- event_consumption table
- Consumer guards

---

## Feature 8.2 — Failure Handling
**Story:** As ops, I need visibility into failures.

**Tasks**
- DLQ monitoring
- Incident logging
- Alerting

---

## Feature 8.3 — Security Hardening
**Story:** As the platform, I must protect data and keys.

**Tasks**
- RBAC enforcement
- Managed identities everywhere
- Key rotation procedures

---

## Suggested Team Split

- Team A — Platform & Events  
- Team B — Imaging & Edge  
- Team C — AI & Review  
- Team D — Slabbing & Verification  
- Team E — Customer, Ops & Shipping  

---
