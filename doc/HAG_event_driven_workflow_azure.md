# Event-Driven Workflow Specification (Azure Services)

This document defines the **event-driven architecture, event catalog, and orchestration model**
for a card grading company built on a **full Azure stack**.

It is written as a **standalone Markdown file** suitable for direct inclusion in a Git repository.

---

## 1. Core principles

- **Azure SQL is the system of record**
- **Azure Service Bus Topics** carry domain events
- **Azure Blob Storage** stores images and evidence (referenced by SQL)
- **Azure Functions / Container Apps** act as event handlers
- **Azure Key Vault** holds signing keys
- **Every handler is idempotent**
- **Every state change is auditable**

---

## 2. Event envelope (standard for all events)

Every event published to Service Bus uses the same envelope:

```json
{
  "event_id": "01JXXXXXXXXXXXXX",
  "event_type": "capture.created",
  "event_version": 1,
  "occurred_at": "2025-12-23T12:34:56.789Z",
  "correlation_id": "submission_id or submission_number",
  "causation_id": "event_id that caused this event",
  "entity": {
    "type": "card | submission | grading_run | slab",
    "id": "GUID"
  },
  "data": {}
}
```

---

## 3. Azure Service Bus topology

### Topic
- `grading.events`

### Subscriptions
- `ingest`
- `imaging`
- `grading`
- `review`
- `slabbing`
- `shipping`
- `notifications`
- `analytics`

Subscriptions can optionally use filters on `event_type`, but most routing logic
is handled inside consumers after loading current state from SQL.

---

## 4. Event catalog (authoritative)

### A. Submission & Intake events

#### `submission.created`
**Producer:** API backend  
**Consumers:** ingest, notifications

```json
{
  "submission_id": "GUID",
  "submission_number": "SUB-2025-000123",
  "customer_id": "GUID",
  "service_level": "EXPRESS",
  "expected_item_count": 20
}
```

---

#### `submission.received`
**Producer:** Intake UI  
**Consumers:** ingest, notifications

```json
{
  "submission_id": "GUID",
  "received_at": "2025-12-23T09:05:00Z",
  "received_by": "user_123"
}
```

---

#### `card.intake_registered`
**Producer:** Intake station  
**Consumers:** imaging orchestration

```json
{
  "card_id": "GUID",
  "submission_id": "GUID",
  "intake_barcode": "BC-000001234",
  "line_number": 7
}
```

---

### B. Imaging & Capture events

#### `capture.created`
**Producer:** Edge Agent  
**Consumers:** capture aggregation, grading orchestration

```json
{
  "card_id": "GUID",
  "capture_id": "GUID",
  "side": "FRONT",
  "light_mode": "RAKE_LEFT",
  "blob_container": "grading-jpg",
  "blob_path": "SUB-2025-000123/{card_id}/captures/{capture_id}.jpg",
  "sha256": "64hex...",
  "lane_id": "lane_02"
}
```

---

#### `capture.qc_failed`
**Producer:** Edge Agent or QC Function  
**Consumers:** ops UI, notifications

```json
{
  "card_id": "GUID",
  "capture_id": "GUID",
  "qc_flags": ["BLUR", "UNDEREXPOSED"],
  "recommended_action": "RECAPTURE"
}
```

---

#### `card.capture_set_completed`
**Producer:** Capture Aggregator  
**Consumers:** grading orchestration

```json
{
  "card_id": "GUID",
  "required_set": "TIER_C_V1",
  "capture_ids": ["GUID", "GUID"],
  "qc_passed": true
}
```

---

### C. Grading events

#### `grading.requested`
**Producer:** Capture Aggregator  
**Consumers:** grading orchestrator

```json
{
  "card_id": "GUID",
  "policy_version": "policy_v1.2.0",
  "priority": "EXPRESS"
}
```

---

#### `grading.started`
**Producer:** Grading Orchestrator  
**Consumers:** observability

```json
{
  "grading_run_id": "GUID",
  "card_id": "GUID",
  "run_type": "AI_PRE",
  "model_version": "tcg-grade-model-0.9.3"
}
```

---

#### `grading.module_completed`
**Producer:** Scoring module worker  
**Consumers:** grading orchestrator

```json
{
  "grading_run_id": "GUID",
  "module": "SURFACE_MULTILIGHT",
  "status": "SUCCEEDED"
}
```

---

#### `grading.completed`
**Producer:** Grading Orchestrator  
**Consumers:** review router

```json
{
  "grading_run_id": "GUID",
  "card_id": "GUID",
  "overall_score": 9.4,
  "confidence": 0.92,
  "recommended_grade": 9.5
}
```

---

### D. Review & Finalization events

#### `review.required`
**Producer:** Review Router  
**Consumers:** Review UI

```json
{
  "card_id": "GUID",
  "grading_run_id": "GUID",
  "reason_codes": ["LOW_CONFIDENCE", "BOUNDARY_CASE"]
}
```

---

#### `review.completed`
**Producer:** Review UI  
**Consumers:** finalization handler

```json
{
  "card_id": "GUID",
  "grading_run_id": "GUID",
  "decision": "APPROVE | OVERRIDE",
  "final_grade": 9.5,
  "reason": "Surface scratch on front holo"
}
```

---

#### `grade.finalized`
**Producer:** Finalization handler  
**Consumers:** slabbing orchestration

```json
{
  "card_id": "GUID",
  "final_grade_id": "GUID",
  "slab_required": true
}
```

---

### E. Slabbing & QR events

#### `slab.requested`
**Producer:** Slabbing Orchestrator  
**Consumers:** label printing

```json
{
  "card_id": "GUID",
  "final_grade_id": "GUID"
}
```

---

#### `slab.qr_signed`
**Producer:** Slabbing Orchestrator  
**Consumers:** print pipeline

```json
{
  "slab_id": "GUID",
  "slab_serial": "GRA-25-ABCDEFG",
  "qr_payload_version": 1
}
```

---

#### `slab.printed`
**Producer:** Print station  
**Consumers:** encapsulation station

```json
{
  "slab_id": "GUID",
  "printed_at": "2025-12-23T14:10:00Z"
}
```

---

#### `slab.applied`
**Producer:** Encapsulation station  
**Consumers:** shipping workflow

```json
{
  "slab_id": "GUID",
  "card_id": "GUID",
  "status": "APPLIED"
}
```

---

### F. Shipping & Closeout

#### `shipment.created`
**Producer:** Shipping service

#### `submission.closed`
**Producer:** Ops workflow

---

## 5. Orchestration flow (plain English)

1. Edge Agent uploads images and emits `capture.created`
2. Capture Aggregator waits for full capture set and emits `card.capture_set_completed`
3. Grading Orchestrator creates grading run and fans out scoring modules
4. Modules emit `grading.module_completed`
5. Orchestrator emits `grading.completed`
6. Review Router decides auto-approve vs human review
7. Finalization writes grade and emits `grade.finalized`
8. Slabbing Orchestrator signs QR and emits `slab.qr_signed`
9. Physical stations emit `slab.printed` and `slab.applied`
10. Shipping emits `submission.closed`

---

## 6. Reliability patterns (mandatory)

### Idempotency
Each consumer records `(consumer_name, event_id)` in an `event_consumption` table.
Duplicate deliveries are safely ignored.

### Retries & DLQ
- Service Bus retry policy
- Dead-letter queue for poison messages
- Ops alert on DLQ entries

### Outbox pattern
Events triggered by SQL changes are written to an `outbox_events` table
inside the same transaction, then published asynchronously.

---

## 7. Design rule

> Events describe **facts that already happened**, never commands.

This guarantees replayability, auditability, and safe retries.

---
