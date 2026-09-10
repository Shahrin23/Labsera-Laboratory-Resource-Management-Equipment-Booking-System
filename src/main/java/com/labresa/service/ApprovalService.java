package com.labresa.service;

import com.labresa.dao.ApprovalDAO;
import com.labresa.dao.ReservationDAO;
import com.labresa.dao.ResourceDAO;
import com.labresa.model.*;
import com.labresa.patterns.approval.ApprovalHandler;
import com.labresa.patterns.observer.NotificationDispatcher;
import com.labresa.patterns.strategy.PriorityStrategy;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs a reservation through the Chain of Responsibility. Three outcomes:
 *  - APPROVED (COMMON resource): confirm immediately, notify now.
 *  - REJECTED (e.g. missing formal letter): release held units, notify now.
 *  - ESCALATED (SPECIAL resource, formal letter present): stays PENDING,
 *    units stay held, and - this is the key fix for the reported bug - NO
 *    approval/rejection notification is sent yet. The applicant only learns
 *    the outcome once a human approval-authority (Faculty/Admin) calls
 *    decide() from the Approval Queue screen. The resource is also NOT
 *    marked IN_USE at this point - that only happens at actual check-in,
 *    which itself requires the reservation to already be CONFIRMED.
 */
public class ApprovalService {

    private final ApprovalHandler approvalChain;
    private final ApprovalDAO approvalDAO;
    private final ReservationDAO reservationDAO;
    private final ResourceDAO resourceDAO;
    private final PriorityStrategy priorityStrategy;
    private final NotificationDispatcher notificationDispatcher;

    public ApprovalService(ApprovalHandler approvalChain, ApprovalDAO approvalDAO, ReservationDAO reservationDAO,
                            ResourceDAO resourceDAO, PriorityStrategy priorityStrategy,
                            NotificationDispatcher notificationDispatcher) {
        this.approvalChain = approvalChain;
        this.approvalDAO = approvalDAO;
        this.reservationDAO = reservationDAO;
        this.resourceDAO = resourceDAO;
        this.priorityStrategy = priorityStrategy;
        this.notificationDispatcher = notificationDispatcher;
    }

    public com.labresa.patterns.approval.ApprovalResult evaluate(Reservation reservation, Resource resource) {
        com.labresa.patterns.approval.ApprovalResult result = approvalChain.handle(reservation, resource);

        if (result.isEscalated()) {
            // Awaiting a human decision - reservation stays PENDING, units stay held,
            // and only a "submitted" notice goes out, never an approval/rejection notice.
            Notification pendingNotice = new Notification(
                    0, reservation.getUserId(), reservation.getId(),
                    "Your request for '" + resource.getName() + "' has been submitted and is awaiting approval.",
                    Notification.Type.PENDING_APPROVAL, false, LocalDateTime.now());
            notificationDispatcher.dispatch(pendingNotice);
            return result;
        }

        Approval.Decision decision = result.isApproved() ? Approval.Decision.APPROVED : Approval.Decision.REJECTED;
        Approval approval = new Approval(0, reservation.getId(), null,
                resource.getCategory() == Resource.Category.COMMON ? Approval.Level.TECHNICIAN : Approval.Level.FACULTY,
                decision, result.toString(), LocalDateTime.now());
        approvalDAO.save(approval);

        Reservation.Status finalStatus = result.isApproved() ? Reservation.Status.CONFIRMED : Reservation.Status.REJECTED;
        reservationDAO.updateStatus(reservation.getId(), finalStatus);

        if (!result.isApproved()) {
            resource.releaseUnits(reservation.getQuantity());
            resourceDAO.update(resource);
        }

        Notification notification = new Notification(
                0, reservation.getUserId(), reservation.getId(),
                "Your reservation request was " + finalStatus + " (" + result + ")",
                Notification.Type.APPROVAL_RESULT, false, LocalDateTime.now());
        notificationDispatcher.dispatch(notification);

        return result;
    }

    /**
     * Manual decision entry point for the Approval Queue screen. This is the
     * ONLY place a SPECIAL/escalated reservation's fate is decided, and the
     * ONLY place its approval notification is sent - never automatically.
     */
    public void decide(int reservationId, User approver, boolean approve, String comments) {
        if (approver.getRole() != User.Role.FACULTY && approver.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only Faculty or Admin can approve special equipment requests.");
        }
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) throw new IllegalArgumentException("Reservation not found.");
        if (reservation.getStatus() != Reservation.Status.PENDING) {
            throw new IllegalStateException("This reservation is no longer pending approval (current status: " + reservation.getStatus() + ").");
        }
        Resource resource = resourceDAO.findById(reservation.getResourceId());

        Approval approval = new Approval(0, reservationId, approver.getId(), Approval.Level.FACULTY,
                approve ? Approval.Decision.APPROVED : Approval.Decision.REJECTED, comments, LocalDateTime.now());
        approvalDAO.save(approval);

        Reservation.Status finalStatus = approve ? Reservation.Status.CONFIRMED : Reservation.Status.REJECTED;
        reservationDAO.updateStatus(reservationId, finalStatus);

        if (!approve && resource != null) {
            resource.releaseUnits(reservation.getQuantity());
            resourceDAO.update(resource);
        }

        Notification notification = new Notification(
                0, reservation.getUserId(), reservationId,
                "Your reservation request was " + finalStatus + " by " + approver.getName() +
                        (comments == null || comments.isBlank() ? "" : " (" + comments + ")"),
                Notification.Type.APPROVAL_RESULT, false, LocalDateTime.now());
        notificationDispatcher.dispatch(notification);
    }

    public Reservation resolveCompetingRequests(List<Reservation> competitors) {
        Reservation winner = priorityStrategy.resolve(competitors);
        for (Reservation r : competitors) {
            if (r.getId() != winner.getId()) {
                reservationDAO.updateStatus(r.getId(), Reservation.Status.REJECTED);
            }
        }
        return winner;
    }
}
