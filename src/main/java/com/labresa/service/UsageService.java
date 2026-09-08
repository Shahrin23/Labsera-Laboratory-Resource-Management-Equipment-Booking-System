package com.labresa.service;

import com.labresa.dao.UsageLogDAO;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.model.UsageLog;

import java.time.LocalDateTime;

public class UsageService {

    private final UsageLogDAO usageLogDAO;
    private final ResourceService resourceService;

    public UsageService(UsageLogDAO usageLogDAO, ResourceService resourceService) {
        this.usageLogDAO = usageLogDAO;
        this.resourceService = resourceService;
    }

    public UsageLog checkIn(Reservation reservation, Resource resource) {
        resource.checkIn();
        UsageLog log = new UsageLog(0, reservation.getId(), LocalDateTime.now());
        usageLogDAO.save(log);
        return log;
    }

    public void checkOut(UsageLog log, Resource resource, String conditionNotes) {
        resource.checkOut();
        log.setCheckOut(LocalDateTime.now());
        log.setConditionNotes(conditionNotes);
        usageLogDAO.update(log);
        resourceService.recordCompletedUse(resource);
    }
}
