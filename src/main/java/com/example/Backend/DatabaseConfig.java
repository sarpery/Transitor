package com.example.Backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Manual settings take precedence over the app-managed local database. */
public final class DatabaseConfig {
    private static volatile ConnectionSettings automaticSettings;

    private DatabaseConfig() {
    }

    private static String setting(String property, String variable) {
        String value = System.getProperty(property);
        return value != null ? value : System.getenv(variable);
    }

    static record ConnectionSettings(String url, String user, String password) {
        boolean isComplete() {
            return url != null && !url.isBlank() && user != null && !user.isBlank() && password != null;
        }

        boolean isPresent() {
            return url != null || user != null || password != null;
        }

        // Avoid accidentally exposing credentials in diagnostic logging.
        @Override
        public String toString() {
            return "ConnectionSettings[redacted]";
        }
    }

    private static ConnectionSettings currentSettings() {
        ConnectionSettings manual = new ConnectionSettings(
                setting("transitor.db.url", "TRANSITOR_DB_URL"),
                setting("transitor.db.user", "TRANSITOR_DB_USER"),
                setting("transitor.db.password", "TRANSITOR_DB_PASSWORD"));
        return manual.isPresent() ? manual : automaticSettings;
    }

    static void useAutomatic(ConnectionSettings settings) {
        automaticSettings = settings;
    }

    public static boolean isConfigured() {
        ConnectionSettings settings = currentSettings();
        return settings != null && settings.isComplete();
    }

    public static Connection openConnection() throws SQLException {
        return openConnection(currentSettings());
    }

    static Connection openConnection(ConnectionSettings settings) throws SQLException {
        if (settings == null || !settings.isComplete()) {
            throw new SQLException("Transit database is not configured.");
        }
        Properties properties = new Properties();
        properties.setProperty("user", settings.user());
        properties.setProperty("password", settings.password());
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "15000");
        return DriverManager.getConnection(settings.url(), properties);
    }
}
