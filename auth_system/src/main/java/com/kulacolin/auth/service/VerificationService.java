package com.kulacolin.auth.service;

import com.kulacolin.auth.dao.UserDAO;
import com.kulacolin.auth.dao.UserTokenDAO;
import com.kulacolin.auth.dao.LockoutDAO;
import com.kulacolin.auth.dao.LoginAttemptDAO;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.util.EmailUtil;
import com.kulacolin.auth.util.HashUtil;

import jakarta.mail.MessagingException;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;

public class VerificationService {
    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final LockoutDAO lockoutDAO;
    private final LoginAttemptDAO loginAttemptDAO;
    private final EmailService emailService;
    private static final int MAX_ATTEMPTS = 3;

    public VerificationService(UserDAO userDAO, UserTokenDAO userTokenDAO, LockoutDAO lockoutDAO, LoginAttemptDAO loginAttemptDAO) {
        this.userDAO = userDAO;
        this.userTokenDAO = userTokenDAO;
        this.lockoutDAO = lockoutDAO;
        this.loginAttemptDAO = loginAttemptDAO;
        this.emailService = new EmailService(userDAO);
    }

    public boolean verifyUserToken(User user, String rawToken) throws SQLException {
        // get stored token
        String hashedToken = userTokenDAO.getHashedToken(user.getId());

        // null or empty - false
        if (rawToken == null || rawToken.trim().isEmpty()) {
            return false;
        }

        // input doesnt match - false
        if (!HashUtil.sha256Matches(rawToken, hashedToken)) {
            return false;
        }

        // token has expired - false
        if (userTokenDAO.isExpired(user.getId())) {
            return false;
        }

        // already verified - false
        if (userTokenDAO.isVerified(user.getId())) {
            return false;
        }

        // verify
        userTokenDAO.verify(user.getId());

        // Mark user as verified 
        if (!userDAO.isVerified(user.getId())) {
            userDAO.verify(user.getId());
        }

        return true;
    }

    public void resendToken(User user, String type) throws SQLException, MessagingException {
        userTokenDAO.unverify(user.getId(), type);

        String rawToken = generateVerificationCode();
        String hashedToken = HashUtil.sha256(rawToken);
        
        Timestamp expiration = new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000); // 5 mins

        userTokenDAO.saveTokenAndTime(user.getId(), hashedToken, expiration, type);

        String content = "Your " + type + " verification code is: " + rawToken;
        EmailUtil.sendEmail(user.getEmail(), "New Verification Code", content);
    }

    public boolean canResendToken(User user, String type) throws SQLException {
        Timestamp lastSent = userTokenDAO.getLastSentTime(user.getId(), type);
    
        if (lastSent == null) {
            return true; // No record = safe to send
        }
    
        long currentTimeMillis = System.currentTimeMillis();
        long lastSentMillis = lastSent.getTime();
        long elapsedSeconds = (currentTimeMillis - lastSentMillis) / 1000;
    
        return elapsedSeconds >= 10;
    }    

    public void clearOldTokens(User user, String type) throws SQLException {
        userTokenDAO.deleteAllTokensPerType(user.getId(), type);
    }

    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    public void sendEmailVerification(User user) throws SQLException, MessagingException {
        String token = emailService.generateVerificationCode();
        Timestamp expiration = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000)); // token existence time

        userTokenDAO.createUserToken(user.getId(), token, "email", expiration);
        emailService.sendEmail(user, token, "email");
    }

    public void sendPassVerification(User user) throws SQLException, MessagingException {
        String token = emailService.generateVerificationCode();
        Timestamp expiration = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000)); // token existence time

        userTokenDAO.createUserToken(user.getId(), token, "resetPass", expiration);
        emailService.sendEmail(user, token, "resetPass");
    }

    public boolean isLockedOut(User user, String type) {
        try {
            return lockoutDAO.isLockedOut(user.getEmail(), type);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Timestamp getTokenExpiration(User user, String type) throws SQLException {
        return userTokenDAO.getTokenExpiration(user.getId(), type);
    }

    public int getAttemptsRemaining(User user, String type) {
        try {
            if (lockoutDAO.isLockedOut(user.getEmail(), type)) {
                return 0;  // Locked right now → zero attempts
            }
    
            int failures = loginAttemptDAO.countFailuresSinceLastSuccess(user.getEmail(), type, Duration.ofMinutes(10));
            int remaining = MAX_ATTEMPTS - failures;
            return Math.max(0, remaining);
        } catch (Exception e) {
            e.printStackTrace();
            return MAX_ATTEMPTS;  // Fail-safe
        }
    }
            
    public Timestamp getLockoutExpiration(User user, String type) {
        try {
            return lockoutDAO.getLockoutEndTime(user.getEmail(), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }    
    }

    public void changePass(User user, String plainPassword) {
        try {
            userDAO.changePass(user, plainPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
