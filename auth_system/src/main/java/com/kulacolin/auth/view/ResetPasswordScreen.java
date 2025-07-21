package com.kulacolin.auth.view;

import com.kulacolin.auth.controller.ResetPasswordController;
import com.kulacolin.auth.util.LayoutUtil;
import com.kulacolin.auth.util.CustomFocusTraversalPolicy;
import com.kulacolin.auth.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ResetPasswordScreen extends JPanel {

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JButton submitButton;
    private JButton backButton;
    private JLabel statusLabel;

    private User user;
    private ResetPasswordController controller;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private GridBagConstraints gbc;

    public ResetPasswordScreen(CardLayout cardLayout, JPanel cardPanel, User user) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        setupUI();
        this.controller = new ResetPasswordController(this, user, cardLayout, cardPanel);
        setupAccessibility();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;

        addTitle();
        addNewPasswordField();
        addConfirmPasswordField();
        addShowPasswordToggle();
        addSubmitButton();
        addBackButton();
        addStatusLabel();
    }

    private void addTitle() {
        gbc.ipady = 20;
        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        LayoutUtil.addComponent(this, gbc, titleLabel, 0, 0, 2);
        gbc.ipady = 0;
    }

    private void addNewPasswordField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("New Password:"), 0, 1, 1);
        newPasswordField = new JPasswordField(20);
        LayoutUtil.addComponent(this, gbc, newPasswordField, 1, 1, 1);
    }

    private void addConfirmPasswordField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("Confirm Password:"), 0, 2, 1);
        confirmPasswordField = new JPasswordField(20);
        LayoutUtil.addComponent(this, gbc, confirmPasswordField, 1, 2, 1);
    }

    private void addShowPasswordToggle() {
        showPasswordCheckBox = new JCheckBox("Show Passwords");
        LayoutUtil.addComponent(this, gbc, showPasswordCheckBox, 1, 3, 1);

        showPasswordCheckBox.addActionListener(e -> {
            char echoChar = showPasswordCheckBox.isSelected() ? (char) 0 : '\u2022';
            newPasswordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
        });
    }

    private void addSubmitButton() {
        submitButton = new JButton("Submit");
        LayoutUtil.addComponent(this, gbc, submitButton, 0, 4, 2);
    }

    private void addBackButton() {
        backButton = new JButton("Back to Login");
        LayoutUtil.addComponent(this, gbc, backButton, 0, 5, 2);
    }

    private void addStatusLabel() {
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        LayoutUtil.addComponent(this, gbc, statusLabel, 0, 6, 2);
    }

    private void setupAccessibility() {
        newPasswordField.getAccessibleContext().setAccessibleName("New Password Input");
        confirmPasswordField.getAccessibleContext().setAccessibleName("Confirm Password Input");
        showPasswordCheckBox.getAccessibleContext().setAccessibleName("Show Passwords Checkbox");
        submitButton.getAccessibleContext().setAccessibleName("Submit Button");
        backButton.getAccessibleContext().setAccessibleName("Back to Login Button");
        statusLabel.getAccessibleContext().setAccessibleName("Status Message");

        setFocusTraversalPolicy(new CustomFocusTraversalPolicy(
            List.of(newPasswordField, confirmPasswordField, showPasswordCheckBox, submitButton, backButton)
        ));
        setFocusTraversalPolicyProvider(true);
    }

    // Getters for controller access:
    public String getNewPassword() { return new String(newPasswordField.getPassword()).trim(); }
    public String getConfirmPassword() { return new String(confirmPasswordField.getPassword()).trim(); }
    public JButton getSubmitButton() { return submitButton; }
    public JButton getBackButton() { return backButton; }
    public JLabel getStatusLabel() { return statusLabel; }
}
