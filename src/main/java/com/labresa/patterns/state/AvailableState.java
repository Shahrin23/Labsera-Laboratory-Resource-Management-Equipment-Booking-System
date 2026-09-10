package com.labresa.patterns.state;

import com.labresa.model.Resource;

public class AvailableState implements ResourceState {

    @Override
    public void markUnderMaintenance(Resource resource) {
        resource.setState(new UnderMaintenanceState());
    }

    @Override
    public void markAvailable(Resource resource) {
        // Already available - idempotent no-op rather than an error, since a
        // Technician clicking "Make Available" twice shouldn't be treated as a bug.
    }

    @Override
    public String getName() {
        return "AVAILABLE";
    }
}
