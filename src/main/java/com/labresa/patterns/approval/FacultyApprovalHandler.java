package com.labresa.patterns.approval;

import com.labresa.model.Reservation;
import com.labresa.model.Resource;

/**
 * Chain of Responsibility, link 2 (Round 4: repurposed from cost thresholds).
 * Only reached for SPECIAL resources. Requires a formal letter (supervisor
 * name + role) to even be considered - without one, rejects immediately.
 * With one, this does NOT auto-approve: it escalates to a human
 * approval-authority (Faculty/Admin), who must explicitly decide via
 * ApprovalService.decide(). The applicant is only notified once that human
 * decision is made - never before.
 */
public class FacultyApprovalHandler extends ApprovalHandler {

    @Override
    protected ApprovalResult process(Reservation reservation, Resource resource) {
        if (!reservation.hasFormalLetter()) {
            return ApprovalResult.rejected(
                    "A formal letter signed by your supervisor, chairman, or director is required for special equipment requests.");
        }
        return ApprovalResult.escalated(
                "Formal letter received from " + reservation.getSupervisorName() +
                        " (" + reservation.getSupervisorRole() + ") - awaiting manual approval.");
    }
}
