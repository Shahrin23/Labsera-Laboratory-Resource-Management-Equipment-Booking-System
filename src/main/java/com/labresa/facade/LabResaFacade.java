package com.labresa.facade;

import com.labresa.dao.*;
import com.labresa.dao.impl.*;
import com.labresa.model.MaintenanceRecord;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.model.User;
import com.labresa.patterns.approval.ApprovalHandler;
import com.labresa.patterns.approval.ApprovalHandlerFactory;
import com.labresa.patterns.approval.ApprovalResult;
import com.labresa.patterns.observer.InAppNotificationChannel;
import com.labresa.patterns.observer.NotificationDispatcher;
import com.labresa.patterns.strategy.PriorityStrategy;
import com.labresa.patterns.strategy.RolePriorityStrategy;
import com.labresa.service.*;

import java.util.List;

/**
 * Facade pattern: the ONLY class JavaFX controllers talk to. A controller
 * never instantiates a DAO or Service directly - it only ever calls a method
 * here.
 */
public class LabResaFacade {

    private final UserDAO userDAO = new SqliteUserDAO();
    private final ResourceDAO resourceDAO = new SqliteResourceDAO();
    private final ReservationDAO reservationDAO = new SqliteReservationDAO();
    private final ApprovalDAO approvalDAO = new SqliteApprovalDAO();
    private final UsageLogDAO usageLogDAO = new SqliteUsageLogDAO();
    private final MaintenanceRecordDAO maintenanceRecordDAO = new SqliteMaintenanceRecordDAO();
    private final NotificationDAO notificationDAO = new SqliteNotificationDAO();

    private final NotificationDispatcher notificationDispatcher = new NotificationDispatcher(notificationDAO);
    private final ApprovalHandler approvalChain = ApprovalHandlerFactory.buildDefaultChain();
    private final PriorityStrategy priorityStrategy = new RolePriorityStrategy();

    private final ResourceService resourceService = new ResourceService(resourceDAO);
    private final UserService userService = new UserService(userDAO);
    private final ApprovalService approvalService = new ApprovalService(
            approvalChain, approvalDAO, reservationDAO, resourceDAO, priorityStrategy, notificationDispatcher);
    private final ReservationService reservationService =
            new ReservationService(reservationDAO, resourceDAO, approvalService);
    private final MaintenanceService maintenanceService = new MaintenanceService(
            resourceDAO, maintenanceRecordDAO, reservationDAO, notificationDispatcher);
    private final UsageService usageService =
            new UsageService(reservationDAO, resourceDAO, usageLogDAO, maintenanceService);
    private final NotificationService notificationService = new NotificationService(notificationDAO);

    public LabResaFacade() {
        notificationDispatcher.subscribe(new InAppNotificationChannel());
    }

    // ---- Auth ----
    public User login(String email, String rawPassword) {
        if (!userDAO.verifyPassword(email, rawPassword)) return null;
        return userDAO.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userService.emailExists(email);
    }

    public boolean isPasswordValid(String password) {
        return userService.isPasswordValid(password);
    }

    public User register(String firstName, String lastName, String session, String classRoll,
                          String email, String password, User.Role role) {
        return userService.register(firstName, lastName, session, classRoll, email, password, role);
    }

    public void resetPassword(String email, String newPassword) {
        userService.resetPassword(email, newPassword);
    }

    public User findUserById(int id) {
        return userDAO.findById(id);
    }

    // ---- Resources ----
    public List<Resource> getResourceCatalog() {
        return resourceService.listAll();
    }

    public Resource createResource(String type, String name, Resource.Category category, int totalQuantity, String detail) {
        return resourceService.createResource(type, name, category, totalQuantity, detail);
    }

    // ---- Reservations (Workflow A) ----
    public ApprovalResult requestReservation(Reservation reservation) {
        return reservationService.request(reservation);
    }

    public void cancelReservation(int reservationId) {
        reservationService.cancel(reservationId);
    }

    public List<Reservation> getAllReservations() {
        return reservationService.findAll();
    }

    public List<Reservation> getReservationsForUser(int userId) {
        return reservationDAO.findByUser(userId);
    }

    // ---- Approval Queue (manual, SPECIAL-category reservations) ----
    /** Reservations still PENDING a human decision - i.e. escalated SPECIAL requests. */
    public List<Reservation> getPendingApprovals() {
        return reservationDAO.findByStatus(Reservation.Status.PENDING).stream()
                .filter(r -> {
                    Resource resource = resourceDAO.findById(r.getResourceId());
                    return resource != null && resource.getCategory() == Resource.Category.SPECIAL;
                })
                .toList();
    }

    public void decideOnReservation(int reservationId, int approverUserId, boolean approve, String comments) {
        User approver = userDAO.findById(approverUserId);
        approvalService.decide(reservationId, approver, approve, comments);
    }

    // ---- Usage / check-in-out (bridges Workflow A -> Workflow B) ----
    public void checkIn(int reservationId) {
        usageService.checkIn(reservationId);
    }

    public void checkOut(int reservationId, String conditionNotes) {
        usageService.checkOut(reservationId, conditionNotes);
    }

    // ---- Maintenance (Workflow B + new Technician-controlled toggle) ----
    public void reportDamage(int resourceId, int technicianId, String notes) {
        maintenanceService.reportDamage(resourceService.findById(resourceId), technicianId, notes);
    }

    public void completeMaintenance(int resourceId, String completionNotes) {
        maintenanceService.complete(resourceService.findById(resourceId), completionNotes);
    }

    // ---- Notifications (scoped strictly per-user) ----
    public List<com.labresa.model.Notification> getNotifications(int userId) {
        return notificationService.forUser(userId);
    }

    // ---- Reports: raw data, so the UI renders interactive tables instead of plain text ----
    public List<Resource> getUtilizationData() {
        return resourceDAO.findAll();
    }

    public List<MaintenanceRecord> getMaintenanceHistoryData() {
        return maintenanceRecordDAO.findAll();
    }
}
