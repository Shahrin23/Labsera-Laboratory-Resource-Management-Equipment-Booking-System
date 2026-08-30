package com.labresa.model;

public class Equipment extends Resource {

    private String category;
    public Equipment(int id, String name, double cost, int maintenanceThreshold, String category) {
        super(id, name, cost, maintenanceThreshold);
        this.category = category;
    }

    public String getCategory() {
        return category; }

    @Override
    public String getType() {
        return "EQUIPMENT";
    }
}
