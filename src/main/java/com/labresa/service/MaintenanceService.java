package com.labresa.service;

import com.labresa.dao.MaintenanceRecordDAO;
import com.labresa.dao.ReservationDAO;
import com.labresa.dao.ResourceDAO;
import com.labresa.model.MaintenanceRecord;
import com.labresa.model.Notification;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.patterns.observer.NotificationDispatcher;
import com.labresa.patterns.state.UnderMaintenanceState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class MaintenanceService {

    private final ResourceDAO resourceDAO;
    private final MaintenanceRecordDAO maintenanceRecordDAO;
    private final ReservationDAO reservationDAO;
    private final NotificationDispatcher notificationDispatcher;

    public MaintenanceService(ResourceDAO resourceDAO,
                              MaintenanceRecordDAO maintenanceRecordDAO,
                              ReservationDAO reservationDAO,
                              NotificationDispatcher notificationDispatcher) {
        this.resourceDAO = resourceDAO;
        this.maintenanceRecordDAO = maintenanceRecordDAO;
        this.reservationDAO = reservationDAO;
        this.notificationDispatcher = notificationDispatcher;
    }


    public Optional<MaintenanceRecord> recordCompletedUse(Resource resource) {
        resource.incrementUsageCounter(); // may internally auto-transition to UNDER_MAINTENANCE
        resourceDAO.update(resource);

        if (isUnderMaintenance(resource)) {
            String notes = "Usage counter reached maintenance threshold ("
                    + resource.getMaintenanceThreshold() + ").";
            return Optional.of(schedule(resource, null, MaintenanceRecord.Reason.THRESHOLD, notes, false));
        }
        return Optional.empty();
    }

    public MaintenanceRecord reportDamage(Resource resource, int technicianId, String notes) {
        return schedule(resource, technicianId, MaintenanceRecord.Reason.DAMAGE_REPORTED, notes, true);
    }


    private MaintenanceRecord schedule(Resource resource, Integer technicianId, MaintenanceRecord.Reason reason,
                                       String notes, boolean forceTransition) {
        if (forceTransition) {
            resource.markUnderMaintenance();
        }
        resourceDAO.update(resource);

        MaintenanceRecord record = new MaintenanceRecord(0, resource.getId(), technicianId,
                LocalDateTime.now(), null, reason, notes);
        maintenanceRecordDAO.save(record);

        notifyAffectedUsers(resource,
                "may be affected: '" + resource.getName() + "' has been placed under maintenance.",
                Notification.Type.MAINTENANCE_ALERT);

        return record;
    }


    public void complete(Resource resource, String completionNotes) {
        if (!isUnderMaintenance(resource)) {
            throw new IllegalStateException(
                    "Resource '" + resource.getName() + "' is not currently under maintenance.");
        }

        resource.markAvailable();
        resourceDAO.update(resource);
        maintenanceRecordDAO.completeActive(resource.getId(), LocalDateTime.now());

        notifyAffectedUsers(resource,
                "is available again: '" + resource.getName() + "' has completed maintenance.",
                Notification.Type.SLOT_FREED);
    }

    private boolean isUnderMaintenance(Resource resource) {
        return resource.getState() instanceof UnderMaintenanceState;
    }

    private void notifyAffectedUsers(Resource resource, String messageSuffix, Notification.Type type) {
        List<Reservation> affected = reservationDAO.findAll().stream()
                .filter(r -> r.getResourceId() == resource.getId())
                .filter(r -> r.getStatus() == Reservation.Status.PENDING
                        || r.getStatus() == Reservation.Status.CONFIRMED)
                .toList();

        for (Reservation reservation : affected) {
            Notification notification = new Notification(0, reservation.getUserId(), reservation.getId(),
                    "Your booking " + messageSuffix, type, false, LocalDateTime.now());
            notificationDispatcher.dispatch(notification);
        }
    }
}

