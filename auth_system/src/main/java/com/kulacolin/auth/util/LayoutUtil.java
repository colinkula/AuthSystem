package com.kulacolin.auth.util;

import java.awt.*;

import javax.swing.*;

public class LayoutUtil {

    public static void addComponent(JPanel panel, GridBagConstraints gbc, Component comp, int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        panel.add(comp, gbc);
    }

    public static void showMessage(JLabel label, String message, boolean success) {
        label.setText(message);
        label.setForeground(success ? new Color(0, 128, 0) : Color.RED);
    }
}
