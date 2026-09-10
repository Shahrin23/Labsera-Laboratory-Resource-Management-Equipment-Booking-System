package com.labresa.service;

import com.labresa.dao.ReservationDAO;
import com.labresa.dao.ResourceDAO;
import com.labresa.dao.UsageLogDAO;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;

import java.time.LocalDateTime;

/**
 * Bridges Workflow A's completion into Workflow B. Round 4: resource-level
 * State transitions were removed (see Resource.java); check-in/out now only
 * updates the Reservation's own status, and releases/holds units on the
 * quantity counter - never a single global resource state.
 */
public class UsageService {

    private final ReservationDAO reservationDAO;
    private final ResourceDAO resourceDAO;
    private final UsageLogDAO usageLogDAO;
    private final MaintenanceService maintenanceService;

    public UsageService(ReservationDAO reservationDAO, ResourceDAO resourceDAO,
                         UsageLogDAO usageLogDAO, MaintenanceService maintenanceService) {
        this.reservationDAO = reservationDAO;
        this.resourceDAO = resourceDAO;
        this.usageLogDAO = usageLogDAO;
        this.maintenanceService = maintenanceService;
    }

    public void checkIn(int reservationId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }
        if (reservation.getStatus() != Reservation.Status.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot check in: reservation is " + reservation.getStatus() +
                            ", not CONFIRMED. It must be approved first.");
        }

        reservationDAO.updateStatus(reservationId, Reservation.Status.IN_USE);
        usageLogDAO.insertCheckIn(reservationId, LocalDateTime.now());
    }

    public void checkOut(int reservationId, String conditionNotes) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }
        if (reservation.getStatus() != Reservation.Status.IN_USE) {
            throw new IllegalStateException(
                    "Cannot check out: reservation is " + reservation.getStatus() +
                            ", not IN_USE. Check in first.");
        }

        Resource resource = resourceDAO.findById(reservation.getResourceId());

        LocalDateTime actualCheckOut = LocalDateTime.now();

        // Bug fix: if returned before the originally requested end time, the
        // reservation's end time is updated to reflect what actually happened,
        // rather than silently keeping the stale, originally-requested time.
        if (actualCheckOut.isBefore(reservation.getEndTime())) {
            reservation.setEndTime(actualCheckOut);
            reservationDAO.updateEndTime(reservationId, actualCheckOut);
        }

        // Release the held unit(s) back to the pool - this booking's window is over.
        if (resource != null) {
            resource.releaseUnits(reservation.getQuantity());
            resourceDAO.update(resource);
            // Tracks usage toward the maintenance threshold; may auto-schedule maintenance.
            maintenanceService.recordCompletedUse(resource);
        }

        reservationDAO.updateStatus(reservationId, Reservation.Status.COMPLETED);
        usageLogDAO.completeCheckOut(reservationId, actualCheckOut, conditionNotes);
    }
}
