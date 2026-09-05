package com.labresa.model;

import java.time.LocalDateTime;

public class Reservation {

    public enum Status { PENDING, CONFIRMED, REJECTED, CANCELLED, COMPLETED }

    private int id;
    private final int resourceId;
    private final int userId;
    private final User.Role requesterRole;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private Status status;
    private final boolean recurring;
    private final String notes;
    private final String priorityTag;


  public Reservation(int id, int resourceId, int userId, User.Role requesterRole,
                LocalDateTime startTime, LocalDateTime endTime, Status status,
                boolean recurring, String notes, String priorityTag) {
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
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResourceId() { return resourceId; }
    public int getUserId() { return userId; }
    public User.Role getRequesterRole() { return requesterRole; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isRecurring() { return recurring; }
    public String getNotes() { return notes; }
    public String getPriorityTag() { return priorityTag; }


    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    @Override
    public String toString() {
        return String.format("Reservation[id=%d, resource=%d, user=%d(%s), %s-%s, status=%s]",
                id, resourceId, userId, requesterRole, startTime, endTime, status);
    }
}
