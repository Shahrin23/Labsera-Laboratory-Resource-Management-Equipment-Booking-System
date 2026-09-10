package com.labresa.patterns.approval;

/**
 * Factory Method: assembles the Chain of Responsibility. ApprovalService only
 * calls buildDefaultChain() - it never constructs TechnicianApprovalHandler or
 * FacultyApprovalHandler directly, so the chain's composition can change in
 * one place without touching ApprovalService.
 *
 * Round 4: the chain no longer decides on cost (removed from the domain per
 * the client's requirement). It now decides on resource Category: COMMON
 * resources auto-approve at the Technician link; SPECIAL resources require a
 * formal letter (checked at the Faculty link) and then escalate to a manual
 * human decision - see ApprovalResult.Decision.ESCALATED.
 */
public class ApprovalHandlerFactory {

    private ApprovalHandlerFactory() { }

    public static ApprovalHandler buildDefaultChain() {
        ApprovalHandler technician = new TechnicianApprovalHandler();
        ApprovalHandler faculty = new FacultyApprovalHandler();
        technician.setNext(faculty);
        return technician;
    }
}
