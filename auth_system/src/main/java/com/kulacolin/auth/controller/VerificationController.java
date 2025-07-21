package com.kulacolin.auth.controller;

import com.kulacolin.auth.dao.*;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.service.*;
import com.kulacolin.auth.view.ResetPasswordScreen;
import com.kulacolin.auth.view.VerificationCodeScreen;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VerificationController {

    private final VerificationCodeScreen verificationCodeScreen;
    private final User user;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final String type;
    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final UserSessionDAO userSessionDAO;
    private final LoginAttemptDAO loginAttemptDAO;
    private final LockoutDAO lockoutDAO;
    private final VerificationService verificationService;
    private final SessionService sessionService;
    private final LoginService loginService;

    public VerificationController(VerificationCodeScreen screen, User user, CardLayout layout, JPanel panel, String type) {
        this.verificationCodeScreen = screen;
        this.user = user;
        this.cardLayout = layout;
        this.cardPanel = panel;
        this.type = type;
        this.userDAO = new UserDAO();
        this.userTokenDAO = new UserTokenDAO();
        this.userSessionDAO = new UserSessionDAO();
        this.loginAttemptDAO = new LoginAttemptDAO();
        this.lockoutDAO = new LockoutDAO();

        this.verificationService = new VerificationService(userDAO, userTokenDAO, lockoutDAO, loginAttemptDAO);
        this.sessionService = new SessionService(userDAO, userTokenDAO);
        this.loginService = new LoginService(userDAO, userTokenDAO, userSessionDAO, loginAttemptDAO, lockoutDAO);

        initController();
    }

    private void initController() {
        verificationCodeScreen.getSubmitButton().addActionListener(e -> handleSubmit());
        verificationCodeScreen.getResendButton().addActionListener(e -> handleResendCode());
        verificationCodeScreen.getBackButton().addActionListener(e -> handleBack());
    }

    public void handleSubmit() {
        verificationCodeScreen.setStatusMessage("", false);

        String rawToken = verificationCodeScreen.getCode();

        if (rawToken.isEmpty()) {
            int remaining = verificationService.getAttemptsRemaining(user, type);
            verificationCodeScreen.setStatusMessage(
                "<html>Please enter the verification code.<br>Login attempts remaining: " + remaining + "</html>",
                true
            );
            return;
        }

        if (verificationService.isLockedOut(user, type)) {
            String unlockMessage = formatUnlockTime(verificationService.getLockoutExpiration(user, type));
            verificationCodeScreen.setStatusMessage(
                "<html>Your account is currently locked.<br>" + unlockMessage + "</html>",
                true
            );
            return;
        }

        boolean success = false;
        try {
            success = verificationService.verifyUserToken(user, rawToken);
            loginService.createLoginAttempt(user, success, type, false);
        } catch (Exception e) {
            try {
                loginService.createLoginAttempt(user, false, type, false);
            } catch (Exception inner) {
                inner.printStackTrace();
            }
            e.printStackTrace();
        }

        int remaining = verificationService.getAttemptsRemaining(user, type);

        if (success) {
            try {
                verificationService.clearOldTokens(user, type);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (type.equals("email")) {
                verificationCodeScreen.setStatusMessage("<html>Email Verification successful!</html>", false);
                cardLayout.show(cardPanel, "login");
            } else if (type.equals("2FA")) {
                boolean trustDevice = verificationCodeScreen.trustDevice();
                sessionService.createSession(user, trustDevice);
                verificationCodeScreen.setStatusMessage("<html>2FA Verification successful!</html>", false);
                cardLayout.show(cardPanel, "home");
            } else if (type.equals("resetPass")) {
                verificationCodeScreen.setStatusMessage("<html>Password Token Verification successful!</html>", false);
                ResetPasswordScreen passScreen = new ResetPasswordScreen(cardLayout, cardPanel, user);
                cardPanel.add(passScreen, "resetPass");
                cardLayout.show(cardPanel, "resetPass");
        }
        } else {
            if (remaining <= 0) {
                String unlockMessage = formatUnlockTime(verificationService.getLockoutExpiration(user, type));
                verificationCodeScreen.setStatusMessage(
                    "<html>Your account is currently locked.<br>" + unlockMessage + "</html>",
                    true
                );
            } else {
                verificationCodeScreen.setStatusMessage(
                    "<html>Invalid or expired code.<br>Login attempts remaining: " + remaining + "</html>",
                    true
                );
            }
        }
    }

    public void handleResendCode() {
        verificationCodeScreen.setStatusMessage("", false);

        try {
            if (!verificationService.canResendToken(user, type)) {
                verificationCodeScreen.setStatusMessage(
                    "<html>Please wait before requesting a new code.</html>",
                    true
                );
                return;
            }

            verificationService.clearOldTokens(user, type);

            if (type.equals("email")) {
                verificationService.sendEmailVerification(user);
            } else if (type.equals("2FA")) {
                loginService.send2FAVerification(user);
            } else if (type.equals("resetPass")) {
                verificationService.sendPassVerification(user);
            }

            Timestamp newExpiration = verificationService.getTokenExpiration(user, type);
            verificationCodeScreen.updateTokenExpiration(newExpiration);

            String unlockMessage = formatUnlockTime(verificationService.getLockoutExpiration(user, type));

            if (verificationService.isLockedOut(user, type)) {
                verificationCodeScreen.setStatusMessage(
                    "<html>New code sent to your email.<br>Your account is currently locked.<br>" + unlockMessage + "</html>",
                    false
                );    
            } else {
                verificationCodeScreen.setStatusMessage(
                    "<html>New code sent to your email.</html>",
                    false
                );    
            }
        } catch (Exception e) {
            int remaining = verificationService.getAttemptsRemaining(user, type);
            verificationCodeScreen.setStatusMessage(
                "<html>Failed to resend code. Try again later.<br>Login attempts remaining: " + remaining + "</html>",
                true
            );
            e.printStackTrace();
        }
    }

    public void handleBack() {
        cardLayout.show(cardPanel, "login");
    }

    private String formatUnlockTime(Timestamp unlockTime) {
        if (unlockTime == null) return "";
    
        LocalDateTime localDateTime = unlockTime.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm:ss a");
        String formattedTime = localDateTime.format(formatter);
        return " Locked until: " + formattedTime;
    }
}
