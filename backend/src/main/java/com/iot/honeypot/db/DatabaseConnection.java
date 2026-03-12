package com.iot.honeypot.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/iot_honeypot";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static Connection conn;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                // Register JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("MySQL JDBC Driver registered successfully.");
                
                // Try to connect
                System.out.println("Attempting to connect to database at: " + URL);
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully.");
            } catch (SQLException e) {
                System.err.println("Database connection failed: " + e.getMessage());
                throw new RuntimeException("Failed to connect to database", e);
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
                throw new RuntimeException("MySQL JDBC Driver not found", e);
            }
        }
        return conn;
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
                System.out.println("Database connection closed successfully.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    public static boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return false;
        }
    }
}