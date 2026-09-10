package com.labresa.patterns.report;

import com.labresa.dao.ResourceDAO;
import com.labresa.model.Resource;

import java.util.List;

public class UtilizationReport extends ReportGenerator<Resource> {

    private final ResourceDAO resourceDAO;

    public UtilizationReport(ResourceDAO resourceDAO) {
        this.resourceDAO = resourceDAO;
    }

    @Override
    protected List<Resource> fetchData() {
        return resourceDAO.findAll();
    }

    @Override
    protected String formatReport(List<Resource> data) {
        StringBuilder sb = new StringBuilder("=== Resource Utilization Report ===\n");
        for (Resource r : data) {
            sb.append(String.format("%-20s %-12s usage=%d/%d status=%s%n",
                    r.getName(), r.getType(), r.getUsageCounter(), r.getMaintenanceThreshold(), r.getStatus()));
        }
        return sb.toString();
    }
}