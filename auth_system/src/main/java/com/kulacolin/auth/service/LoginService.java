package com.kulacolin.auth.service;

import com.kulacolin.auth.config.AppConfig;
import com.kulacolin.auth.dao.UserDAO;
import com.kulacolin.auth.dao.UserTokenDAO;
import com.kulacolin.auth.dao.UserSessionDAO;
import com.kulacolin.auth.dao.LoginAttemptDAO;
import com.kulacolin.auth.dao.LockoutDAO;
import com.kulacolin.auth.exception.AuthenticationFailedException;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.util.HashUtil;
import com.kulacolin.auth.util.IPUtil;

import jakarta.mail.MessagingException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


public class LoginService {

    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final UserSessionDAO userSessionDAO;
    private final LoginAttemptDAO loginAttemptDAO;
    private final LockoutDAO lockoutDAO;
    private final EmailService emailService;

    public LoginService(UserDAO userDAO, UserTokenDAO userTokenDAO, UserSessionDAO userSessionDAO, LoginAttemptDAO loginAttemptDAO, LockoutDAO lockoutDAO) {
        this.userDAO = userDAO;
        this.userTokenDAO = userTokenDAO;
        this.userSessionDAO = userSessionDAO;
        this.loginAttemptDAO = loginAttemptDAO;
        this.lockoutDAO = lockoutDAO;
        this.emailService = new EmailService(userDAO);
    }

    public User authenticateUser(String email, String rawPassword) throws SQLException, AuthenticationFailedException {
        User user = userDAO.findUserByEmail(email);
    
        if (user == null) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }
    
        String storedHash = userDAO.getHashedPassword(user.getId());
    
        if (storedHash == null || rawPassword == null) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }
    
        if (!HashUtil.argon2Matches(rawPassword, storedHash)) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }
    
        return user;
    }

    public User emailExists(String email) throws SQLException {
        return userDAO.findUserByEmail(email);
    }
    
    public boolean needs2FAVerification(User user) {
        return AppConfig.isEmailVerificationEnabled();
    }

    public void send2FAVerification(User user) throws SQLException, MessagingException {
        String token = emailService.generateVerificationCode();
        Timestamp expiration = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000)); // token existence time

        userTokenDAO.createUserToken(user.getId(), token, "2FA", expiration);
        emailService.sendEmail(user, token, "2FA");
    }

    public boolean isUserVerified(User user) throws SQLException {
        return userDAO.isVerified(user.getId());
    }

    public void updateLastActiveAt(User user) throws SQLException {
        userSessionDAO.updateLastActiveAt(user.getId());
    }

    public void createLoginAttempt(User user, boolean success, String type, boolean passive) throws SQLException {
        String ipAddress = IPUtil.getPublicIP();
        String email = user.getEmail();

        // 1. Check if the user is currently locked out
        boolean isLockedOut = lockoutDAO.isLockedOut(email, type);
        if (isLockedOut) {
            return;
        }

        // 2. Record the login attempt in db
        if (!passive) {
            loginAttemptDAO.createLoginAttempt(user.getId(), email, success, ipAddress, type);
        }

        // 3. If the attempt failed, check recent failure count for rate limiting
        if (!success) {
            int recentFailures = loginAttemptDAO.countFailuresSinceLastSuccess(email, type, Duration.ofMinutes(10));
            
            if (recentFailures >= 3) {
                // Lock the user out for 1 minute (adjust as needed)
                Duration lockoutDuration = Duration.ofMinutes(1);
                Instant lockoutUntilInstant = Instant.now().plus(lockoutDuration);
                Timestamp lockoutUntil = Timestamp.from(lockoutUntilInstant);

                lockoutDAO.setLockout(email, ipAddress, type, lockoutUntil);
            }
        } else {
            // Optional: Clear lockout on successful login
            lockoutDAO.clearLockout(email, type);
        }
    }

    public void createLoginAttempt(String email, boolean success, String type) throws SQLException {
        String ipAddress = IPUtil.getPublicIP();

        // 1. Check if the user is currently locked out
        boolean isLockedOut = lockoutDAO.isLockedOut(email, type);
        if (isLockedOut) {
            return;
        }

        // 2. Record the login attempt
        loginAttemptDAO.createLoginAttempt(null, email, success, ipAddress, type);

        // 3. If the attempt failed, check recent failure count for rate limiting
        if (!success) {
            int recentFailures = loginAttemptDAO.countFailuresSinceLastSuccess(email, type, Duration.ofMinutes(10));

            if (recentFailures >= 3) {
                // Lock the user out for 10 mins
                Duration lockoutDuration = Duration.ofMinutes(10);
                Instant lockoutUntilInstant = Instant.now().plus(lockoutDuration);
                Timestamp lockoutUntil = Timestamp.from(lockoutUntilInstant);

                lockoutDAO.setLockout(email, ipAddress, type, lockoutUntil);
            }
        } else {
            // Optional: Clear lockout on successful login
            lockoutDAO.clearLockout(email, type);
        }
    }

    public Optional<Duration> getRemainingTime(String email, String type) {
        try {
            Optional<Duration> remainingLockout = lockoutDAO.getRemainingLockoutTime(email, type);
            return remainingLockout;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
