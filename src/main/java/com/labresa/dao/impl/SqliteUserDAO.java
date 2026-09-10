package com.labresa.dao.impl;

import com.labresa.dao.UserDAO;
import com.labresa.db.DatabaseConnectionManager;
import com.labresa.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteUserDAO implements UserDAO {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("session"),
                rs.getString("class_roll"),
                User.Role.valueOf(rs.getString("role"))
        );
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user " + id, e);
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> results = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch users", e);
        }
        return results;
    }

    @Override
    public void save(User user, String email, String passwordHash) {
        String sql = "INSERT INTO users (first_name, last_name, session, class_roll, email, password_hash, role, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getSession());
            ps.setString(4, user.getClassRoll());
            ps.setString(5, email);
            ps.setString(6, passwordHash);
            ps.setString(7, user.getRole().name());
            ps.setString(8, java.time.LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public boolean verifyPassword(String email, String rawPassword) {
        String sql = "SELECT password_hash FROM users WHERE email = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String hash = rs.getString("password_hash");
                return org.mindrot.jbcrypt.BCrypt.checkpw(rawPassword, hash);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify password for " + email, e);
        }
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check email existence for " + email, e);
        }
    }

    @Override
    public void updatePassword(String email, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password for " + email, e);
        }
    }
}
