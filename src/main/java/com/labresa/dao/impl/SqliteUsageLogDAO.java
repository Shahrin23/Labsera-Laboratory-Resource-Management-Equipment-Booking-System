package com.labresa.dao.impl;

import com.labresa.dao.UsageLogDAO;
import com.labresa.db.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class SqliteUsageLogDAO implements UsageLogDAO {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    @Override
    public void insertCheckIn(int reservationId, LocalDateTime checkIn) {
        String sql = "INSERT INTO usage_logs (reservation_id, check_in) VALUES (?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setString(2, checkIn.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record check-in for reservation " + reservationId, e);
        }
    }

    @Override
    public void completeCheckOut(int reservationId, LocalDateTime checkOut, String conditionNotes) {
        String sql = "UPDATE usage_logs SET check_out = ?, condition_notes = ? WHERE reservation_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, checkOut.toString());
            ps.setString(2, conditionNotes);
            ps.setInt(3, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record check-out for reservation " + reservationId, e);
        }
    }
}
