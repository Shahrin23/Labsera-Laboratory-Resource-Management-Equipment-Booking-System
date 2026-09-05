package com.labresa.patterns.approval;

public class ApprovalResult {

    public enum Decision { APPROVED, REJECTED, ESCALATED }

    private final Decision decision;
    private final String approvedBy;
    private final String reason;

    private ApprovalResult(Decision decision, String approvedBy, String reason) {
        this.decision = decision;
        this.approvedBy = approvedBy;
        this.reason = reason;
    }

    public static ApprovalResult approved(String approvedBy) {
        return new ApprovalResult(Decision.APPROVED, approvedBy, null);
    }

    public static ApprovalResult rejected(String reason) {
        return new ApprovalResult(Decision.REJECTED, null, reason);
    }

    public Decision getDecision() { return decision; }
    public String getApprovedBy() { return approvedBy; }
    public String getReason() { return reason; }
    public boolean isApproved() { return decision == Decision.APPROVED; }

    @Override
    public String toString() {
        return decision == Decision.APPROVED
                ? "APPROVED by " + approvedBy
                : "REJECTED: " + reason;
    }
}

