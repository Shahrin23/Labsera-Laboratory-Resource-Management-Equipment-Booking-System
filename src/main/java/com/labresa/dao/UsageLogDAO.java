package com.labresa.dao;

import java.time.LocalDateTime;

public interface UsageLogDAO {
    void insertCheckIn(int reservationId, LocalDateTime checkIn);
    void completeCheckOut(int reservationId, LocalDateTime checkOut, String conditionNotes);
}
