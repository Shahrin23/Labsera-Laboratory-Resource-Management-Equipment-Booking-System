package com.labresa.model;

import com.labresa.patterns.state.AvailableState;
import com.labresa.patterns.state.ResourceState;

public abstract class Resource {

    /**
     * COMMON resources are bookable instantly whenever a unit is free and the
     * resource isn't under maintenance. SPECIAL resources always require a
     * human approval-authority (Faculty/Admin) to manually sign off, backed
     * by a formal letter from the applicant's supervisor/chairman/director -
     * see Reservation's formal-letter fields and ApprovalService.decide().
     */
    public enum Category { COMMON, SPECIAL }

    protected int id;
    protected String name;
    protected Category category;
    protected int totalQuantity;
    protected int availableQuantity;
    protected int usageCounter;
    protected int maintenanceThreshold;
    protected ResourceState state;

    protected Resource(int id, String name, Category category, int totalQuantity, int maintenanceThreshold) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.maintenanceThreshold = maintenanceThreshold;
        this.usageCounter = 0;
        this.state = new AvailableState();
    }

    public abstract String getType();

    // ---- Maintenance state (State pattern: Available <-> UnderMaintenance) ----
    // Per the new requirement, ONLY a Technician (or Admin) manually triggers these.
    public void markUnderMaintenance() { state.markUnderMaintenance(this); }
    public void markAvailable()        { state.markAvailable(this); }

    public void setState(ResourceState state) { this.state = state; }
    public ResourceState getState() { return state; }
    public String getStatus() { return state.getName(); }

    // ---- Quantity tracking: replaces the old single-instance Reserved/InUse states ----
    public boolean hasUnitsAvailable(int requestedQuantity) {
        return "AVAILABLE".equals(getStatus()) && availableQuantity >= requestedQuantity;
    }

    /** Holds units for a pending/confirmed reservation. Throws if not enough units are free. */
    public void holdUnits(int quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalStateException("Only " + availableQuantity + " unit(s) of '" + name + "' available, requested " + quantity);
        }
        availableQuantity -= quantity;
    }

    /** Releases units back to the pool (reservation cancelled/rejected/completed). */
    public void releaseUnits(int quantity) {
        availableQuantity = Math.min(totalQuantity, availableQuantity + quantity);
    }

    public void incrementUsageCounter() {
        usageCounter++;
        if (usageCounter >= maintenanceThreshold) {
            state.markUnderMaintenance(this);
        }
    }

    public void resetUsageCounter() {
        usageCounter = 0;
    }

    /**
     * Used only by DAOs when reconstructing a Resource from a database row.
     * Bypasses the normal State transition rules, since this is loading
     * already-persisted, already-valid data rather than performing a live action.
     */
    public void restore(ResourceState state, int usageCounter, int availableQuantity) {
        this.state = state;
        this.usageCounter = usageCounter;
        this.availableQuantity = availableQuantity;
    }

    public int getId() { return id; }

    /** Used by DAOs to assign the database-generated id after an INSERT. */
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getUsageCounter() { return usageCounter; }
    public int getMaintenanceThreshold() { return maintenanceThreshold; }

    @Override
    public String toString() {
        return String.format("%s[id=%d, name=%s, category=%s, status=%s, available=%d/%d, usage=%d/%d]",
                getType(), id, name, category, getStatus(), availableQuantity, totalQuantity, usageCounter, maintenanceThreshold);
    }
}
