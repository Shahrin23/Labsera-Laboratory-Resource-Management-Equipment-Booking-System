package com.labresa.model;

import com.labresa.patterns.state.AvailableState;
import com.labresa.patterns.state.ResourceState;

public abstract class Resource {

    protected int id;
    protected String name;
    protected double cost;
    protected int usageCounter;
    protected int maintenanceThreshold;
    protected ResourceState state;

    protected Resource(int id, String name, double cost, int maintenanceThreshold) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.maintenanceThreshold = maintenanceThreshold;
        this.usageCounter = 0;
        this.state = new AvailableState();
    }


    public abstract String getType();


    public void reserve()             {
        state.reserve(this);
    }
    public void checkIn()             {
        state.checkIn(this);
    }
    public void checkOut()            {
        state.checkOut(this);
    }
    public void markUnderMaintenance(){
        state.markUnderMaintenance(this);
    }
    public void markAvailable()       {
        state.markAvailable(this);
    }

    public void setState(ResourceState state) {
        this.state = state;
    }
    public ResourceState getState() {
        return state;
    }
    public String getStatus() {
        return state.getName();
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

    public int getId() {
        return id;
    }
    public String getName() {
        return name; }
    public double getCost() {
        return cost;
    }
    public int getUsageCounter() {
        return usageCounter;
    }
    public int getMaintenanceThreshold() {
        return maintenanceThreshold;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, name=%s, cost=%.2f, status=%s, usage=%d/%d]",
                getType(), id, name, cost, getStatus(), usageCounter, maintenanceThreshold);
    }
}
