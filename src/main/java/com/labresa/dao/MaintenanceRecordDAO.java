package com.labresa.dao;

import com.labresa.model.MaintenanceRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceRecordDAO {
    void save(MaintenanceRecord record);
    void completeActive(int resourceId, LocalDateTime endDate);
    List<MaintenanceRecord> findAll();
    List<MaintenanceRecord> findByResource(int resourceId);
}