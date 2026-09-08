package com.labresa.model;

import java.time.LocalDateTime;

public class MaintenanceRecord {

    private int id;
    private final int resourceId;
    private final LocalDateTime startDate;
    private LocalDateTime endDate;
    private String notes;

    public MaintenanceRecord(int id, int resourceId, LocalDateTime startDate, String notes) {
        this.id = id;
        this.resourceId = resourceId;
        this.startDate = startDate;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResourceId() { return resourceId; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isOpen() { return endDate == null; }

    @Override
    public String toString() {
        return String.format("MaintenanceRecord[id=%d, resource=%d, start=%s, end=%s]", id, resourceId, startDate, endDate);
    }
}
