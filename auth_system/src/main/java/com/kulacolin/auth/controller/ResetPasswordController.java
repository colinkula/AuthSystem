package com.kulacolin.auth.controller;

import com.kulacolin.auth.view.ResetPasswordScreen;
import com.kulacolin.auth.util.LayoutUtil;
import com.kulacolin.auth.dao.*;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.service.*;

import javax.swing.*;
import java.awt.*;


public class ResetPasswordController {

    private final ResetPasswordScreen passwordScreen;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private final User user;

    private final VerificationService verificationService;
    private final UserDAO userDAO;
    private final UserTokenDAO userTokenDAO;
    private final LoginAttemptDAO loginAttemptDAO;
    private final LockoutDAO lockoutDAO;

    public ResetPasswordController(ResetPasswordScreen passwordScreen, User user, CardLayout cardLayout, JPanel cardPanel) {
        this.passwordScreen = passwordScreen;
        this.user = user;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.userDAO = new UserDAO();
        this.userTokenDAO = new UserTokenDAO();
        this.loginAttemptDAO = new LoginAttemptDAO();
        this.lockoutDAO = new LockoutDAO();

        this.verificationService = new VerificationService(userDAO, userTokenDAO, lockoutDAO, loginAttemptDAO);

        initController();
    }

    private void initController() {
        passwordScreen.getSubmitButton().addActionListener(e -> handleChangePassword());
        passwordScreen.getBackButton().addActionListener(e -> handleBack());
    }

    public void handleChangePassword() {
        String newPass = passwordScreen.getNewPassword();
        String confirmPass = passwordScreen.getConfirmPassword();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            LayoutUtil.showMessage(passwordScreen.getStatusLabel(), "Passwords cannot be empty.", false);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            LayoutUtil.showMessage(passwordScreen.getStatusLabel(), "Passwords do not match.", false);
            return;
        }

        if (confirmPass.length() < 6) {
            LayoutUtil.showMessage(passwordScreen.getStatusLabel(), "Password must be at least 6 characters.", false);
            return;
        }

        verificationService.changePass(user, confirmPass);

        JOptionPane.showMessageDialog(null, "Your password has been changed.");

        cardLayout.show(cardPanel, "login");
    }

    public void handleBack() {
        cardLayout.show(cardPanel, "login");
    }
}
