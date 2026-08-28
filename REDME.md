# LabResa — Laboratory Resource & Equipment Booking System

---

## Overview

LabResa is a JavaFX desktop application for managing the booking of shared laboratory equipment and resources (microscopes, 3D printers, testing kits, lab rooms) within a university department.

The core problem it solves: multiple students and researchers compete for a limited pool of equipment, and higher-cost or higher-risk equipment requires supervisor sign-off before it can be used. A simple booking form isn't enough — the system needs to:

- Prevent double-booking the same resource for overlapping time slots
- Route approval differently depending on the resource's cost/risk level (cheap equipment auto-approves; expensive/restricted equipment needs a Technician, then a Faculty Supervisor, to sign off)
- Track a resource's lifecycle so it can be temporarily blocked from booking while under maintenance
- Automatically flag a resource for maintenance once it's been used enough times

**Stakeholders:**
- **Student/Researcher** — requests reservations, checks equipment in/out
- **Lab Technician** — first-level approver, manages resources and maintenance
- **Faculty Supervisor** — second-level approver for high-cost/restricted resources
- **Maintenance Staff** — services equipment and clears it back to available

This combination of conflict-checking, conditional multi-level approval, and a state-driven equipment lifecycle is what makes LabResa suitable for demonstrating meaningful design patterns rather than being a CRUD-only application.

---

## Workflows

### Workflow A — Reservation & Approval

```
User requests slot for Resource
   → Conflict check against existing reservations
        conflict found  → Rejected, alternative slots suggested
        no conflict     → [PENDING]
   → Resource type check
        low-cost/common      → Auto-approved → [CONFIRMED]
        high-cost/restricted → Approval chain: Technician → Faculty Supervisor
              chain approves  → [CONFIRMED]
              chain rejects   → [REJECTED]
   → On usage day: user checks in → [IN_USE]
   → User checks out → UsageLog created → [COMPLETED]
```

### Workflow B — Maintenance Lifecycle

```
Resource usage_counter increments on each COMPLETED reservation
   → counter crosses threshold OR technician flags damage
        → Resource → [UNDER_MAINTENANCE]
        → blocks new reservations
        → Observer notifies users with pending bookings on that resource
   → Technician completes service
        → Resource → [AVAILABLE]
        → usage_counter reset
        → Observer notifies affected users

```

### Tech Stack

```

| Layer / Concern | Technology |
|---|---|
| UI framework | JavaFX (`javafx-controls`, `javafx-fxml`) |
| Build & dependency management | Maven |
| Database | SQLite (`org.xerial:sqlite-jdbc`) |
| Connection pooling | HikariCP (`com.zaxxer:HikariCP`) |
| Password hashing | jBCrypt (`org.mindrot:jbcrypt`) |
| PDF report generation | Apache PDFBox (`org.apache.pdfbox:pdfbox`) |
| Charts (utilization, peak hours) | JFreeChart-FX (`org.jfree:jfreechart-fx`) |
| Extra UI controls (date/time pickers) | ControlsFX (`org.controlsfx:controlsfx`) |
| JSON config / import-export | Jackson Databind (`com.fasterxml.jackson.core:jackson-databind`) |
| QR check-in/check-out (optional) | ZXing (`com.google.zxing:core`, `zxing:javase`) |
| Unit testing | JUnit 5 (`org.junit.jupiter:junit-jupiter`), Mockito (`org.mockito:mockito-core`) |
| Logging | SLF4J + Logback (`org.slf4j:slf4j-api`, `ch.qos.logback:logback-classic`) |

```

###Architecture

```

3-layer desktop architecture — JavaFX Controllers (Presentation) → Services (Application/business logic, where design patterns live) → DAOs (Persistence, SQLite via JDBC).
```
