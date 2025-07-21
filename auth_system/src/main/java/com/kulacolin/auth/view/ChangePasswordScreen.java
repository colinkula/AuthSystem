package com.kulacolin.auth.view;

import com.kulacolin.auth.controller.ChangePasswordController;
import com.kulacolin.auth.util.LayoutUtil;
import com.kulacolin.auth.util.CustomFocusTraversalPolicy;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChangePasswordScreen extends JPanel {

    private JTextField emailField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPasswordsCheckBox;
    private JButton submitButton;
    private JButton resetInsteadButton;
    private JLabel statusLabel;
    private JButton backButton;

    private ChangePasswordController controller;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private GridBagConstraints gbc;

    public ChangePasswordScreen(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        setupUI();
        this.controller = new ChangePasswordController(this, cardLayout, cardPanel);
        setupAccessibility();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;

        addTitle();
        addEmailField();
        addCurrentPasswordField();
        addNewPasswordField();
        addConfirmPasswordField();
        addShowPasswordsToggle();
        addSubmitAndResetButtons();
        addBackButton();
        addStatusLabel();
    }

    private void addTitle() {
        gbc.ipady = 20;
        JLabel titleLabel = new JLabel("Change Your Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        LayoutUtil.addComponent(this, gbc, titleLabel, 0, 0, 2);
        gbc.ipady = 0;
    }

    private void addEmailField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("Email:"), 0, 1, 1);
        emailField = new JTextField(20);
        LayoutUtil.addComponent(this, gbc, emailField, 1, 1, 1);
    }

    private void addCurrentPasswordField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("Current Password:"), 0, 2, 1);
        currentPasswordField = new JPasswordField(20);
        LayoutUtil.addComponent(this, gbc, currentPasswordField, 1, 2, 1);
    }

    private void addNewPasswordField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("New Password:"), 0, 3, 1);
        newPasswordField = new JPasswordField(20);
        LayoutUtil.addComponent(this, gbc, newPasswordField, 1, 3, 1);
    }

    private void addConfirmPasswordField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("Confirm New Password:"), 0, 4, 1);
        confirmPasswordField = new JPasswordField(20);
        LayoutUtil.addComponent(this, gbc, confirmPasswordField, 1, 4, 1);
    }

    private void addShowPasswordsToggle() {
        showPasswordsCheckBox = new JCheckBox("Show Passwords");
        LayoutUtil.addComponent(this, gbc, showPasswordsCheckBox, 1, 5, 1);

        showPasswordsCheckBox.addActionListener(e -> {
            char echoChar = showPasswordsCheckBox.isSelected() ? (char) 0 : '\u2022';
            currentPasswordField.setEchoChar(echoChar);
            newPasswordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
        });
    }

    private void addSubmitAndResetButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 2 buttons side by side with spacing
    
        submitButton = new JButton("Submit");
        resetInsteadButton = new JButton("Reset Password Instead");
    
        // Match button widths to the wider one
        Dimension buttonSize = resetInsteadButton.getPreferredSize();
        submitButton.setPreferredSize(buttonSize);
    
        buttonPanel.add(submitButton);
        buttonPanel.add(resetInsteadButton);
    
        LayoutUtil.addComponent(this, gbc, buttonPanel, 0, 6, 2);
    }
            
    private void addBackButton() {
        backButton = new JButton("Back to Login");
        LayoutUtil.addComponent(this, gbc, backButton, 0, 7, 2);
    }

    private void addStatusLabel() {
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        LayoutUtil.addComponent(this, gbc, statusLabel, 0, 8, 2);
    }    
        
    private void setupAccessibility() {
        emailField.getAccessibleContext().setAccessibleName("Email Input");
        currentPasswordField.getAccessibleContext().setAccessibleName("Current Password Input");
        newPasswordField.getAccessibleContext().setAccessibleName("New Password Input");
        confirmPasswordField.getAccessibleContext().setAccessibleName("Confirm New Password Input");
        showPasswordsCheckBox.getAccessibleContext().setAccessibleName("Show Passwords Checkbox");
        submitButton.getAccessibleContext().setAccessibleName("Submit Button");
        resetInsteadButton.getAccessibleContext().setAccessibleName("Reset Password Instead Button");
        backButton.getAccessibleContext().setAccessibleName("Back to Login Button");
        statusLabel.getAccessibleContext().setAccessibleName("Status Message");

        setFocusTraversalPolicy(new CustomFocusTraversalPolicy(
            List.of(emailField, currentPasswordField, newPasswordField, confirmPasswordField,
                showPasswordsCheckBox, submitButton, resetInsteadButton, backButton)
        ));
        setFocusTraversalPolicyProvider(true);
    }

    // Getters for controller access:
    public String getEmail() { return emailField.getText().trim(); }
    public String getCurrentPassword() { return new String(currentPasswordField.getPassword()).trim(); }
    public String getNewPassword() { return new String(newPasswordField.getPassword()).trim(); }
    public String getConfirmPassword() { return new String(confirmPasswordField.getPassword()).trim(); }
    public JButton getSubmitButton() { return submitButton; }
    public JButton getResetInsteadButton() { return resetInsteadButton; }
    public JButton getBackButton() { return backButton; }
    public JLabel getStatusLabel() { return statusLabel; }

    public void setStatusMessage(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : new Color(0, 128, 0)); // green for success
    }
}
