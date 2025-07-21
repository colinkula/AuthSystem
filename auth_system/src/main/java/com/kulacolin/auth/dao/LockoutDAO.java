package com.kulacolin.auth.dao;

import java.sql.*;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;

import com.kulacolin.auth.config.DatabaseConnectionPool;

public class LockoutDAO {
    public void setLockout(String email, String ipAddress, String type, Timestamp lockoutUntil) throws SQLException {
        String query = "INSERT INTO lockouts (email, ip_address, type, lockout_until) " +
                       "VALUES (?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE lockout_until = VALUES(lockout_until);";
    
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setString(1, email);
            stmt.setString(2, ipAddress);
            stmt.setString(3, type);
            stmt.setTimestamp(4, lockoutUntil);
    
            stmt.executeUpdate();
        }
    }
    
    public boolean isLockedOut(String email, String type) throws SQLException {
        String query = "SELECT 1 FROM lockouts " +
                       "WHERE email = ? AND type = ? AND CURRENT_TIMESTAMP < lockout_until";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, type);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // user is locked out if a row exists
            }
        }
    }

    public void clearLockout(String email, String type) throws SQLException {
        String query = "DELETE FROM lockouts WHERE email = ? AND type = ?";
    
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setString(1, email);
            stmt.setString(2, type);
            stmt.executeUpdate(); 
        }
    }
    
    public Optional<Duration> getRemainingLockoutTime(String email, String type) throws SQLException {
    String query = "SELECT lockout_until FROM lockouts WHERE email = ? AND type = ? AND CURRENT_TIMESTAMP < lockout_until";

    try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, email);
        stmt.setString(2, type);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Timestamp lockoutUntil = rs.getTimestamp("lockout_until");
                Instant now = Instant.now();
                Instant until = lockoutUntil.toInstant();

                if (until.isAfter(now)) {
                    return Optional.of(Duration.between(now, until));
                }
            }
        }
    }

    return Optional.empty(); // not locked out
}

public Timestamp getLockoutEndTime(String email, String type) throws SQLException {
    String query = "SELECT lockout_until FROM lockouts WHERE email = ? AND type = ? ORDER BY lockout_until DESC LIMIT 1";

    try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, email);
        stmt.setString(2, type);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getTimestamp("lockout_until");
            }
        }
    }
    return null;
}


    
}
