package com.kulacolin.auth.view;

import com.kulacolin.auth.controller.RegisterController;
import com.kulacolin.auth.util.LayoutUtil;
import com.kulacolin.auth.util.CustomFocusTraversalPolicy;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RegisterScreen extends JPanel {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private JButton registerButton;
    private JButton backButton;
    private JLabel statusLabel;

    private RegisterController controller;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private GridBagConstraints gbc;

    public RegisterScreen(CardLayout cardLayout, JPanel cardPanel) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        setupUI();
        this.controller = new RegisterController(this, cardLayout, cardPanel);
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
        addRegisterButton();
        addBackButton();
        addStatusLabel();
    }

    private void addTitle() {
        gbc.ipady = 20;
        JLabel titleLabel = new JLabel("Create Account");
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
                passwordField.setEchoChar((char) 0);  // show text
            } else {
                passwordField.setEchoChar('\u2022');  // mask with bullet
            }
        });
    }

    private void addRegisterButton() {
        registerButton = new JButton("Register");
        LayoutUtil.addComponent(this, gbc, registerButton, 0, 4, 2);
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
        emailField.getAccessibleContext().setAccessibleName("Email Input");
        passwordField.getAccessibleContext().setAccessibleName("Password Input");
        showPasswordCheckBox.getAccessibleContext().setAccessibleName("Show Password Checkbox");
        registerButton.getAccessibleContext().setAccessibleName("Register Button");
        backButton.getAccessibleContext().setAccessibleName("Back to Login Button");
        statusLabel.getAccessibleContext().setAccessibleName("Status Message");

        setFocusTraversalPolicy(new CustomFocusTraversalPolicy(
            List.of(emailField, passwordField, showPasswordCheckBox, registerButton, backButton)
        ));
        setFocusTraversalPolicyProvider(true);
    }

    // Getters for controller access
    public JButton getRegisterButton() { return registerButton; }
    public JButton getBackButton() { return backButton; }
    public JLabel getStatusLabel() { return statusLabel; }
    public String getEmail() { return emailField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()).trim(); }
}
