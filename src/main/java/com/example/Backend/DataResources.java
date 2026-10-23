package com.example.Backend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Opens bundled application data without relying on the working directory. */
public final class DataResources {
    private static final String CLASSPATH_PREFIX = "classpath:";

    private DataResources() {
    }

    public static InputStream open(String location) throws IOException {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("A data location is required.");
        }
        if (!location.startsWith(CLASSPATH_PREFIX)) {
            return Files.newInputStream(Path.of(location));
        }
        String resource = location.substring(CLASSPATH_PREFIX.length());
        if (!resource.startsWith("/")) {
            resource = "/" + resource;
        }
        InputStream stream = DataResources.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new FileNotFoundException("Bundled data was not found: " + resource);
        }
        return stream;
    }

    public static BufferedReader reader(String location) throws IOException {
        return new BufferedReader(new InputStreamReader(open(location), StandardCharsets.UTF_8));
    }
}
