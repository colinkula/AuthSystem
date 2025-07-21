package com.kulacolin.auth.controller;

import com.kulacolin.auth.dao.*;

import com.kulacolin.auth.exception.AuthenticationFailedException;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.service.LoginService;
import com.kulacolin.auth.service.VerificationService;
import com.kulacolin.auth.view.LoginScreen;
import com.kulacolin.auth.view.VerificationCodeScreen;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Optional;
import java.time.Duration;
import java.sql.Timestamp;

public class LoginController {
    private final LoginScreen loginScreen;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final LoginService loginService;
    private final VerificationService verificationService;
    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final UserSessionDAO userSessionDAO;
    private final LoginAttemptDAO loginAttemptDAO;
    private final LockoutDAO lockoutDAO;


    public LoginController(LoginScreen screen, CardLayout layout, JPanel panel) {
        this.loginScreen = screen;
        this.cardLayout = layout;
        this.cardPanel = panel;

        userDAO = new UserDAO();
        userTokenDAO = new UserTokenDAO();
        userSessionDAO = new UserSessionDAO();
        loginAttemptDAO = new LoginAttemptDAO();
        lockoutDAO = new LockoutDAO();
        this.loginService = new LoginService(userDAO, userTokenDAO, userSessionDAO, loginAttemptDAO, lockoutDAO);
        this.verificationService = new VerificationService(userDAO, userTokenDAO, lockoutDAO, loginAttemptDAO);

        initController();
    }

    private void initController() {
        loginScreen.getLoginButton().addActionListener(e -> handleLogin());
        loginScreen.getRegisterButton().addActionListener(e -> handleRegister());
        loginScreen.getChangePasswordButton().addActionListener(e -> handlePasswordChange());
    }

    public void handleLogin() {
        // get nromalized email
        String email = loginScreen.getEmail().trim().toLowerCase();
        // get raw password
        String rawPassword = loginScreen.getPassword();
        String type = "login";

        // check if either are empty, prompt user
        if (email.isEmpty() || rawPassword.isEmpty()) {
            try {
                loginService.createLoginAttempt(email, false, type);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Please enter both email and password.");
            return;
        }
    
        try {
            // check for lockout and show remaining time if locked
            Optional<Duration> remainingLockout = loginService.getRemainingTime(email, type);
            if (remainingLockout.isPresent()) {
                long secondsLeft = remainingLockout.get().getSeconds();
                long minutes = secondsLeft / 60;
                long seconds = secondsLeft % 60;
                JOptionPane.showMessageDialog(null, String.format(
                    "Your account is locked due to multiple failed login attempts.\nPlease try again in %d minute(s) and %d second(s).",
                    minutes, seconds
                ));
                return;
            }
    
            // authenticate w email and comparing hashed password
            User user = loginService.authenticateUser(email, rawPassword);

            // when they exceed 2 attempts of trying to verify their email token, time them out for 1 min
            // HERE????
            if (!loginService.isUserVerified(user)) {
                JOptionPane.showMessageDialog(null, "Please verify your email before logging in. We've sent you a new code.");

                verificationService.sendEmailVerification(user);
                Timestamp expiration = verificationService.getTokenExpiration(user, "email");  
                VerificationCodeScreen verifyScreen = new VerificationCodeScreen(cardLayout, cardPanel, user, expiration, "email");
                cardPanel.add(verifyScreen, "verify");
                cardLayout.show(cardPanel, "verify");

                // consider this a successful login attempt (email + password correct)
                loginService.createLoginAttempt(user, true, type, false);

                return;
            }
    
            // did they click "Trust this device for 7 days"
            boolean trustDevice = loginScreen.isTrustDeviceChecked();

            // if system wide 2FA is activated
            if (loginService.needs2FAVerification(user)) {
                // if the user clicked the trust this device checkbox or does NOT have a current active session (ephemeral or trusted)
                if (trustDevice || !userSessionDAO.hasActiveSession(user.getId())) {
                    // send 2FA email
                    JOptionPane.showMessageDialog(null, "A 2FA verification code has been sent to your email.");
                    loginService.send2FAVerification(user);
                    Timestamp expiration = verificationService.getTokenExpiration(user, "2FA");  

                    VerificationCodeScreen verifyScreen = new VerificationCodeScreen(cardLayout, cardPanel, user, trustDevice, expiration);
                    cardPanel.add(verifyScreen, "verify");
                    cardLayout.show(cardPanel, "verify");

    
                    // consider this a successful login attempt (email + password correct)
                    loginService.createLoginAttempt(user, true, type, false);
                    return;
                } else {
                    // has active session, skip 2FA
                    loginService.updateLastActiveAt(user);
                    loginService.createLoginAttempt(user, true, type, false);
                    cardLayout.show(cardPanel, "home");
                    return;
                }
            }
    
            // 2FA not required
            loginService.createLoginAttempt(user, true, type, false);
            cardLayout.show(cardPanel, "home");
    
        } catch (AuthenticationFailedException e) {
            try {
                loginService.createLoginAttempt(email, false, type); // safe logging
            } catch (SQLException sqlEx) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An unexpected error occurred during login.");
        }
    }
    
    public void handleRegister() {
        cardLayout.show(cardPanel, "register");
    }

    public void handlePasswordChange() {
        cardLayout.show(cardPanel, "changePass");
    }
}
