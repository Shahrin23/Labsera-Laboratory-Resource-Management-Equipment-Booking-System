package com.labresa.patterns.report;

import com.labresa.dao.MaintenanceRecordDAO;
import com.labresa.model.MaintenanceRecord;

import java.util.List;

public class MaintenanceHistoryReport extends ReportGenerator<MaintenanceRecord> {

    private final MaintenanceRecordDAO maintenanceRecordDAO;

    public MaintenanceHistoryReport(MaintenanceRecordDAO maintenanceRecordDAO) {
        this.maintenanceRecordDAO = maintenanceRecordDAO;
    }

    @Override
    protected List<MaintenanceRecord> fetchData() {
        return maintenanceRecordDAO.findAll();
    }

    @Override
    protected String formatReport(List<MaintenanceRecord> data) {
        StringBuilder sb = new StringBuilder("=== Maintenance History Report ===\n");
        for (MaintenanceRecord m : data) {
            sb.append(String.format("resource=%d reason=%s start=%s end=%s%n",
                    m.getResourceId(), m.getReason(), m.getStartDate(),
                    m.getEndDate() == null ? "ONGOING" : m.getEndDate()));
        }
        return sb.toString();
    }
}