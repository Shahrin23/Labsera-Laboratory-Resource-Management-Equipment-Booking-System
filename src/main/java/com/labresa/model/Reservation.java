package com.labresa.model;

import java.time.LocalDateTime;

public class Reservation {

    public enum Status { PENDING, CONFIRMED, REJECTED, CANCELLED, IN_USE, COMPLETED }

    private int id;
    private final int resourceId;
    private final int userId;
    private final User.Role requesterRole;
    private final LocalDateTime startTime;
    private LocalDateTime endTime; // mutable: updated to the actual checkout time if returned early
    private Status status;
    private final boolean recurring;
    private final String notes;
    private final String priorityTag;
    private final int quantity;

    // Formal-letter fields, required only for SPECIAL-category resources.
    private final String supervisorName;
    private final String supervisorRole; // e.g. "Supervisor", "Chairman", "Director"
    private final String letterReference; // free-text reference/description of the signed letter

    public Reservation(int id, int resourceId, int userId, User.Role requesterRole,
                        LocalDateTime startTime, LocalDateTime endTime, Status status,
                        boolean recurring, String notes, String priorityTag, int quantity,
                        String supervisorName, String supervisorRole, String letterReference) {
        this.id = id;
        this.resourceId = resourceId;
        this.userId = userId;
        this.requesterRole = requesterRole;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.recurring = recurring;
        this.notes = notes;
        this.priorityTag = priorityTag;
        this.quantity = quantity;
        this.supervisorName = supervisorName;
        this.supervisorRole = supervisorRole;
        this.letterReference = letterReference;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResourceId() { return resourceId; }
    public int getUserId() { return userId; }
    public User.Role getRequesterRole() { return requesterRole; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    /** Used when a reservation is checked out before its originally requested end time. */
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isRecurring() { return recurring; }
    public String getNotes() { return notes; }
    public String getPriorityTag() { return priorityTag; }
    public int getQuantity() { return quantity; }
    public String getSupervisorName() { return supervisorName; }
    public String getSupervisorRole() { return supervisorRole; }
    public String getLetterReference() { return letterReference; }

    public boolean hasFormalLetter() {
        return supervisorName != null && !supervisorName.isBlank()
                && supervisorRole != null && !supervisorRole.isBlank();
    }

    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    @Override
    public String toString() {
        return String.format("Reservation[id=%d, resource=%d, qty=%d, user=%d(%s), %s-%s, status=%s]",
                id, resourceId, quantity, userId, requesterRole, startTime, endTime, status);
    }
}
