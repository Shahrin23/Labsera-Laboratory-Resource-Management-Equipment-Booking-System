package com.labresa.model;

public class LabRoom extends Resource {

    private int capacity;

    public LabRoom(int id, String name, double cost, int maintenanceThreshold, int capacity) {
        super(id, name, cost, maintenanceThreshold);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity; }

    @Override
    public String getType() {

        return "LAB_ROOM";
    }
}
