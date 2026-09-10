package com.labresa.patterns.state;

import com.labresa.model.Resource;

/**
 * RETIRED as of Round 4, kept for reference rather than deleted.
 * See ReservedState.java for the full explanation - a single global IN_USE
 * state for an entire resource no longer fits a quantity-based booking model
 * where several units can be in use while others remain free. This class is
 * never assigned to a Resource in the current flow.
 */
public class InUseState implements ResourceState {

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
        return "IN_USE";
    }
}
