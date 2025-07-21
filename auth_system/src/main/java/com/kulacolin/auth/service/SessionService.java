package com.kulacolin.auth.service;

import com.kulacolin.auth.dao.UserDAO;
import com.kulacolin.auth.dao.UserTokenDAO;
import com.kulacolin.auth.dao.UserSessionDAO;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.util.HashUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SessionService {
    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final UserSessionDAO userSessionDAO;

    public SessionService(UserDAO userDAO, UserTokenDAO userTokenDAO) {
        this.userDAO = userDAO;
        this.userTokenDAO = userTokenDAO;
        this.userSessionDAO = new UserSessionDAO();
    }

    public void createSession(User user, boolean trustDevice) {

        try {
            // get all session info
            int userId = user.getId();

            String hashedToken = HashUtil.sha256(UUID.randomUUID().toString());
            Timestamp expiresAt;
            boolean isEphemeral;

            // clicked checkbox 
            if (trustDevice) {
                // trusted: 7-day session
                expiresAt = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
                isEphemeral = false;
            // did not click checkbox
            } else {
                // ephemeral: expires on shutdown
                expiresAt = Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)); // or any short time
                isEphemeral = true;
            }

            String deviceName = System.getProperty("os.name");
            String ipAddress = null; // Add actual IP logic if needed
            String userAgent = System.getProperty("os.version");

            userSessionDAO.createUserSession(userId, hashedToken, expiresAt, deviceName, ipAddress, userAgent, isEphemeral);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearEphemeralSessions(User user) {
        try {
            userSessionDAO.deleteEphemeralSessions(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
