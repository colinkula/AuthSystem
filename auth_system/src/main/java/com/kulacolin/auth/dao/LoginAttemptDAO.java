package com.kulacolin.auth.dao;

import java.sql.*;
import java.time.Duration;
import com.kulacolin.auth.config.DatabaseConnectionPool;

public class LoginAttemptDAO {

    public void createLoginAttempt(Integer userId, String email, boolean success, String ipAddress, String type) throws SQLException {
        String query = "INSERT INTO login_attempts (user_id, email_entered, success, ip_address, type) VALUES (?, ?, ?, ?, ?)";
    
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
    
            stmt.setString(2, email);
            stmt.setBoolean(3, success);
            stmt.setString(4, ipAddress);
            stmt.setString(5, type);
            stmt.executeUpdate();
        }
    }

    public Timestamp getLastSuccessfulAttempt(String email, String type) throws SQLException {
        String query = "SELECT attempt_time FROM login_attempts " +
                       "WHERE email_entered = ? AND type = ? AND success = 1 " +
                       "ORDER BY attempt_time DESC LIMIT 1";
    
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, type);
    
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("attempt_time");
                }
            }
        }
        return null;  // No successful attempts ever
    }
        
    public int countFailuresSinceLastSuccess(String email, String type, Duration window) throws SQLException {
        Timestamp lastSuccess = getLastSuccessfulAttempt(email, type);
    
        String query;
        if (lastSuccess != null) {
            query = "SELECT COUNT(*) FROM login_attempts " +
                    "WHERE email_entered = ? AND type = ? AND success = 0 " +
                    "AND attempt_time > ? AND attempt_time > (CURRENT_TIMESTAMP - INTERVAL ? SECOND)";
        } else {
            query = "SELECT COUNT(*) FROM login_attempts " +
                    "WHERE email_entered = ? AND type = ? AND success = 0 " +
                    "AND attempt_time > (CURRENT_TIMESTAMP - INTERVAL ? SECOND)";
        }
    
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, type);
            if (lastSuccess != null) {
                stmt.setTimestamp(3, lastSuccess);
                stmt.setLong(4, window.getSeconds());
            } else {
                stmt.setLong(3, window.getSeconds());
            }
    
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
    
        return 0;
    }
    
            
}
