package com.kulacolin.auth.view;

import com.kulacolin.auth.controller.VerificationController;
import com.kulacolin.auth.dao.UserDAO;
import com.kulacolin.auth.model.User;
import com.kulacolin.auth.service.EmailService;
import com.kulacolin.auth.util.LayoutUtil;
import com.kulacolin.auth.util.CustomFocusTraversalPolicy;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.sql.Timestamp;

public class VerificationCodeScreen extends JPanel {

    private JTextField codeField;
    private JButton submitButton;
    private JButton resendButton;
    private JButton backButton;
    private JLabel statusLabel;
    private boolean trustDevice;
    private JLabel timerLabel;
    private Timestamp tokenExpiration;
    private Timer countdownTimer;

    private VerificationController controller;

    private UserDAO userDAO;
    private User user;
    private EmailService emailService;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private GridBagConstraints gbc;

    public VerificationCodeScreen(CardLayout cardLayout, JPanel cardPanel, User user, boolean trustDevice, Timestamp tokenExpiration) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        this.trustDevice = trustDevice;
        this.tokenExpiration = tokenExpiration;
    
        setupUI();
    
        this.controller = new VerificationController(this, user, cardLayout, cardPanel, "2FA");
        setupAccessibility();

        startCountdown();
    }

    public VerificationCodeScreen(CardLayout cardLayout, JPanel cardPanel, User user, Timestamp tokenExpiration, String type) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.user = user;
        this.tokenExpiration = tokenExpiration;
    
        setupUI();
    
        this.controller = new VerificationController(this, user, cardLayout, cardPanel, type);
        setupAccessibility();
    
        startCountdown();
    }
    
    private void setupUI() {
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;

        addTitle();
        addCodeField();
        addSubmitButton();
        addResendButton();
        addBackButton();
        addStatusLabel();
    }

    private void addTitle() {
        gbc.ipady = 20;
    
        JLabel titleLabel = new JLabel("Verification");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
    
        timerLabel = new JLabel("");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timerLabel.setForeground(Color.GRAY);
    
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(timerLabel);
    
        LayoutUtil.addComponent(this, gbc, titlePanel, 0, 0, 2);
        gbc.ipady = 0;
    }
    
    private void addCodeField() {
        LayoutUtil.addComponent(this, gbc, new JLabel("Verification Code:"), 0, 1, 1);
        codeField = new JTextField(10);
        LayoutUtil.addComponent(this, gbc, codeField, 1, 1, 1);
    }

    private void addSubmitButton() {
        submitButton = new JButton("Submit Code");
        LayoutUtil.addComponent(this, gbc, submitButton, 0, 2, 2);
    }

    private void addResendButton() {
        resendButton = new JButton("Resend Code");
        LayoutUtil.addComponent(this, gbc, resendButton, 0, 3, 2);
    }

    private void addBackButton() {
        backButton = new JButton("Back to Login");
        LayoutUtil.addComponent(this, gbc, backButton, 0, 4, 2);
    }

    private void addStatusLabel() {
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        LayoutUtil.addComponent(this, gbc, statusLabel, 0, 5, 2);
    }

    private void setupAccessibility() {
        codeField.getAccessibleContext().setAccessibleName("Verification Code Input");
        submitButton.getAccessibleContext().setAccessibleName("Submit Code Button");
        resendButton.getAccessibleContext().setAccessibleName("Resend Code Button");
        backButton.getAccessibleContext().setAccessibleName("Back to Login Button");
        statusLabel.getAccessibleContext().setAccessibleName("Status Message");

        setFocusTraversalPolicy(new CustomFocusTraversalPolicy(
            List.of(codeField, submitButton, resendButton)
        ));
        setFocusTraversalPolicyProvider(true);
    }

    public String getCode() {
        return codeField.getText().trim();
    }

    public void setStatusMessage(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : new Color(0, 128, 0)); // green for success
    }

    private void startCountdown() {
        if (tokenExpiration == null) return;
    
        // Stop previous timer if running
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    
        long millisLeft = tokenExpiration.getTime() - System.currentTimeMillis();
        final int[] secondsLeft = { (int) (millisLeft / 1000) };
    
        countdownTimer = new Timer(1000, null);
        countdownTimer.addActionListener(e -> {
            if (secondsLeft[0] <= 0) {
                timerLabel.setText("Code expired");
                submitButton.setEnabled(false);
                countdownTimer.stop();
            } else {
                int minutes = secondsLeft[0] / 60;
                int secs = secondsLeft[0] % 60;
                timerLabel.setText(String.format("Valid for: %02d:%02d", minutes, secs));
                secondsLeft[0]--;
            }
        });
    
        submitButton.setEnabled(true);
        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }
    
    public void updateTokenExpiration(Timestamp newExpiration) {
        this.tokenExpiration = newExpiration;
        startCountdown();
    }    

    public JButton getSubmitButton() {return submitButton; }
    public JButton getResendButton() {return resendButton; }
    public JButton getBackButton() { return backButton; }
    public boolean trustDevice() { return trustDevice; }        
    
}
