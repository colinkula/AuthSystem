package com.kulacolin.auth.model;

import java.sql.Timestamp;

public class TrustedSession {
    private int sessionId;
    private int userId;
    private Timestamp createdAt;
    private Timestamp expiresAt;

    public TrustedSession(int sessionId, int userId, Timestamp createdAt, Timestamp expiresAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public int getSessionId() { return sessionId; }
    public int getUserId() { return userId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getExpiresAt() { return expiresAt; }

    public boolean isExpired() {
        return expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }
}
