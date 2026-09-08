package com.labresa.dao;

import com.labresa.model.UsageLog;

import java.util.List;

public interface UsageLogDAO {
    void save(UsageLog usageLog);
    void update(UsageLog usageLog);
    List<UsageLog> findByResource(int resourceId);
    List<UsageLog> findAll();
}

