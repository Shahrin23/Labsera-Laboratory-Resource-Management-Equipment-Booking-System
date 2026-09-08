package com.labresa.model;

import java.time.LocalDateTime;

/**
 * Persisted record of an approval-chain decision for a reservation.
 * One row is written each time the Chain of Responsibility (Technician -> Faculty)
 * reaches a final decision for a reservation.
 */
public class Approval {

    public enum Level { TECHNICIAN, FACULTY }
    public enum Decision { APPROVED, REJECTED, PENDING }

    private int id;
    private final int reservationId;
    private final Integer approverId; // nullable: null when auto-approved by the system
    private final Level level;
    private final Decision decision;
    private final String comments;
    private final LocalDateTime decidedAt;

    public Approval(int id, int reservationId, Integer approverId, Level level,
                     Decision decision, String comments, LocalDateTime decidedAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.approverId = approverId;
        this.level = level;
        this.decision = decision;
        this.comments = comments;
        this.decidedAt = decidedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReservationId() { return reservationId; }
    public Integer getApproverId() { return approverId; }
    public Level getLevel() { return level; }
    public Decision getDecision() { return decision; }
    public String getComments() { return comments; }
    public LocalDateTime getDecidedAt() { return decidedAt; }

    @Override
    public String toString() {
        return String.format("Approval[reservation=%d, level=%s, decision=%s, comments=%s]",
                reservationId, level, decision, comments);
    }
}