package com.example.Frontend;

import java.util.Locale;

/** Normalizes user input without accepting malformed Dutch postcodes. */
public final class PostcodeInput {
    private PostcodeInput() {
    }

    public static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    public static String requireValid(String value, String fieldName) {
        String postcode = normalize(value);
        if (!postcode.matches("[1-9][0-9]{3}[A-Z]{2}")) {
            throw new IllegalArgumentException("Enter a valid " + fieldName + " postcode, for example 6211 AB.");
        }
        return postcode;
    }
}
