package com.kulacolin.auth.config;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath");
            }
            props.load(input);
        } catch (IOException e) {
            System.err.println("Error loading config.properties");
            e.printStackTrace();
        }
    }

    public static boolean isEmailVerificationEnabled() {
        return Boolean.parseBoolean(props.getProperty("email.verification.enabled", "true"));
    }
}
