package com.labresa.patterns.approval;

import com.labresa.model.Reservation;
import com.labresa.model.Resource;

public class TechnicianApprovalHandler extends ApprovalHandler {

    private final double autoApproveCostThreshold;

    public TechnicianApprovalHandler(double autoApproveCostThreshold) {
        this.autoApproveCostThreshold = autoApproveCostThreshold;
    }

    @Override
    protected ApprovalResult process(Reservation reservation, Resource resource) {
        if (resource.getCost() <= autoApproveCostThreshold) {
            return ApprovalResult.approved("Technician (auto-approved, low-cost)");
        }
        return null;
    }
}
