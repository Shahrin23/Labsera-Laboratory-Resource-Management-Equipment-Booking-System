package com.labresa.patterns.state;

import com.labresa.model.Resource;

public class UnderMaintenanceState implements ResourceState {

    @Override
    public void reserve(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot reserve resource '" + resource.getName() + "': it is under maintenance.");
    }

    @Override
    public void checkIn(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot check in resource '" + resource.getName() + "': it is under maintenance.");
    }

    @Override
    public void checkOut(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot check out resource '" + resource.getName() + "': it is under maintenance.");
    }

    @Override
    public void markUnderMaintenance(Resource resource) {
        // already under maintenance, no-op
    }

    @Override
    public void markAvailable(Resource resource) {
        resource.resetUsageCounter();
        resource.setState(new AvailableState());
    }

    @Override
    public String getName() {
        return "UNDER_MAINTENANCE";
    }
}

