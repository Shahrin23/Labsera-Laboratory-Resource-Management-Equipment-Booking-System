package com.labresa.dao.impl;

import com.labresa.dao.MaintenanceRecordDAO;
import com.labresa.db.DatabaseConnectionManager;
import com.labresa.model.MaintenanceRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteMaintenanceRecordDAO implements MaintenanceRecordDAO {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    private MaintenanceRecord mapRow(ResultSet rs) throws SQLException {
        Integer techId = (Integer) rs.getObject("technician_id");
        String endStr = rs.getString("end_date");
        return new MaintenanceRecord(
                rs.getInt("id"), rs.getInt("resource_id"), techId,
                LocalDateTime.parse(rs.getString("start_date")),
                endStr == null ? null : LocalDateTime.parse(endStr),
                MaintenanceRecord.Reason.valueOf(rs.getString("reason")),
                rs.getString("notes")
        );
    }

    @Override
    public void save(MaintenanceRecord record) {
        String sql = "INSERT INTO maintenance_records (resource_id, technician_id, start_date, end_date, reason, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getResourceId());
            if (record.getTechnicianId() != null) {
                ps.setInt(2, record.getTechnicianId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, record.getStartDate().toString());
            ps.setString(4, record.getEndDate() == null ? null : record.getEndDate().toString());
            ps.setString(5, record.getReason().name());
            ps.setString(6, record.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) record.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save maintenance record", e);
        }
    }

    @Override
    public void completeActive(int resourceId, LocalDateTime endDate) {
        String sql = "UPDATE maintenance_records SET end_date = ? WHERE resource_id = ? AND end_date IS NULL";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, endDate.toString());
            ps.setInt(2, resourceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to complete maintenance for resource " + resourceId, e);
        }
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        List<MaintenanceRecord> results = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_records";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch maintenance records", e);
        }
        return results;
    }

    @Override
    public List<MaintenanceRecord> findByResource(int resourceId) {
        List<MaintenanceRecord> results = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_records WHERE resource_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch maintenance records for resource " + resourceId, e);
        }
        return results;
    }
}
