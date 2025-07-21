package com.kulacolin.auth.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConnectionPool {

    // double checked locking
    private static volatile HikariDataSource dataSource;

    private DatabaseConnectionPool() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (dataSource == null) {
                    init();  // safe
                }
            }
        }
        return dataSource;
    }

    private static void init() {
        try (InputStream input = DatabaseConnectionPool.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath");
            }

            Properties props = new Properties();
            props.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("DB_URL"));
            config.setUsername(props.getProperty("DB_USERNAME"));
            config.setPassword(props.getProperty("DB_PASSWORD"));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

        } catch (IOException e) {
            System.err.println("Error loading config.properties");
            e.printStackTrace();
        }
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
