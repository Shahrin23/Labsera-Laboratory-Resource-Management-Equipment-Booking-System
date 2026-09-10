package com.labresa.dao.impl;

import com.labresa.dao.NotificationDAO;
import com.labresa.db.DatabaseConnectionManager;
import com.labresa.model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteNotificationDAO implements NotificationDAO {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    @Override
    public void save(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, reservation_id, message, type, is_read, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            if (notification.getReservationId() != null) {
                ps.setInt(2, notification.getReservationId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType().name());
            ps.setInt(5, notification.isRead() ? 1 : 0);
            ps.setString(6, notification.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) notification.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save notification", e);
        }
    }

    @Override
    public List<Notification> findByUser(int userId) {
        List<Notification> results = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer reservationId = (Integer) rs.getObject("reservation_id");
                    results.add(new Notification(
                            rs.getInt("id"), rs.getInt("user_id"), reservationId,
                            rs.getString("message"),
                            Notification.Type.valueOf(rs.getString("type")),
                            rs.getInt("is_read") == 1,
                            LocalDateTime.parse(rs.getString("created_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch notifications for user " + userId, e);
        }
        return results;
    }
}
