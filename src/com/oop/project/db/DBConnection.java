package com.oop.project.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/restaurant_pos";
    private static final String DEFAULT_USER = "postgres";

    private static String getSetting(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }

        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }

        return defaultValue;
    }

    public static String getUrl() {
        return getSetting("DB_URL", DEFAULT_URL);
    }

    public static String getUser() {
        return getSetting("DB_USER", DEFAULT_USER);
    }

    public static String getPassword() {
        String password = getSetting("DB_PASSWORD", "");
        if (password.isBlank()) {
            throw new IllegalStateException("DB_PASSWORD is required. Set it as a system property or environment variable.");
        }
        return password;
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(getUrl(), getUser(), getPassword());
        } catch (SQLException e) {
            String safeMessage = String.format(
                    "❌ Cannot connect to database (url=%s, user=%s). Check DB_PASSWORD/DB_USER/DB_URL.",
                    getUrl(),
                    getUser()
            );
            throw new RuntimeException(safeMessage, e);
        }
    }
}
