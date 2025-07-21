package com.kulacolin.auth.view;

import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JPanel {
    private JButton logoutButton;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public HomeScreen(CardLayout layout, JPanel panel) {
        this.cardLayout = layout;
        this.cardPanel = panel;
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel welcomeLabel = new JLabel("Welcome to the Home Screen!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> cardLayout.show(cardPanel, "login"));

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(welcomeLabel, gbc);

        gbc.gridy++;
        add(logoutButton, gbc);
    }
}
