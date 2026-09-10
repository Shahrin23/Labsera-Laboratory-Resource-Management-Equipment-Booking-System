package com.labresa.model;

import java.time.LocalDateTime;

/** One row per maintenance cycle a resource goes through. */
public class MaintenanceRecord {

    public enum Reason { THRESHOLD, DAMAGE_REPORTED }

    private int id;
    private final int resourceId;
    private final Integer technicianId; // nullable until a technician is assigned
    private final LocalDateTime startDate;
    private LocalDateTime endDate; // null while maintenance is ongoing
    private final Reason reason;
    private String notes;

    public MaintenanceRecord(int id, int resourceId, Integer technicianId, LocalDateTime startDate,
                              LocalDateTime endDate, Reason reason, String notes) {
        this.id = id;
        this.resourceId = resourceId;
        this.technicianId = technicianId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResourceId() { return resourceId; }
    public Integer getTechnicianId() { return technicianId; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Reason getReason() { return reason; }
    public String getNotes() { return notes; }

    public boolean isActive() { return endDate == null; }
}
