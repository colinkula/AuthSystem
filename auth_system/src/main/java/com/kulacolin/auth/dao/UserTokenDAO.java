package com.kulacolin.auth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.kulacolin.auth.config.DatabaseConnectionPool;
import com.kulacolin.auth.util.HashUtil;

public class UserTokenDAO {

    public void createUserToken(int userId, String token, String type, Timestamp expiresAt) throws SQLException {
        deleteAllTokensPerType(userId, type);
        
        String hashedToken = HashUtil.sha256(token);
        String query = "INSERT INTO user_tokens (user_id, token_hash, type, expires_at) VALUES (?, ?, ?, ?)";
    
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, userId);
            stmt.setString(2, hashedToken);
            stmt.setString(3, type); // use "2FA" or "password_reset"
            stmt.setTimestamp(4, expiresAt); 
    
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // handle better in production
        }
    }
        
    public void saveTokenAndTime(int userId, String hashedToken, Timestamp timestamp, String type) throws SQLException {
        String sql = "UPDATE user_tokens SET token_hash = ?, created_at = ? WHERE user_id = ? AND type = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedToken);
            stmt.setTimestamp(2, timestamp);
            stmt.setInt(3, userId);
            stmt.setString(4, type);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void verify(int userId) throws SQLException {
        String sql = "UPDATE user_tokens SET verified = TRUE WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public void unverify(int userId, String type) throws SQLException {
        String sql = "UPDATE user_tokens SET verified = FALSE WHERE user_id = ? AND type = ?";
        
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.executeUpdate();
        }
    }

    public String getHashedToken(int userId) throws SQLException {
        String query = "SELECT token_hash FROM user_tokens WHERE user_id = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("token_hash");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isExpired(int userId) throws SQLException {
        String query = "SELECT * FROM user_tokens WHERE user_id = ? AND NOW() < expires_at";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                // reutrn true if next
                return !rs.next(); 
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // otherwise return false
        return true;
    }

    public boolean isVerified(int userId) throws SQLException {
        String query = "SELECT verified FROM user_tokens WHERE user_id = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getBoolean("verified");
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }
        
    public void deleteAllTokensPerType(int userId, String type) throws SQLException {
        String sql = "DELETE FROM user_tokens WHERE user_id = ? AND type = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setInt(1, userId);
            stmt.setString(2, type);

            stmt.executeUpdate();
        }
    }     
    
    public Timestamp getLastSentTime(int userId, String type) throws SQLException {
        String query = "SELECT last_sent_at FROM user_tokens WHERE user_id = ? AND type = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("last_sent_at");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }    

    public Timestamp getTokenExpiration(int userId, String type) throws SQLException {
        String query = "SELECT expires_at FROM user_tokens WHERE user_id = ? AND type = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("expires_at");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }
}
