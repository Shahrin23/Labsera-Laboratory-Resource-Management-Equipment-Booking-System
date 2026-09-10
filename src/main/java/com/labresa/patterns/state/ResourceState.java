package com.labresa.patterns.state;

import com.labresa.model.Resource;

/**
 * State pattern, simplified in Round 4: now models only the resource-level
 * maintenance toggle (Available <-> UnderMaintenance), which a Technician
 * controls manually (see new requirement). reserve()/checkIn()/checkOut()
 * were removed from this interface because they modeled a SINGLE physical
 * unit's lifecycle; the new quantity-based booking model (multiple units per
 * resource, tracked via Resource.availableQuantity) means individual booking
 * lifecycle now lives on Reservation.status instead, not on a single global
 * Resource state. See CHANGES_SUMMARY.md Round 4 for the full rationale.
 */
public interface ResourceState {

    /** Called when maintenance staff flag this resource as out of service. */
    void markUnderMaintenance(Resource resource);

    /** Called when maintenance staff complete servicing and restore availability. */
    void markAvailable(Resource resource);

    /** Human readable name, also used for persistence (status column). */
    String getName();
}
