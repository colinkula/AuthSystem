package com.kulacolin.auth.view;

import com.kulacolin.auth.controller.LoginController;
import com.kulacolin.auth.util.LayoutUtil;
import com.kulacolin.auth.util.CustomFocusTraversalPolicy;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginScreen extends JPanel {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private JCheckBox trustDeviceCheckBox;
    private JButton loginButton;
    private JButton registerButton;
    private JButton changePasswordButton;
    private JLabel statusLabel;

    private LoginController controller;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private GridBagConstraints gbc;

    public LoginScreen(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        setupUI();
        this.controller = new LoginController(this, cardLayout, cardPanel);
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
        addEmailField();
        addPasswordField();
        addShowPasswordToggle();
        addTrustDeviceCheckbox();
        addLoginButton();
        addBottomButtons();
        addStatusLabel();
    }

    private void addTitle() {
        gbc.ipady = 20;
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        LayoutUtil.addComponent(this, gbc, titleLabel, 0, 0, 2);
        gbc.ipady = 0;
    }
    
    private void addEmailField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("Email:"), 0, 1, 1);
        emailField = new JTextField(20);
        LayoutUtil.addComponent(this, gbc, emailField, 1, 1, 1);
    }
    
    private void addPasswordField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("Password:"), 0, 2, 1);
        passwordField = new JPasswordField(20);
        LayoutUtil.addComponent(this, gbc, passwordField, 1, 2, 1);
    }
    
    private void addShowPasswordToggle() {
        showPasswordCheckBox = new JCheckBox("Show Password");
        LayoutUtil.addComponent(this, gbc, showPasswordCheckBox, 1, 3, 1);
    
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('\u2022');
            }
        });
    }
    
    private void addTrustDeviceCheckbox() {
        trustDeviceCheckBox = new JCheckBox("Trust this device for 7 days");
        LayoutUtil.addComponent(this, gbc, trustDeviceCheckBox, 1, 5, 1);
    }
    
    private void addLoginButton() {
        loginButton = new JButton("Login");
        LayoutUtil.addComponent(this, gbc, loginButton, 0, 6, 2);
    }
    
    private void addBottomButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 row, 2 columns, 10px horizontal gap
    
        registerButton = new JButton("Create Account");
        changePasswordButton = new JButton("Change Password");
    
        buttonPanel.add(registerButton);
        buttonPanel.add(changePasswordButton);
    
        LayoutUtil.addComponent(this, gbc, buttonPanel, 0, 7, 2);
    }
        
    private void addStatusLabel() {
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        LayoutUtil.addComponent(this, gbc, statusLabel, 0, 6, 2);
    }
    
    private void setupAccessibility() {
        emailField.getAccessibleContext().setAccessibleName("Email Input");
        passwordField.getAccessibleContext().setAccessibleName("Password Input");
        loginButton.getAccessibleContext().setAccessibleName("Login Button");
        showPasswordCheckBox.getAccessibleContext().setAccessibleName("Show Password Checkbox");
        trustDeviceCheckBox.getAccessibleContext().setAccessibleName("Trust Device Checkbox");
        registerButton.getAccessibleContext().setAccessibleName("Create Account Button");
        changePasswordButton.getAccessibleContext().setAccessibleName("Change Password Button");
        statusLabel.getAccessibleContext().setAccessibleName("Status Message");
        setFocusTraversalPolicy(new CustomFocusTraversalPolicy(
            List.of(emailField, passwordField, showPasswordCheckBox, trustDeviceCheckBox, loginButton, registerButton, changePasswordButton)
        ));
        setFocusTraversalPolicyProvider(true);
    }

    public JButton getLoginButton() { return loginButton; }
    public JButton getRegisterButton() { return registerButton; }
    public JButton getChangePasswordButton() { return changePasswordButton; }
    public String getEmail() { return emailField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()).trim(); }
    public boolean isTrustDeviceChecked() { return trustDeviceCheckBox.isSelected(); }
    
}
