package com.kulacolin.auth.dao;

import com.kulacolin.auth.config.DatabaseConnectionPool;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.util.HashUtil;
import com.kulacolin.auth.exception.UserAlreadyExistsException;

import java.sql.*;

public class UserDAO {
    // get user based on the users provided email
    public User findUserByEmail(String email) throws SQLException {
        String query = "SELECT user_id, email, password_hash, email_verified, created_at, deleted_at FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String passwordHash = rs.getString("password_hash");
                    boolean isVerified = rs.getBoolean("email_verified");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    Timestamp deletedAt = rs.getTimestamp("deleted_at");
    
                    return new User(id, email, passwordHash, isVerified, createdAt, deletedAt);
                }
            }
        }
        return null;
    }

    public User createUser(String email, String plainPassword) throws SQLException, UserAlreadyExistsException {
        if (emailExists(email)) {
            throw new UserAlreadyExistsException("Email is already registered.");
        }
    
        String hashedPassword = HashUtil.argon2(plainPassword);
        String insertQuery = "INSERT INTO users (email, password_hash) VALUES (?, ?)";
    
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
    
            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
        }
    
        // After successful insert, fetch and return the full user object
        return findUserByEmail(email);

    }
    
    // check if email exists 
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
        
    public boolean isVerified(int userId) throws SQLException {
        String query = "SELECT email_verified FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getBoolean("email_verified");
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public void verify(int userId) throws SQLException {
        String sql = "UPDATE users SET email_verified = TRUE WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            stmt.executeUpdate();
        }
    }

    public String getHashedPassword(int userId) throws SQLException {
        String query = "SELECT password_hash FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return rs.getString("password_hash");
            } else {
                // No password found for this userId
                return null;
            }
        } 
    }

    public void changePass(User user, String plainPassword) throws SQLException {
        int userId = user.getId();
        String hashedPassword = HashUtil.argon2(plainPassword);

        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
        
            stmt.executeUpdate();
        }

    }
}
