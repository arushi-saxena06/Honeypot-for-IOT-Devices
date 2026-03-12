package com.iot.honeypot.service;

import com.iot.honeypot.db.DatabaseConnection;
import com.iot.honeypot.entity.AttackLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AttackService supports two modes:
 *  - DB-backed (when a JDBC connection is available)
 *  - In-memory fallback when DB is unavailable (so UI can run without MySQL)
 */
public class AttackService {

    private final Connection connection;
    private final boolean useDb;
    private final List<AttackLog> inMemoryLogs = Collections.synchronizedList(new ArrayList<>());
    private long nextId = 1;

    public AttackService() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
        } catch (Exception ignored) {
            // DatabaseConnection prints its own errors
        }
        this.connection = conn;
        this.useDb = (this.connection != null);
        if (!useDb) {
            System.out.println("[AttackService] DB unavailable — using in-memory mode.");
        } else {
            System.out.println("[AttackService] Using DB-backed mode.");
        }
    }

    // Overload: callers that don't know port can call this
    public void recordAttack(String protocol, String sourceIp, String payload) {
        recordAttack(protocol, sourceIp, payload, 0);
    }

    public void recordAttack(String protocol, String sourceIp, String payload, int port) {
        if (useDb) {
            String sql = "INSERT INTO attack_logs (protocol, source_ip, payload, port) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, protocol);
                stmt.setString(2, sourceIp);
                stmt.setString(3, payload);
                stmt.setInt(4, port);
                stmt.executeUpdate();
                System.out.println("Attack recorded (DB): " + protocol + " from " + sourceIp);
            } catch (SQLException e) {
                e.printStackTrace();
                // fallback to in-memory if DB write fails
                addInMemory(protocol, sourceIp, payload, port);
            }
        } else {
            addInMemory(protocol, sourceIp, payload, port);
        }
    }

    private void addInMemory(String protocol, String sourceIp, String payload, int port) {
        AttackLog log = new AttackLog(nextId++, new Timestamp(System.currentTimeMillis()), protocol, sourceIp, payload, port);
        inMemoryLogs.add(0, log); // keep most recent at index 0
        System.out.println("Attack recorded (MEM): " + protocol + " from " + sourceIp);
    }

    public List<AttackLog> getAllAttacks() {
        if (useDb) {
            List<AttackLog> logs = new ArrayList<>();
            String sql = "SELECT id, timestamp, protocol, source_ip, payload, port FROM attack_logs ORDER BY timestamp DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    AttackLog log = new AttackLog(
                            rs.getLong("id"),
                            rs.getTimestamp("timestamp"),
                            rs.getString("protocol"),
                            rs.getString("source_ip"),
                            rs.getString("payload"),
                            rs.getInt("port")
                    );
                    logs.add(log);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return logs;
        } else {
            synchronized (inMemoryLogs) {
                return new ArrayList<>(inMemoryLogs);
            }
        }
    }
}
                    }
                }
            }
        }
    }

    public void recordAttack(String protocol, String sourceIp, String payload, int port) throws SQLException {
        String sql = "INSERT INTO attack_logs (protocol, source_ip, payload, port) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, protocol);
            stmt.setString(2, sourceIp);
            stmt.setString(3, payload);
            stmt.setInt(4, port);
            stmt.executeUpdate();

            // Get the inserted record for notification
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    AttackLog log = getAttackLog(id);
                    notifyListeners(log);
                }
            }
        }
    }

    public List<AttackLog> getAllAttacks() throws SQLException {
        List<AttackLog> attacks = new ArrayList<>();
        String sql = "SELECT * FROM attack_logs ORDER BY timestamp DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                attacks.add(new AttackLog(
                    rs.getLong("id"),
                    rs.getTimestamp("timestamp"),
                    rs.getString("protocol"),
                    rs.getString("source_ip"),
                    rs.getString("payload"),
                    rs.getInt("port")
                ));
            }
        }
        return attacks;
    }

    private AttackLog getAttackLog(long id) throws SQLException {
        String sql = "SELECT * FROM attack_logs WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AttackLog(
                        rs.getLong("id"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("protocol"),
                        rs.getString("source_ip"),
                        rs.getString("payload"),
                        rs.getInt("port")
                    );
                }
            }
        }
        throw new SQLException("Attack log not found with id: " + id);
    }

    private void notifyListeners(AttackLog log) {
        for (AttackLogListener listener : listeners) {
            listener.onAttackLogged(log);
        }
    }
}
