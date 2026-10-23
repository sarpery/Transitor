package com.example.Backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Optional local transit database settings. No credentials are stored in source. */
public final class DatabaseConfig {
    private DatabaseConfig() {
    }

    private static String setting(String property, String variable) {
        String value = System.getProperty(property);
        return value != null ? value : System.getenv(variable);
    }

    public static boolean isConfigured() {
        String url = setting("transitor.db.url", "TRANSITOR_DB_URL");
        String user = setting("transitor.db.user", "TRANSITOR_DB_USER");
        String password = setting("transitor.db.password", "TRANSITOR_DB_PASSWORD");
        return url != null && !url.isBlank() && user != null && !user.isBlank() && password != null;
    }

    public static Connection openConnection() throws SQLException {
        if (!isConfigured()) {
            throw new SQLException("Transit database is not configured.");
        }
        Properties properties = new Properties();
        properties.setProperty("user", setting("transitor.db.user", "TRANSITOR_DB_USER"));
        properties.setProperty("password", setting("transitor.db.password", "TRANSITOR_DB_PASSWORD"));
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "15000");
        return DriverManager.getConnection(setting("transitor.db.url", "TRANSITOR_DB_URL"), properties);
    }
}
