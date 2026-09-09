package com.labresa.patterns.approval;

import com.labresa.model.Equipment;
import com.labresa.model.LabRoom;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalHandlerChainTest {

    private final Reservation dummyReservation = new Reservation(
            1, 1, 1, User.Role.GRAD,
            LocalDateTime.now(), LocalDateTime.now().plusHours(1),
            Reservation.Status.PENDING, false, "", "");

    @Test
    void lowCostResourceIsAutoApprovedByTechnician() {
        ApprovalHandler chain = ApprovalHandlerFactory.buildDefaultChain();
        Resource cheap = new Equipment(1, "Testing Kit", 120, 15, "TESTING_KIT");

        ApprovalResult result = chain.handle(dummyReservation, cheap);

        assertTrue(result.isApproved());
        assertTrue(result.getApprovedBy().contains("Technician"));
    }

    @Test
    void midCostResourceEscalatesToFacultyAndIsApproved() {
        ApprovalHandler chain = ApprovalHandlerFactory.buildDefaultChain();
        Resource midCost = new Equipment(2, "3D Printer", 2000, 30, "3D_PRINTER");

        ApprovalResult result = chain.handle(dummyReservation, midCost);

        assertTrue(result.isApproved());
        assertEquals("Faculty Supervisor", result.getApprovedBy());
    }

    @Test
    void extremelyHighCostResourceIsRejectedByFaculty() {
        ApprovalHandler chain = ApprovalHandlerFactory.buildDefaultChain();
        Resource veryExpensive = new LabRoom(3, "Wet Lab Room A", 10000, 200, 20);

        ApprovalResult result = chain.handle(dummyReservation, veryExpensive);

        assertFalse(result.isApproved());
    }

    @Test
    void costExactlyAtAutoApproveThresholdIsApprovedByTechnician() {
        ApprovalHandler chain = ApprovalHandlerFactory.buildDefaultChain();
        Resource atThreshold = new Equipment(4, "Boundary Item", 500, 20, "GENERAL");

        ApprovalResult result = chain.handle(dummyReservation, atThreshold);

        assertTrue(result.isApproved());
        assertTrue(result.getApprovedBy().contains("Technician"));
    }
}
