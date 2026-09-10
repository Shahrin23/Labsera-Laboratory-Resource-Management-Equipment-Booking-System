package com.labresa.db;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the schema (matching the ER diagram, Round 4 revision) if it
 * doesn't exist yet, and seeds demo users and resources.
 */
public class DatabaseSeeder {

    public static void run() {
        Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        try (Statement st = conn.createStatement()) {
            createSchema(st);
            if (isEmpty(st, "users")) seedUsers(st);
            if (isEmpty(st, "resources")) seedResources(st);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private static void createSchema(Statement st) throws SQLException {
        st.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "session TEXT, " +
                "class_roll TEXT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "created_at TEXT NOT NULL)");

        // cost removed per requirement; category (COMMON/SPECIAL) and quantity added
        st.execute("CREATE TABLE IF NOT EXISTS resources (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "category_or_capacity TEXT, " +
                "category TEXT NOT NULL, " +
                "total_quantity INTEGER NOT NULL, " +
                "available_quantity INTEGER NOT NULL, " +
                "status TEXT NOT NULL, " +
                "usage_counter INTEGER NOT NULL, " +
                "maintenance_threshold INTEGER NOT NULL)");

        st.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "resource_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "requester_role TEXT NOT NULL, " +
                "start_time TEXT NOT NULL, " +
                "end_time TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "recurring INTEGER NOT NULL DEFAULT 0, " +
                "notes TEXT, " +
                "priority_tag TEXT, " +
                "quantity INTEGER NOT NULL DEFAULT 1, " +
                "supervisor_name TEXT, " +
                "supervisor_role TEXT, " +
                "letter_reference TEXT, " +
                "requested_at TEXT NOT NULL, " +
                "FOREIGN KEY (resource_id) REFERENCES resources(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");

        st.execute("CREATE TABLE IF NOT EXISTS approvals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reservation_id INTEGER NOT NULL, " +
                "approver_id INTEGER, " +
                "level TEXT NOT NULL, " +
                "decision TEXT NOT NULL, " +
                "comments TEXT, " +
                "decided_at TEXT NOT NULL, " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(id), " +
                "FOREIGN KEY (approver_id) REFERENCES users(id))");

        st.execute("CREATE TABLE IF NOT EXISTS usage_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reservation_id INTEGER UNIQUE NOT NULL, " +
                "check_in TEXT NOT NULL, " +
                "check_out TEXT, " +
                "condition_notes TEXT, " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(id))");

        st.execute("CREATE TABLE IF NOT EXISTS maintenance_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "resource_id INTEGER NOT NULL, " +
                "technician_id INTEGER, " +
                "start_date TEXT NOT NULL, " +
                "end_date TEXT, " +
                "reason TEXT NOT NULL, " +
                "notes TEXT, " +
                "FOREIGN KEY (resource_id) REFERENCES resources(id), " +
                "FOREIGN KEY (technician_id) REFERENCES users(id))");

        st.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "reservation_id INTEGER, " +
                "message TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "is_read INTEGER NOT NULL DEFAULT 0, " +
                "created_at TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(id))");
    }

    private static boolean isEmpty(Statement st, String table) throws SQLException {
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM " + table)) {
            rs.next();
            return rs.getInt("cnt") == 0;
        }
    }

    private static void seedUsers(Statement st) throws SQLException {
        String now = java.time.LocalDateTime.now().toString();
        insertUser(st, "Alice", "Rahman", "2021-25", "21-001", "alice@labresa.edu", "password123", "UNDERGRAD", now);
        insertUser(st, "Ben", "Chowdhury", "2019-23", "GR-014", "ben@labresa.edu", "password123", "GRAD", now);
        insertUser(st, "Carol", "Islam", "", "FAC-002", "carol@labresa.edu", "password123", "FACULTY", now);
        insertUser(st, "Dave", "Karim", "", "TECH-005", "dave@labresa.edu", "password123", "TECHNICIAN", now);
        insertUser(st, "Erin", "Hasan", "", "ADM-001", "erin@labresa.edu", "password123", "ADMIN", now);
    }

    private static void insertUser(Statement st, String firstName, String lastName, String session, String classRoll,
                                    String email, String rawPassword, String role, String now) throws SQLException {
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        st.execute(String.format(
                "INSERT INTO users (first_name, last_name, session, class_roll, email, password_hash, role, created_at) " +
                        "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                firstName, lastName, session, classRoll, email, hash, role, now));
    }

    private static void seedResources(Statement st) throws SQLException {
        st.execute("INSERT INTO resources (name, type, category_or_capacity, category, total_quantity, available_quantity, status, usage_counter, maintenance_threshold) VALUES " +
                "('Basic Microscope', 'EQUIPMENT', 'MICROSCOPE', 'COMMON', 5, 5, 'AVAILABLE', 0, 50)");
        st.execute("INSERT INTO resources (name, type, category_or_capacity, category, total_quantity, available_quantity, status, usage_counter, maintenance_threshold) VALUES " +
                "('Chemistry Testing Kit', 'EQUIPMENT', 'TESTING_KIT', 'COMMON', 10, 10, 'AVAILABLE', 0, 15)");
        st.execute("INSERT INTO resources (name, type, category_or_capacity, category, total_quantity, available_quantity, status, usage_counter, maintenance_threshold) VALUES " +
                "('Electron Microscope', 'EQUIPMENT', 'MICROSCOPE', 'SPECIAL', 1, 1, 'AVAILABLE', 0, 50)");
        st.execute("INSERT INTO resources (name, type, category_or_capacity, category, total_quantity, available_quantity, status, usage_counter, maintenance_threshold) VALUES " +
                "('Prusa 3D Printer', 'EQUIPMENT', '3D_PRINTER', 'SPECIAL', 2, 2, 'AVAILABLE', 0, 30)");
        st.execute("INSERT INTO resources (name, type, category_or_capacity, category, total_quantity, available_quantity, status, usage_counter, maintenance_threshold) VALUES " +
                "('Wet Lab Room A', 'LAB_ROOM', '20', 'SPECIAL', 1, 1, 'AVAILABLE', 0, 200)");
    }
}
