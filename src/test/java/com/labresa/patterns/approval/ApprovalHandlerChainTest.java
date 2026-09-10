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

    private Reservation reservationWithLetter(boolean withLetter) {
        return new Reservation(1, 1, 1, User.Role.GRAD,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                Reservation.Status.PENDING, false, "", "", 1,
                withLetter ? "Dr. Smith" : null, withLetter ? "Chairman" : null, withLetter ? "Letter ref #1" : null);
    }

    @Test
    void commonResourceIsAutoApprovedByTechnicianRegardlessOfFormalLetter() {
        ApprovalHandler chain = ApprovalHandlerFactory.buildDefaultChain();
        Resource common = new Equipment(1, "Testing Kit", Resource.Category.COMMON, 10, 15, "TESTING_KIT");

        ApprovalResult result = chain.handle(reservationWithLetter(false), common);

        assertTrue(result.isApproved());
        assertTrue(result.getApprovedBy().contains("Technician"));
    }

    @Test
    void specialResourceWithoutFormalLetterIsRejected() {
        ApprovalHandler chain = ApprovalHandlerFactory.buildDefaultChain();
        Resource special = new Equipment(2, "Electron Microscope", Resource.Category.SPECIAL, 1, 50, "MICROSCOPE");

        ApprovalResult result = chain.handle(reservationWithLetter(false), special);

        assertFalse(result.isApproved());
        assertFalse(result.isEscalated());
    }

    @Test
    void specialResourceWithFormalLetterEscalatesToManualReviewInsteadOfAutoApproving() {
        ApprovalHandler chain = ApprovalHandlerFactory.buildDefaultChain();
        Resource special = new LabRoom(3, "Wet Lab Room A", Resource.Category.SPECIAL, 1, 200, 20);

        ApprovalResult result = chain.handle(reservationWithLetter(true), special);

        // The key fix under test: presence of a formal letter must NOT cause an
        // automatic approval - it must escalate to a human decision.
        assertTrue(result.isEscalated());
        assertFalse(result.isApproved());
    }
}
