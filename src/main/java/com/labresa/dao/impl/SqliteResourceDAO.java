package com.labresa.dao.impl;

import com.labresa.dao.ResourceDAO;
import com.labresa.db.DatabaseConnectionManager;
import com.labresa.model.Resource;
import com.labresa.patterns.ResourceFactory;
import com.labresa.patterns.state.AvailableState;
import com.labresa.patterns.state.ResourceState;
import com.labresa.patterns.state.UnderMaintenanceState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteResourceDAO implements ResourceDAO {

    private Connection conn() {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    private static ResourceState stateFromName(String name) {
        switch (name) {
            case "AVAILABLE": return new AvailableState();
            case "UNDER_MAINTENANCE": return new UnderMaintenanceState();
            default: throw new IllegalArgumentException("Unknown resource state: " + name);
        }
    }

    private Resource mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Resource.Category category = Resource.Category.valueOf(rs.getString("category"));
        int totalQuantity = rs.getInt("total_quantity");
        String detail = rs.getString("category_or_capacity");

        Resource resource = ResourceFactory.create(type, id, name, category, totalQuantity, detail);
        resource.restore(stateFromName(rs.getString("status")), rs.getInt("usage_counter"), rs.getInt("available_quantity"));
        return resource;
    }

    @Override
    public Resource findById(int id) {
        String sql = "SELECT * FROM resources WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find resource " + id, e);
        }
    }

    @Override
    public List<Resource> findAll() {
        List<Resource> results = new ArrayList<>();
        String sql = "SELECT * FROM resources";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch resources", e);
        }
        return results;
    }

    @Override
    public void save(Resource resource) {
        String detail = detailFor(resource);
        String sql = "INSERT INTO resources (name, type, category_or_capacity, category, total_quantity, available_quantity, status, usage_counter, maintenance_threshold) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getName());
            ps.setString(2, resource.getType());
            ps.setString(3, detail);
            ps.setString(4, resource.getCategory().name());
            ps.setInt(5, resource.getTotalQuantity());
            ps.setInt(6, resource.getAvailableQuantity());
            ps.setString(7, resource.getStatus());
            ps.setInt(8, resource.getUsageCounter());
            ps.setInt(9, resource.getMaintenanceThreshold());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    resource.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save resource", e);
        }
    }

    @Override
    public void update(Resource resource) {
        String sql = "UPDATE resources SET name=?, available_quantity=?, status=?, usage_counter=?, maintenance_threshold=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, resource.getName());
            ps.setInt(2, resource.getAvailableQuantity());
            ps.setString(3, resource.getStatus());
            ps.setInt(4, resource.getUsageCounter());
            ps.setInt(5, resource.getMaintenanceThreshold());
            ps.setInt(6, resource.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update resource " + resource.getId(), e);
        }
    }

    @Override
    public List<Resource> findByStatus(String status) {
        List<Resource> results = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE status = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch resources by status " + status, e);
        }
        return results;
    }

    private String detailFor(Resource resource) {
        if ("LAB_ROOM".equals(resource.getType())) {
            return String.valueOf(((com.labresa.model.LabRoom) resource).getCapacity());
        }
        return ((com.labresa.model.Equipment) resource).getEquipmentKind();
    }
}
