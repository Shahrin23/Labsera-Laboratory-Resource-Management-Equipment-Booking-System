package com.labresa.patterns.approval;

import com.labresa.model.Reservation;
import com.labresa.model.Resource;

public abstract class ApprovalHandler {

    protected ApprovalHandler next;

    public ApprovalHandler setNext(ApprovalHandler next) {
        this.next = next;
        return next;
    }

    public final ApprovalResult handle(Reservation reservation, Resource resource) {
        ApprovalResult result = process(reservation, resource);
        if (result != null) {
            return result;
        }
        if (next != null) {
            return next.handle(reservation, resource);
        }

        return ApprovalResult.rejected("No approver available for this resource/request combination.");
    }

    protected abstract ApprovalResult process(Reservation reservation, Resource resource);
}

