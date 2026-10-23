package com.example;

import com.example.Backend.DatabaseConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTests {
    private final Map<String, String> previous = new HashMap<>();

    @BeforeEach
    void isolateSettings() {
        for (String key : new String[]{"url", "user", "password"}) {
            String property = "transitor.db." + key;
            previous.put(property, System.getProperty(property));
            System.setProperty(property, "");
        }
    }

    @AfterEach
    void restoreSettings() {
        previous.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }

    @Test
    void missingConfigurationDoesNotAttemptAConnection() {
        assertFalse(DatabaseConfig.isConfigured());
        SQLException failure = assertThrows(SQLException.class, DatabaseConfig::openConnection);
        assertEquals("Transit database is not configured.", failure.getMessage());
    }

    @Test
    void acceptsExplicitSettingsWithoutConnecting() {
        System.setProperty("transitor.db.url", "jdbc:mysql://localhost:3306/gtfs");
        System.setProperty("transitor.db.user", "local-development");
        assertTrue(DatabaseConfig.isConfigured());
    }
}
