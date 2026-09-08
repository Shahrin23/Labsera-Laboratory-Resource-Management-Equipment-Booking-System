package com.labresa.service;

import com.labresa.dao.ReservationDAO;
import com.labresa.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO;

    public ReservationService(ReservationDAO reservationDAO) {
        this.reservationDAO = reservationDAO;
    }


    public boolean checkConflict(int resourceId, LocalDateTime start, LocalDateTime end) {
        return !reservationDAO.findOverlapping(resourceId, start, end).isEmpty();
    }


    public Reservation request(Reservation reservation) {
        if (checkConflict(reservation.getResourceId(), reservation.getStartTime(), reservation.getEndTime())) {
            throw new ReservationConflictException(
                    "Requested slot conflicts with an existing reservation for resource "
                            + reservation.getResourceId());
        }
        reservation.setStatus(Reservation.Status.PENDING);
        reservationDAO.save(reservation);
        return reservation;
    }

    public void cancel(int reservationId) {
        reservationDAO.updateStatus(reservationId, Reservation.Status.CANCELLED);
    }

    public List<Reservation> findOverlapping(int resourceId, LocalDateTime start, LocalDateTime end) {
        return reservationDAO.findOverlapping(resourceId, start, end);
    }

    public static class ReservationConflictException extends RuntimeException {
        public ReservationConflictException(String message) { super(message); }
    }
}
