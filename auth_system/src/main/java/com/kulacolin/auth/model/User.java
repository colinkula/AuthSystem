package com.kulacolin.auth.model;

import java.sql.Timestamp;

public class User {
    private final int id;
    private String email;
    private String passwordHash;
    private boolean emailVerified;
    private Timestamp createdAt;
    private Timestamp deletedAt;

    public User(int id, String email, String passwordHash, boolean emailVerified, Timestamp createdAt, Timestamp deletedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }
    
    // Getters
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean emailVerified() { return emailVerified; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getDeletedAt() { return deletedAt; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setVerified(boolean verified) { this.emailVerified = verified; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', createdAt=" + createdAt + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id == user.id &&
               email.equals(user.email);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(id);
        result = 31 * result + email.hashCode();
        return result;
    }
}
