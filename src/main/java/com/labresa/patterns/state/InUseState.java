package com.labresa.patterns.state;


import com.labresa.model.Resource;

public class InUseState implements ResourceState {

    @Override
    public void reserve(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot reserve resource '" + resource.getName() + "': it is currently in use.");
    }

    @Override
    public void checkIn(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot check in resource '" + resource.getName() + "': it is already checked in.");
    }

    @Override
    public void checkOut(Resource resource) {
        resource.incrementUsageCounter();
        resource.setState(new AvailableState());
    }

    @Override
    public void markUnderMaintenance(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot start maintenance on '" + resource.getName() + "': it is currently in use. Check it out first.");
    }

    @Override
    public void markAvailable(Resource resource) {
        throw new IllegalResourceTransitionException(
                "Cannot mark '" + resource.getName() + "' available while it is in use.");
    }

    @Override
    public String getName() {
        return "IN_USE";
    }
}
