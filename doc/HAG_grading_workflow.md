# End-to-End Grading Workflow (Azure Stack)

This document describes the **full operational and grading workflow** for a card grading company using a **fully automated Azure-based architecture**.

It is written as a **standalone Markdown file** and includes **Mermaid diagrams** suitable for GitHub, GitLab, and most modern Markdown renderers.

---

## 1. High-level business workflow (customer-facing)

```mermaid
flowchart LR
  A[Customer creates order] --> B[Submission received]
  B --> C[Intake & ID assignment]
  C --> D[Imaging / Capture lane]
  D --> E[AI pre-grade + evidence]
  E --> F[Human review or auto-approve]
  F --> G[QA checks]
  G --> H[Slab + label + QR]
  H --> I[Shipping]
  I --> J[Submission closed]
```

---

## 2. Detailed operational workflow (facility level)

```mermaid
flowchart TD
  subgraph Intake
    A1[Receive package]
    A2[Verify contents]
    A3[Assign intake barcode]
    A4[Create card records]
    A1 --> A2 --> A3 --> A4
  end

  subgraph Imaging
    B1[Place card in jig]
    B2[Trigger capture]
    B3[Front: multi-light capture]
    B4[Flip card]
    B5[Back: multi-light capture]
    B6[Upload to Blob + SQL]
    B7[Emit capture.created event]
    B1 --> B2 --> B3 --> B4 --> B5 --> B6 --> B7
  end

  subgraph Grading
    C1[Create AI grading run]
    C2[Run scoring modules]
    C3[Centering]
    C4[Corners]
    C5[Edges]
    C6[Surface diffuse]
    C7[Surface multi-light]
    C8[Write subscores & defects]
    C1 --> C2
    C2 --> C3 --> C8
    C2 --> C4 --> C8
    C2 --> C5 --> C8
    C2 --> C6 --> C8
    C2 --> C7 --> C8
  end

  subgraph Review_and_QA
    D1{Confidence high?}
    D2[Auto-pass to QA]
    D3[Human review]
    D4{Approve?}
    D5[Adjust grade + reason]
    D6[Finalize grade + audit]
    D1 -->|Yes| D2 --> D6
    D1 -->|No| D3 --> D4
    D4 -->|Yes| D6
    D4 -->|No| D5 --> D6
  end

  subgraph Slab_and_Ship
    E1[Generate slab serial]
    E2[Generate QR + signature]
    E3[Print label]
    E4[Encapsulate card]
    E5[Ship submission]
    E1 --> E2 --> E3 --> E4 --> E5
  end

  Intake --> Imaging --> Grading --> Review_and_QA --> Slab_and_Ship
```

---

## 3. Event-driven workflow (Azure services)

```mermaid
sequenceDiagram
  participant Edge as Edge Agent
  participant Blob as Azure Blob Storage
  participant SQL as Azure SQL
  participant Bus as Event Grid / Service Bus
  participant Orch as Orchestrator
  participant AI as AI Services
  participant UI as Review UI
  participant KV as Key Vault
  participant Verify as Verify API

  Edge->>Blob: Upload RAW/JPG captures
  Edge->>SQL: Insert captures rows
  Edge->>Bus: Publish capture.created

  Orch->>SQL: Create grading_run (AI_PRE)
  Orch->>AI: Execute scoring modules
  AI->>Blob: Write evidence assets
  AI->>SQL: Write subscores & defects
  Orch->>SQL: Update grading_run results

  Orch->>UI: Route to review or auto-pass
  UI->>SQL: Approve / override grade
  SQL->>SQL: Insert grade_audit

  Orch->>KV: Sign QR payload
  Orch->>SQL: Create slab record
  Verify->>SQL: Validate QR + return grade
```

---

## 4. Card grading state machine

```mermaid
stateDiagram-v2
  [*] --> INTAKE
  INTAKE --> IMAGED
  IMAGED --> GRADED
  GRADED --> SLABBED
  SLABBED --> ARCHIVED
```

---

## 5. What data is written at each stage

| Stage | Azure SQL Tables | Blob Storage |
|---|---|---|
| Intake | customers, submissions, cards | — |
| Imaging | captures | grading-raw, grading-jpg |
| AI grading | grading_runs, subscores, defects | grading-evidence |
| Review | final_grades, grade_audits | optional evidence |
| Slabbing | slabs | exports |
| Verification | read-only | — |

---

## 6. Grading logic overview (AI + policy)

```mermaid
flowchart LR
  A[Captured images] --> B[Feature extraction]
  B --> C[Subscores]
  C --> D[Defects + evidence]
  D --> E[Policy engine]
  E --> F[Recommended grade]
  F --> G{Confidence threshold met?}
  G -->|Yes| H[Auto-approve]
  G -->|No| I[Human review]
```

**Important:**  
The **policy engine** determines the final grade.  
AI provides measurements and evidence — not authority.

---

## 7. Automation thresholds (starter defaults)

- Auto-approve if:
  - confidence ≥ 0.93
  - no HIGH severity defects
  - grade not within ±0.5 of a major boundary (e.g., 9 / 9.5 / 10)
- Otherwise route to human review

---

## 8. Design principle

> Treat grading like a regulated manufacturing process, not photography.

- Fixed geometry
- Deterministic lighting
- Versioned policies
- Immutable evidence
- Auditable decisions

---
