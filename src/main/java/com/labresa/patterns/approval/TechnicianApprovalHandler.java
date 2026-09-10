package com.labresa.patterns.approval;

import com.labresa.model.Reservation;
import com.labresa.model.Resource;

/**
 * Chain of Responsibility, link 1 (Round 4: repurposed from cost thresholds
 * to resource category, since cost was removed from the domain per the
 * client's requirement). COMMON resources are auto-approved here immediately.
 * SPECIAL resources are passed to the next handler (FacultyApprovalHandler),
 * which checks for the required formal letter and escalates to manual review.
 */
public class TechnicianApprovalHandler extends ApprovalHandler {

    @Override
    protected ApprovalResult process(Reservation reservation, Resource resource) {
        if (resource.getCategory() == Resource.Category.COMMON) {
            return ApprovalResult.approved("Technician (auto-approved, common resource)");
        }
        return null; // SPECIAL resources pass through to the next handler
    }
}
