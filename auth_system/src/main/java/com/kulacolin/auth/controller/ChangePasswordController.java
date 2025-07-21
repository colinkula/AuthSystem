package com.kulacolin.auth.controller;

import com.kulacolin.auth.view.ChangePasswordScreen;
import com.kulacolin.auth.view.VerificationCodeScreen;
import com.kulacolin.auth.dao.*;
import com.kulacolin.auth.service.*;
import com.kulacolin.auth.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChangePasswordController {

    private final ChangePasswordScreen passwordScreen;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final LoginAttemptDAO loginAttemptDAO;
    private final LockoutDAO lockoutDAO;
    private final UserSessionDAO userSessionDAO;
    private final VerificationService verificationService;
    private final LoginService loginService;

    public ChangePasswordController(ChangePasswordScreen passwordScreen, CardLayout cardLayout, JPanel cardPanel) {
        this.passwordScreen = passwordScreen;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;

        this.userDAO = new UserDAO();
        this.userTokenDAO = new UserTokenDAO();
        this.userSessionDAO = new UserSessionDAO();
        this.loginAttemptDAO = new LoginAttemptDAO();
        this.lockoutDAO = new LockoutDAO();

        this.loginService = new LoginService(userDAO, userTokenDAO, userSessionDAO, loginAttemptDAO, lockoutDAO);
        this.verificationService = new VerificationService(userDAO, userTokenDAO, lockoutDAO, loginAttemptDAO);

        initController();
    }

    private void initController() {
        passwordScreen.getSubmitButton().addActionListener(e -> handleChangePassword());
        passwordScreen.getResetInsteadButton().addActionListener(e -> handleResetInstead());
        passwordScreen.getBackButton().addActionListener(e -> handleBack());
    }

    public void handleChangePassword() {
        String email = passwordScreen.getEmail().trim().toLowerCase();
        String currPass = passwordScreen.getCurrentPassword();
        String newPass = passwordScreen.getNewPassword();
        String confirmNewPass = passwordScreen.getConfirmPassword();
        String type = "changePass";
    
        if (email.isEmpty() || currPass.isEmpty() || newPass.isEmpty() || confirmNewPass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields.");
            return;
        }
    
        if (!newPass.equals(confirmNewPass)) {
            JOptionPane.showMessageDialog(null, "New password does not match confirmation.");
            return;
        }
    
        if (confirmNewPass.length() < 6) {
            JOptionPane.showMessageDialog(null, "Password must be at least 6 characters.");
            return;
        }
    
        User user = null;
    
        try {
            user = loginService.authenticateUser(email, currPass);
    
            // Check lockout before proceeding
            if (verificationService.isLockedOut(user, type)) {
                String unlockMessage = formatUnlockTime(verificationService.getLockoutExpiration(user, type));
                JOptionPane.showMessageDialog(null, "<html>Your account is currently locked.<br>" + unlockMessage + "</html>");
                return;
            }
    
            loginService.createLoginAttempt(user, true, type, false);
            verificationService.changePass(user, confirmNewPass);
            JOptionPane.showMessageDialog(null, "Your password has been changed.");
            cardLayout.show(cardPanel, "login");
    
        } catch (com.kulacolin.auth.exception.AuthenticationFailedException e) {
            // If authentication failed, record attempt and check lockout
            try {
                loginService.createLoginAttempt(email, false, type);
                user = loginService.emailExists(email); // get user if exists
    
                if (user != null && verificationService.isLockedOut(user, type)) {
                    String unlockMessage = formatUnlockTime(verificationService.getLockoutExpiration(user, type));
                    JOptionPane.showMessageDialog(null, "<html>Your account is currently locked.<br>" + unlockMessage + "</html>");
                } else {
                    int remaining = verificationService.getAttemptsRemaining(user, type);
                    JOptionPane.showMessageDialog(null, "<html>Invalid email or password.<br>Login attempts remaining: " + remaining + "</html>");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred during login attempt.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An unexpected error occurred.");
        }
    }
    
    public void handleResetInstead() {
        String email = passwordScreen.getEmail().trim().toLowerCase();
        String type = "resetPass";
        User user;

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter an email.");
            return;
        }
        
        try {
            user = loginService.emailExists(email);

            if (user != null) {
                verificationService.sendPassVerification(user);
                JOptionPane.showMessageDialog(null, "A verification code has been sent to confirm your email.");


                Timestamp expiration = verificationService.getTokenExpiration(user, type);  
                VerificationCodeScreen verifyScreen = new VerificationCodeScreen(cardLayout, cardPanel, user, expiration, type);
                cardPanel.add(verifyScreen, "verify");
                cardLayout.show(cardPanel, "verify");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
