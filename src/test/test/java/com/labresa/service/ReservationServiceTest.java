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

    @Test
    void rejectsWhenTimeSlotOverlapsExistingReservation() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);
        Reservation existing = new Reservation(1, 1, 2, User.Role.UNDERGRAD, start, end,
                Reservation.Status.CONFIRMED, false, "", "");
        when(reservationDAO.findOverlapping(1, start, end)).thenReturn(List.of(existing));

        Reservation newRequest = new Reservation(0, 1, 3, User.Role.GRAD, start, end,
                Reservation.Status.PENDING, false, "", "");

        ApprovalResult result = reservationService.request(newRequest);

        assertFalse(result.isApproved());
        assertEquals(Reservation.Status.REJECTED, newRequest.getStatus());
        verify(approvalService, never()).evaluate(any(), any());
    }

    @Test
    void proceedsToApprovalWhenNoConflictAndResourceAvailable() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);
        when(reservationDAO.findOverlapping(1, start, end)).thenReturn(Collections.emptyList());

        Resource resource = new Equipment(1, "Microscope", 300, 50, "MICROSCOPE");
        when(resourceDAO.findById(1)).thenReturn(resource);
        when(approvalService.evaluate(any(), any())).thenReturn(ApprovalResult.approved("Technician"));

        Reservation newRequest = new Reservation(0, 1, 3, User.Role.GRAD, start, end,
                Reservation.Status.PENDING, false, "", "");

        ApprovalResult result = reservationService.request(newRequest);

        assertTrue(result.isApproved());
        verify(reservationDAO).save(newRequest);
        verify(approvalService).evaluate(eq(newRequest), eq(resource));
        assertEquals("RESERVED", resource.getStatus());
    }

    @Test
    void rejectsWhenResourceIsUnderMaintenance() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 11, 0);
        when(reservationDAO.findOverlapping(1, start, end)).thenReturn(Collections.emptyList());

        Resource resource = new Equipment(1, "Microscope", 300, 50, "MICROSCOPE");
        resource.markUnderMaintenance();
        when(resourceDAO.findById(1)).thenReturn(resource);

        Reservation newRequest = new Reservation(0, 1, 3, User.Role.GRAD, start, end,
                Reservation.Status.PENDING, false, "", "");

        ApprovalResult result = reservationService.request(newRequest);

        assertFalse(result.isApproved());
        assertEquals(Reservation.Status.REJECTED, newRequest.getStatus());
    }
}
