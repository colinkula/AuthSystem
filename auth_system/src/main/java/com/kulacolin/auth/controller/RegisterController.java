package com.kulacolin.auth.controller;

import com.kulacolin.auth.dao.*;
import com.kulacolin.auth.exception.UserAlreadyExistsException;
import com.kulacolin.auth.view.RegisterScreen;
import com.kulacolin.auth.view.VerificationCodeScreen;
import com.kulacolin.auth.util.LayoutUtil;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.service.VerificationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Timestamp;

public class RegisterController {

    private final RegisterScreen registerScreen;
    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final LockoutDAO lockoutDAO;
    private final LoginAttemptDAO loginAttemptDAO;
    private final VerificationService verificationService;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public RegisterController(RegisterScreen registerScreen, CardLayout layout, JPanel panel) {
        this.registerScreen = registerScreen;
        this.cardLayout = layout;
        this.cardPanel = panel;
        this.userDAO = new UserDAO();
        this.userTokenDAO = new UserTokenDAO();
        this.lockoutDAO = new LockoutDAO();
        this.loginAttemptDAO = new LoginAttemptDAO();
        this.verificationService = new VerificationService(userDAO, userTokenDAO, lockoutDAO, loginAttemptDAO);
        initController();
    }

    private void initController() {
        registerScreen.getRegisterButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegistration();
            }
        });

        registerScreen.getBackButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBack();
            }
        });

    }

    public void handleRegistration() {
        String email = registerScreen.getEmail().trim().toLowerCase();
        String password = registerScreen.getPassword();

        if (!isValidEmailPass(email, password)) { 
            return;
        }

        try {
            // create accout based on email and password
            User user = userDAO.createUser(email, password);

            // send user to verification screen
            verificationService.sendEmailVerification(user);
            Timestamp expiration = verificationService.getTokenExpiration(user, "email"); 
            JOptionPane.showMessageDialog(null, "An email verification code has been sent to your email.");
            
            VerificationCodeScreen verifyScreen = new VerificationCodeScreen(cardLayout, cardPanel, user, expiration, "email");
            cardPanel.add(verifyScreen, "verify");
            cardLayout.show(cardPanel, "verify");

        } catch (UserAlreadyExistsException e) {
            LayoutUtil.showMessage(registerScreen.getStatusLabel(), e.getMessage(), false);
        } catch (SQLException e) {
            LayoutUtil.showMessage(registerScreen.getStatusLabel(), "Database error. Please try again later.", false);
            e.printStackTrace();
        } catch (Exception e) {
            LayoutUtil.showMessage(registerScreen.getStatusLabel(), "Unexpected error. Please try again.", false);
        }
    }

    public boolean isValidEmailPass(String email, String pass) {
        if (email.isEmpty() || pass.isEmpty()) {
            LayoutUtil.showMessage(registerScreen.getStatusLabel(), "Email and password are required.", false);
            return false;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            LayoutUtil.showMessage(registerScreen.getStatusLabel(), "Invalid email format.", false);
            return false;
        }

        if (pass.length() < 6) {
            LayoutUtil.showMessage(registerScreen.getStatusLabel(), "Password must be at least 6 characters.", false);
            return false;
        }

        return true;
    }

    public void handleBack() {
        cardLayout.show(cardPanel, "login");
    }
}
