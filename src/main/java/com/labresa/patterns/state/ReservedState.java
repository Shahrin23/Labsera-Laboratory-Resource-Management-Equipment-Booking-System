package com.labresa.patterns.state;

import com.labresa.model.Resource;

public class ReservedState implements ResourceState {

    @Override
    public void reserve(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot reserve resource '" + resource.getName() + "': it is already reserved.");
    }

    @Override
    public void checkIn(Resource resource) {
        resource.setState(new InUseState());
    }

    @Override
    public void checkOut(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot check out resource '" + resource.getName() + "': it hasn't been checked in yet.");
    }

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

