package com.labresa.dao;

import java.time.LocalDateTime;
import java.util.List;

import com.labresa.model.MaintenanceRecord;

public interface MaintenanceRecordDAO {
    void save(MaintenanceRecord record);
    void completeActive(int resourceId, LocalDateTime endDate);
    List<MaintenanceRecord> findAll();
    List<MaintenanceRecord> findByResource(int resourceId);
}