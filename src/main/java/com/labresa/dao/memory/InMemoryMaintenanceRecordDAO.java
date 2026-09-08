package com.labresa.dao.memory;

import com.labresa.dao.MaintenanceRecordDAO;
import com.labresa.model.MaintenanceRecord;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryMaintenanceRecordDAO implements MaintenanceRecordDAO {

    private final Map<Integer, MaintenanceRecord> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public void save(MaintenanceRecord record) {
        if (record.getId() == 0) {
            record.setId(nextId++);
        }
        store.put(record.getId(), record);
    }

    @Override
    public void update(MaintenanceRecord record) {
        store.put(record.getId(), record);
    }

    @Override
    public List<MaintenanceRecord> findByResource(int resourceId) {
        return store.values().stream()
                .filter(m -> m.getResourceId() == resourceId)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        return new ArrayList<>(store.values());
    }
}

