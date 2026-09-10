package com.labresa.model;

public class LabRoom extends Resource {

    private final int capacity;

    public LabRoom(int id, String name, Category category, int totalQuantity, int maintenanceThreshold, int capacity) {
        super(id, name, category, totalQuantity, maintenanceThreshold);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String getType() {
        return "LAB_ROOM";
    }
}
