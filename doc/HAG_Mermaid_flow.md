---
config:
  layout: dagre
---
flowchart LR
  %% Customer Lane
  subgraph S1["Customer"]
    Customer["Customer"]
    Ops["Intake / Ops Staff"]
    Grader["Human Grader"]
  end

  %% API / App Layer
  subgraph S2["API Layer"]
    API["API / App Service"]
    VerifyAPI["Public Verify API"]
    ReviewUI["Review UI"]
  end

  %% Imaging & Scoring & Orchestration
  subgraph S3["Processing & Orchestration"]
    Lane["Imaging Lane<br>(Camera + Lights + Edge PC)"]
    CaptureAgg["Capture Aggregator<br>Function"]
    GradeOrch["Grading Orchestrator"]
    ReviewRouter["Review Router"]
    SlabOrch["Slabbing Orchestrator"]
    Shipping["Shipping Service"]
    AI["AI Scoring Modules<br>(AKS / Azure ML)"]
  end

  %% Infra / Storage
  subgraph S4["Infra & Storage"]
    SQL["Azure SQL<br>System of Record"]
    SB["Service Bus<br>grading.events"]
    Blob["Azure Blob Storage<br>Images & Evidence"]
    KV["Azure Key Vault<br>QR Signing Keys"]
  end

  %% Connections
  Customer --> API
  Customer --> VerifyAPI

  Ops --> API
  Ops --> Lane

  API --> SQL
  API --> SB

  Lane --> Blob
  Lane --> SQL
  Lane --> SB

  SB --> CaptureAgg
  SB --> GradeOrch
  SB --> ReviewRouter
  SB --> SlabOrch
  SB --> Shipping

  CaptureAgg --> SQL
  CaptureAgg --> SB

  GradeOrch --> SQL
  GradeOrch --> AI
  GradeOrch --> SB

  AI --> Blob
  AI --> SQL
  AI --> SB

  ReviewRouter --> ReviewUI
  ReviewUI --> SQL
  ReviewUI --> SB

  Grader --> ReviewUI

  SlabOrch --> SQL
  SlabOrch --> KV
  SlabOrch --> SB

  VerifyAPI --> SQL
  VerifyAPI --> KV

  Shipping --> SQL
  Shipping --> SB