package com.labresa.model;

import java.time.LocalDateTime;

public class UsageLog {

    private int id;
    private final int reservationId;
    private final LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String conditionNotes;

    public UsageLog(int id, int reservationId, LocalDateTime checkIn) {
        this.id = id;
        this.reservationId = reservationId;
        this.checkIn = checkIn;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReservationId() { return reservationId; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }
    public String getConditionNotes() { return conditionNotes; }
    public void setConditionNotes(String conditionNotes) { this.conditionNotes = conditionNotes; }

    @Override
    public String toString() {
        return String.format("UsageLog[id=%d, reservation=%d, in=%s, out=%s]",
                id, reservationId, checkIn, checkOut);
    }
}
