package com.labresa.patterns.state;

import com.labresa.model.Resource;

/**
 * RETIRED as of Round 4, kept for reference rather than deleted.
 *
 * Previously represented "this single resource instance is reserved."
 * Once bookings became quantity-based (a resource can have many units, with
 * some checked out and others still free at the same time), a single global
 * RESERVED state for the whole resource no longer made sense - it would
 * incorrectly block every unit just because one was booked. Individual
 * booking lifecycle now lives entirely on Reservation.status instead.
 * This class is never assigned to a Resource in the current flow.
 */
public class ReservedState implements ResourceState {

    @Override
    public void markUnderMaintenance(Resource resource) {
        resource.setState(new UnderMaintenanceState());
    }

    @Override
    public void markAvailable(Resource resource) {
        resource.setState(new AvailableState());
    }

    @Override
    public String getName() {
        return "RESERVED";
    }
}
