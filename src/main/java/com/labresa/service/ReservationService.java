package com.labresa.service;

import com.labresa.dao.ReservationDAO;
import com.labresa.dao.ResourceDAO;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.patterns.approval.ApprovalResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implements Workflow A end-to-end: capacity check -> [PENDING] -> hold units
 * -> approval chain (Chain of Responsibility, run by ApprovalService) ->
 * [CONFIRMED] (auto for COMMON) / [PENDING, escalated] (SPECIAL, awaiting a
 * human decision) / [REJECTED].
 */
public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final ResourceDAO resourceDAO;
    private final ApprovalService approvalService;

    public ReservationService(ReservationDAO reservationDAO, ResourceDAO resourceDAO, ApprovalService approvalService) {
        this.reservationDAO = reservationDAO;
        this.resourceDAO = resourceDAO;
        this.approvalService = approvalService;
    }

    /**
     * Round 4: with quantity-based resources, a simple "any overlap = reject"
     * check is wrong - it would block a second booking even if 5 microscopes
     * exist and only 1 is taken. Capacity is now checked by summing the
     * quantities already held by overlapping active reservations and
     * comparing against the resource's total quantity.
     */
    public boolean hasCapacity(int resourceId, LocalDateTime start, LocalDateTime end, int requestedQuantity, int totalQuantity) {
        List<Reservation> overlapping = reservationDAO.findOverlapping(resourceId, start, end);
        int alreadyHeld = overlapping.stream().mapToInt(Reservation::getQuantity).sum();
        return alreadyHeld + requestedQuantity <= totalQuantity;
    }

    public ApprovalResult request(Reservation reservation) {
        Resource resource = resourceDAO.findById(reservation.getResourceId());
        if (resource == null) {
            reservation.setStatus(Reservation.Status.REJECTED);
            reservationDAO.save(reservation);
            return ApprovalResult.rejected("Resource not found.");
        }
        if ("UNDER_MAINTENANCE".equals(resource.getStatus())) {
            reservation.setStatus(Reservation.Status.REJECTED);
            reservationDAO.save(reservation);
            return ApprovalResult.rejected("Resource is currently under maintenance.");
        }
        if (!hasCapacity(reservation.getResourceId(), reservation.getStartTime(), reservation.getEndTime(),
                reservation.getQuantity(), resource.getTotalQuantity())) {
            reservation.setStatus(Reservation.Status.REJECTED);
            reservationDAO.save(reservation);
            return ApprovalResult.rejected("Not enough units available for that time period.");
        }

        reservation.setStatus(Reservation.Status.PENDING);
        reservationDAO.save(reservation);

        // Hold the units now so the live "available" count reflects this booking
        // immediately (fixes the "shows 0 available" display bug from before -
        // this counter is now actually wired up and kept in sync).
        resource.holdUnits(reservation.getQuantity());
        resourceDAO.update(resource);

        // Chain of Responsibility decides: auto-approve (COMMON), reject
        // (missing formal letter), or escalate to manual review (SPECIAL).
        return approvalService.evaluate(reservation, resource);
    }

    public void cancel(int reservationId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) return;
        if (reservation.getStatus() == Reservation.Status.PENDING || reservation.getStatus() == Reservation.Status.CONFIRMED) {
            Resource resource = resourceDAO.findById(reservation.getResourceId());
            if (resource != null) {
                resource.releaseUnits(reservation.getQuantity());
                resourceDAO.update(resource);
            }
        }
        reservationDAO.updateStatus(reservationId, Reservation.Status.CANCELLED);
    }

    public List<Reservation> findAll() {
        return reservationDAO.findAll();
    }

    public Reservation findById(int id) {
        return reservationDAO.findById(id);
    }
}
