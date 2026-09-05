package com.labresa.patterns;

import com.labresa.model.Reservation;
import com.labresa.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationBuilder {

    private int id;
    private final int resourceId;
    private final int userId;
    private final User.Role requesterRole;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private Reservation.Status status = Reservation.Status.PENDING;
    private boolean recurring = false;
    private String notes = "";
    private String priorityTag = "";

    public ReservationBuilder(int resourceId, int userId, User.Role requesterRole,
                              LocalDateTime startTime, LocalDateTime endTime) {
        this.resourceId = resourceId;
        this.userId = userId;
        this.requesterRole = Objects.requireNonNull(requesterRole);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }

    public ReservationBuilder id(int id) { this.id = id; return this; }
    public ReservationBuilder status(Reservation.Status status) { this.status = status; return this; }
    public ReservationBuilder recurring(boolean recurring) { this.recurring = recurring; return this; }
    public ReservationBuilder notes(String notes) { this.notes = notes; return this; }
    public ReservationBuilder priorityTag(String priorityTag) { this.priorityTag = priorityTag; return this; }

    public Reservation build() {
        return new Reservation(id, resourceId, userId, requesterRole, startTime, endTime,
                status, recurring, notes, priorityTag);
    }
}
