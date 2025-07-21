package com.kulacolin.auth.model;

import java.time.LocalDateTime;

public class Verification {
    private final int verificationId;
    private final int userId;
    private String token;
    private final LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean verified;
    private String type; // Should be either "2FA" or "password_reset"

    public Verification(int verificationId, int userId, String token,
                        LocalDateTime createdAt, LocalDateTime expiresAt,
                        boolean verified, String type) {
        this.verificationId = verificationId;
        this.userId = userId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verified = verified;
        this.type = type;
    }

    // Getters
    public int getVerificationId() { return verificationId; }
    public int getUserId() { return userId; }
    public String getToken() { return token; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isVerified() { return verified; }
    public String getType() { return type; }

    // Setters
    public void setToken(String token) { this.token = token; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "Verification{id=" + verificationId +
               ", userId=" + userId +
               ", type=" + type +
               ", verified=" + verified +
               ", createdAt=" + createdAt +
               ", expiresAt=" + expiresAt + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Verification)) return false;
        Verification other = (Verification) obj;
        return verificationId == other.verificationId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(verificationId);
    }
}
