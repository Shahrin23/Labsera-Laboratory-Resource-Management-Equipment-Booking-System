package com.labresa.dao.impl;

import com.labresa.dao.ApprovalDAO;
import com.labresa.db.DatabaseConnectionManager;
import com.labresa.model.Approval;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteApprovalDAO implements ApprovalDAO {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    @Override
    public void save(Approval approval) {
        String sql = "INSERT INTO approvals (reservation_id, approver_id, level, decision, comments, decided_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, approval.getReservationId());
            if (approval.getApproverId() != null) {
                ps.setInt(2, approval.getApproverId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, approval.getLevel().name());
            ps.setString(4, approval.getDecision().name());
            ps.setString(5, approval.getComments());
            ps.setString(6, approval.getDecidedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) approval.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save approval", e);
        }
    }

    @Override
    public List<Approval> findByReservation(int reservationId) {
        List<Approval> results = new ArrayList<>();
        String sql = "SELECT * FROM approvals WHERE reservation_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer approverId = (Integer) rs.getObject("approver_id");
                    results.add(new Approval(
                            rs.getInt("id"), rs.getInt("reservation_id"), approverId,
                            Approval.Level.valueOf(rs.getString("level")),
                            Approval.Decision.valueOf(rs.getString("decision")),
                            rs.getString("comments"),
                            LocalDateTime.parse(rs.getString("decided_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch approvals for reservation " + reservationId, e);
        }
        return results;
    }
}
