package com.labresa.service;

import com.labresa.dao.MaintenanceRecordDAO;
import com.labresa.dao.ResourceDAO;
import com.labresa.model.MaintenanceRecord;
import com.labresa.model.Resource;
import com.labresa.patterns.observer.NotificationDispatcher;

import java.time.LocalDateTime;

public class ResourceService {

    private final ResourceDAO resourceDAO;
    private final MaintenanceRecordDAO maintenanceRecordDAO;
    private final NotificationDispatcher dispatcher;

    public ResourceService(ResourceDAO resourceDAO, MaintenanceRecordDAO maintenanceRecordDAO,
                           NotificationDispatcher dispatcher) {
        this.resourceDAO = resourceDAO;
        this.maintenanceRecordDAO = maintenanceRecordDAO;
        this.dispatcher = dispatcher;
    }


    public void recordCompletedUse(Resource resource) {
        resourceDAO.update(resource);

        if (resource.getStatus().equals("UNDER_MAINTENANCE")) {
            openMaintenanceRecord(resource);
            dispatcher.dispatch("MAINTENANCE_STARTED",
                    "Resource '" + resource.getName() + "' has reached its usage threshold and now requires maintenance. Bookings are blocked.");
        }
    }

    public void markMaintenance(Resource resource, String notes) {
        resource.markUnderMaintenance();
        resourceDAO.update(resource);
        MaintenanceRecord record = new MaintenanceRecord(0, resource.getId(), LocalDateTime.now(), notes);
        maintenanceRecordDAO.save(record);
        dispatcher.dispatch("MAINTENANCE_STARTED",
                "Resource '" + resource.getName() + "' has been taken offline for maintenance: " + notes);
    }

    public void completeMaintenance(Resource resource) {
        resource.markAvailable();
        resourceDAO.update(resource);
        maintenanceRecordDAO.findByResource(resource.getId()).stream()
                .filter(MaintenanceRecord::isOpen)
                .findFirst()
                .ifPresent(record -> {
                    record.setEndDate(LocalDateTime.now());
                    maintenanceRecordDAO.update(record);
                });
        dispatcher.dispatch("SLOT_FREED",
                "Resource '" + resource.getName() + "' is available again after maintenance.");
    }

    private void openMaintenanceRecord(Resource resource) {
        MaintenanceRecord record = new MaintenanceRecord(0, resource.getId(), LocalDateTime.now(),
                "Auto-triggered: usage threshold reached (" + resource.getUsageCounter() + "/" + resource.getMaintenanceThreshold() + ")");
        maintenanceRecordDAO.save(record);
    }
}
