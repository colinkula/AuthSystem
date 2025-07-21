package com.kulacolin.auth;

import com.kulacolin.auth.lifecycle.AppLifecycleManager;
import com.kulacolin.auth.view.*;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        AppLifecycleManager.initialize(); // sets up scheduler + shutdown hooks, temp off
        SwingUtilities.invokeLater(Main::createAndShowLoginScreen); // starts app
    }

    private static void createAndShowLoginScreen() {
        JFrame frame = new JFrame("Lyric Lounge");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);

        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        cardPanel.add(new LoginScreen(cardLayout, cardPanel), "login");
        cardPanel.add(new RegisterScreen(cardLayout, cardPanel), "register");
        cardPanel.add(new HomeScreen(cardLayout, cardPanel), "home");
        cardPanel.add(new ChangePasswordScreen(cardLayout, cardPanel), "changePass");

        frame.setContentPane(cardPanel);
        frame.setVisible(true);
    }
}
