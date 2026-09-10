package com.labresa.patterns.state;

import com.labresa.model.Resource;

public class UnderMaintenanceState implements ResourceState {

    @Override
    public void markUnderMaintenance(Resource resource) {
        // Already under maintenance - idempotent no-op.
    }

    @Override
    public void markAvailable(Resource resource) {
        resource.setState(new AvailableState());
        resource.resetUsageCounter();
    }

    @Override
    public String getName() {
        return "UNDER_MAINTENANCE";
    }
}
