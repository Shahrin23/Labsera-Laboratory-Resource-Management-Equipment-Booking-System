package com.labresa.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton pattern: exactly one connection to the local SQLite database
 * should exist for the whole application. Every DAO obtains its connection
 * through DatabaseConnectionManager.getInstance().getConnection() rather than
 * opening its own, so connection lifecycle is managed in one place.
 */
public final class DatabaseConnectionManager {

    private static final String DB_URL = "jdbc:sqlite:labresa.db";
    private static DatabaseConnectionManager instance;

    private final Connection connection;

    private DatabaseConnectionManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not connect to SQLite database at " + DB_URL, e);
        }
    }

    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}