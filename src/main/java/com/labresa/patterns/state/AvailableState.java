package com.labresa.patterns.state;

import com.labresa.model.Resource;

public class AvailableState implements ResourceState {

    @Override
    public void reserve(Resource resource) {
        resource.setState(new ReservedState());
    }

    @Override
    public void checkIn(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot check in resource '" + resource.getName() + "': it has no active reservation.");
    }

    @Override
    public void checkOut(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot check out resource '" + resource.getName() + "': it is not currently in use.");
    }

    @Override
    public void markUnderMaintenance(Resource resource) {
        resource.setState(new UnderMaintenanceState());
    }

    @Override
    public void markAvailable(Resource resource) {

    }

    @Override
    public String getName() {
        return "AVAILABLE";
    }
}
