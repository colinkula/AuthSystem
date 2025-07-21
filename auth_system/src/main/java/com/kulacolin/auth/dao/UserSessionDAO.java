package com.kulacolin.auth.dao;

import java.sql.*;
import com.kulacolin.auth.config.DatabaseConnectionPool;

public class UserSessionDAO {

    // Create session with optional ephemeral flag
    public void createUserSession(int userId, String sessionToken, Timestamp expiresAt,
                                     String deviceName, String ipAddress, String userAgent, boolean isEphemeral) throws SQLException {
        deleteExpiredSessions(userId); // Optional: Clean up before inserting

        String query = "INSERT INTO user_sessions (user_id, session_token_hash, expires_at, device_name, ip_address, user_agent, is_ephemeral) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setString(2, sessionToken);
            stmt.setTimestamp(3, expiresAt);
            stmt.setString(4, deviceName);
            stmt.setString(5, ipAddress);
            stmt.setString(6, userAgent);
            stmt.setBoolean(7, isEphemeral);

            stmt.executeUpdate();
        }
    }

    // Clean up expired sessions
    public void deleteExpiredSessions(int userId) throws SQLException {
        String query = "DELETE FROM user_sessions WHERE user_id = ? AND expires_at < NOW()";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // Delete all ephemeral sessions (called on shutdown or logout)
    public void deleteEphemeralSessions(int userId) throws SQLException {
        String query = "DELETE FROM user_sessions WHERE user_id = ? AND is_ephemeral = true";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // Check for any active session (trusted or not)
    public boolean hasActiveSession(int userId) throws SQLException {
        String query = "SELECT 1 FROM user_sessions WHERE user_id = ? AND expires_at > NOW() ORDER BY expires_at DESC LIMIT 1";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void updateLastActiveAt(int userId) throws SQLException {
        String query = "UPDATE user_sessions SET last_active_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}
