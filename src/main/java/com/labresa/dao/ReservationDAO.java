package com.labresa.dao;

import com.labresa.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationDAO {
    Reservation findById(int id);
    List<Reservation> findAll();
    List<Reservation> findByUser(int userId);
    List<Reservation> findByStatus(Reservation.Status status);
    List<Reservation> findOverlapping(int resourceId, LocalDateTime start, LocalDateTime end);
    void save(Reservation reservation);
    void updateStatus(int reservationId, Reservation.Status status);
    void updateEndTime(int reservationId, LocalDateTime endTime);
}
