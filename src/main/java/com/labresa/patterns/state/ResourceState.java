package com.labresa.patterns.state;

import com.labresa.model.Resource;

public interface  ResourceState {

    /** Called when a reservation is confirmed for this resource. */
    void reserve(Resource resource);

    /** Called when the user physically checks the resource in for use. */
    void checkIn(Resource resource);

    /** Called when the user finishes using the resource. */
    void checkOut(Resource resource);

    /** Called when the usage counter crosses the maintenance threshold. */
    void markUnderMaintenance(Resource resource);

    /** Called when maintenance staff complete servicing. */
    void markAvailable(Resource resource);

    /** Human readable name, also used for persistence (status column). */
    String getName();
}