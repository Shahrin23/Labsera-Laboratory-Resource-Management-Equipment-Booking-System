package com.labresa.patterns.approval;

/**
 * Factory Method: assembles the Chain of Responsibility. ApprovalService only
 * calls buildDefaultChain() - it never constructs TechnicianApprovalHandler or
 * FacultyApprovalHandler directly, so the chain's composition (order, cost
 * thresholds) can change in one place without touching ApprovalService.
 */
public class ApprovalHandlerFactory {

    public static final double LOW_COST_AUTO_APPROVE_THRESHOLD = 500.0;
    public static final double FACULTY_MAX_APPROVABLE_COST = 5000.0;

    private ApprovalHandlerFactory() { }

    public static ApprovalHandler buildDefaultChain() {
        ApprovalHandler technician = new TechnicianApprovalHandler(LOW_COST_AUTO_APPROVE_THRESHOLD);
        ApprovalHandler faculty = new FacultyApprovalHandler(FACULTY_MAX_APPROVABLE_COST);
        technician.setNext(faculty);
        return technician;
    }
}