package com.labresa.dao;

import com.labresa.model.MaintenanceRecord;

import java.util.List;

public interface MaintenanceRecordDAO {
    void save(MaintenanceRecord record);
    void update(MaintenanceRecord record);
    List<MaintenanceRecord> findByResource(int resourceId);
    List<MaintenanceRecord> findAll();
}

