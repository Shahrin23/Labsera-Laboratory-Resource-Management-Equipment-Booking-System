package com.labresa.dao.memory;

import com.labresa.dao.UsageLogDAO;
import com.labresa.model.UsageLog;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryUsageLogDAO implements UsageLogDAO {

    private final Map<Integer, UsageLog> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public void save(UsageLog usageLog) {
        if (usageLog.getId() == 0) {
            usageLog.setId(nextId++);
        }
        store.put(usageLog.getId(), usageLog);
    }

    @Override
    public void update(UsageLog usageLog) {
        store.put(usageLog.getId(), usageLog);
    }

    @Override
    public List<UsageLog> findByResource(int resourceId) {

        return new ArrayList<>(store.values());
    }

    @Override
    public List<UsageLog> findAll() {
        return new ArrayList<>(store.values());
    }
}
