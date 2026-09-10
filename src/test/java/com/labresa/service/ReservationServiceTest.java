package com.labresa.service;

import com.labresa.dao.ReservationDAO;
import com.labresa.dao.ResourceDAO;
import com.labresa.model.Equipment;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.model.User;
import com.labresa.patterns.approval.ApprovalResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationDAO reservationDAO;
    private ResourceDAO resourceDAO;
    private ApprovalService approvalService;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationDAO = mock(ReservationDAO.class);
        resourceDAO = mock(ResourceDAO.class);
        approvalService = mock(ApprovalService.class);
        reservationService = new ReservationService(reservationDAO, resourceDAO, approvalService);
    }

    private Reservation newReservation(int resourceId, LocalDateTime start, LocalDateTime end, int quantity) {
        return new Reservation(0, resourceId, 3, User.Role.GRAD, start, end,
                Reservation.Status.PENDING, false, "", "", quantity, null, null, null);
    }

    @Test
    void rejectsWhenNotEnoughUnitsAvailableForThePeriod() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

        Resource resource = new Equipment(1, "Microscope", Resource.Category.COMMON, 2, 50, "MICROSCOPE");
        when(resourceDAO.findById(1)).thenReturn(resource);

        Reservation existing = newReservation(1, start, end, 2); // already holds both units
        when(reservationDAO.findOverlapping(1, start, end)).thenReturn(List.of(existing));

        Reservation newRequest = newReservation(1, start, end, 1);
        ApprovalResult result = reservationService.request(newRequest);

        assertFalse(result.isApproved());
        assertEquals(Reservation.Status.REJECTED, newRequest.getStatus());
        verify(approvalService, never()).evaluate(any(), any());
    }

    @Test
    void allowsBookingWhenEnoughUnitsRemainDespiteOverlap() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);

        // 5 units total, 1 already held by an overlapping reservation - plenty of room for 2 more
        Resource resource = new Equipment(1, "Microscope", Resource.Category.COMMON, 5, 50, "MICROSCOPE");
        when(resourceDAO.findById(1)).thenReturn(resource);

        Reservation existing = newReservation(1, start, end, 1);
        when(reservationDAO.findOverlapping(1, start, end)).thenReturn(List.of(existing));
        when(approvalService.evaluate(any(), any())).thenReturn(ApprovalResult.approved("Technician (auto-approved, common resource)"));

        Reservation newRequest = newReservation(1, start, end, 2);
        ApprovalResult result = reservationService.request(newRequest);

        assertTrue(result.isApproved());
        verify(reservationDAO).save(newRequest);
        // This is the exact bug that was fixed: a resource with multiple units must
        // remain bookable by other people even while some units are already held.
        assertEquals(2, resource.getAvailableQuantity()); // 5 total - 1 existing - 2 new = 2 left
    }

    @Test
    void rejectsWhenResourceIsUnderMaintenance() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);
        when(reservationDAO.findOverlapping(1, start, end)).thenReturn(Collections.emptyList());

        Resource resource = new Equipment(1, "Microscope", Resource.Category.COMMON, 3, 50, "MICROSCOPE");
        resource.markUnderMaintenance();
        when(resourceDAO.findById(1)).thenReturn(resource);

        Reservation newRequest = newReservation(1, start, end, 1);
        ApprovalResult result = reservationService.request(newRequest);

        assertFalse(result.isApproved());
        assertEquals(Reservation.Status.REJECTED, newRequest.getStatus());
    }

    @Test
    void sameResourceCanBeBookedForADifferentNonOverlappingDateAfterApproval() {
        LocalDateTime firstStart = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime firstEnd = LocalDateTime.of(2026, 9, 10, 11, 0);
        LocalDateTime secondStart = LocalDateTime.of(2026, 9, 15, 9, 0);
        LocalDateTime secondEnd = LocalDateTime.of(2026, 9, 15, 11, 0);

        Resource resource = new Equipment(1, "Microscope", Resource.Category.COMMON, 1, 50, "MICROSCOPE");
        when(resourceDAO.findById(1)).thenReturn(resource);
        when(approvalService.evaluate(any(), any())).thenReturn(ApprovalResult.approved("Technician (auto-approved, common resource)"));

        when(reservationDAO.findOverlapping(eq(1), eq(firstStart), eq(firstEnd))).thenReturn(Collections.emptyList());
        when(reservationDAO.findOverlapping(eq(1), eq(secondStart), eq(secondEnd))).thenReturn(Collections.emptyList());

        Reservation first = newReservation(1, firstStart, firstEnd, 1);
        Reservation second = newReservation(1, secondStart, secondEnd, 1);

        ApprovalResult firstResult = reservationService.request(first);
        ApprovalResult secondResult = reservationService.request(second);

        assertTrue(firstResult.isApproved());
        assertTrue(secondResult.isApproved());
    }
}
