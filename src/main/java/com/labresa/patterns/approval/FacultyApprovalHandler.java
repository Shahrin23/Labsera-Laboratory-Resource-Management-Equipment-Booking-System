package com.labresa.patterns.approval;

import com.labresa.model.Reservation;
import com.labresa.model.Resource;

public class FacultyApprovalHandler extends ApprovalHandler {

    private final double absoluteMaxCost;

    public FacultyApprovalHandler(double absoluteMaxCost) {
        this.absoluteMaxCost = absoluteMaxCost;
    }

    @Override
    protected ApprovalResult process(Reservation reservation, Resource resource) {
        if (resource.getCost() > absoluteMaxCost) {
            return ApprovalResult.rejected(
                    "Resource cost exceeds the maximum a faculty supervisor can approve; escalate manually.");
        }
        return ApprovalResult.approved("Faculty Supervisor");
    }
}
