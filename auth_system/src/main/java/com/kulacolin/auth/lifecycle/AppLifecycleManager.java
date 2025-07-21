package com.kulacolin.auth.lifecycle;

import com.kulacolin.auth.config.DatabaseConnectionPool;

public class AppLifecycleManager {

    public static void initialize() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down application...");
            DatabaseConnectionPool.close();   // Close DB pool
        }));
    }
}
