package com.labresa.dao.impl;

import com.labresa.dao.ReservationDAO;
import com.labresa.db.DatabaseConnectionManager;
import com.labresa.model.Reservation;
import com.labresa.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteReservationDAO implements ReservationDAO {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("id"),
                rs.getInt("resource_id"),
                rs.getInt("user_id"),
                User.Role.valueOf(rs.getString("requester_role")),
                LocalDateTime.parse(rs.getString("start_time")),
                LocalDateTime.parse(rs.getString("end_time")),
                Reservation.Status.valueOf(rs.getString("status")),
                rs.getInt("recurring") == 1,
                rs.getString("notes"),
                rs.getString("priority_tag"),
                rs.getInt("quantity"),
                rs.getString("supervisor_name"),
                rs.getString("supervisor_role"),
                rs.getString("letter_reference")
        );
    }

    @Override
    public Reservation findById(int id) {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservation " + id, e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> results = new ArrayList<>();
        String sql = "SELECT * FROM reservations";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch reservations", e);
        }
        return results;
    }

    @Override
    public List<Reservation> findByUser(int userId) {
        List<Reservation> results = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE user_id = ? ORDER BY requested_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch reservations for user " + userId, e);
        }
        return results;
    }

    @Override
    public List<Reservation> findByStatus(Reservation.Status status) {
        List<Reservation> results = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status = ? ORDER BY requested_at ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch reservations by status " + status, e);
        }
        return results;
    }

    @Override
    public List<Reservation> findOverlapping(int resourceId, LocalDateTime start, LocalDateTime end) {
        List<Reservation> results = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE resource_id = ? " +
                "AND status IN ('PENDING','CONFIRMED','IN_USE') " +
                "AND start_time < ? AND end_time > ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setString(2, end.toString());
            ps.setString(3, start.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check overlapping reservations", e);
        }
        return results;
    }

    @Override
    public void save(Reservation reservation) {
        String sql = "INSERT INTO reservations " +
                "(resource_id, user_id, requester_role, start_time, end_time, status, recurring, notes, priority_tag, " +
                "quantity, supervisor_name, supervisor_role, letter_reference, requested_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getResourceId());
            ps.setInt(2, reservation.getUserId());
            ps.setString(3, reservation.getRequesterRole().name());
            ps.setString(4, reservation.getStartTime().toString());
            ps.setString(5, reservation.getEndTime().toString());
            ps.setString(6, reservation.getStatus().name());
            ps.setInt(7, reservation.isRecurring() ? 1 : 0);
            ps.setString(8, reservation.getNotes());
            ps.setString(9, reservation.getPriorityTag());
            ps.setInt(10, reservation.getQuantity());
            ps.setString(11, reservation.getSupervisorName());
            ps.setString(12, reservation.getSupervisorRole());
            ps.setString(13, reservation.getLetterReference());
            ps.setString(14, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) reservation.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save reservation", e);
        }
    }

    @Override
    public void updateStatus(int reservationId, Reservation.Status status) {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reservation status " + reservationId, e);
        }
    }

    @Override
    public void updateEndTime(int reservationId, LocalDateTime endTime) {
        String sql = "UPDATE reservations SET end_time = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, endTime.toString());
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update end time for reservation " + reservationId, e);
        }
    }
}
