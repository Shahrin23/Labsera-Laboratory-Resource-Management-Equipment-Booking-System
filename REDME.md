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

Workflows
Workflow A — Reservation & Approval
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


Workflow B — Maintenance Lifecycle
Resource usage_counter increments on each COMPLETED reservation
   → counter crosses threshold OR technician flags damage
        → Resource → [UNDER_MAINTENANCE]
        → blocks new reservations
        → Observer notifies users with pending bookings on that resource
   → Technician completes service
        → Resource → [AVAILABLE]
        → usage_counter reset

Both workflows involve state transitions with restricted legal actions per state, branching driven by runtime data (resource cost, usage counter vs. threshold), and cross-module interaction — which is what qualifies them as non-trivial workflows rather than simple CRUD operations.