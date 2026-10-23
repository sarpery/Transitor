package com.example;

import com.example.Frontend.PostcodeInput;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostcodeInputTests {
    @Test
    void acceptsCommonDutchPostcodeFormatting() {
        assertEquals("6211AB", PostcodeInput.requireValid(" 6211 ab ", "departure"));
        assertEquals("6211AS", PostcodeInput.requireValid("6211AS", "destination"));
    }

    @Test
    void rejectsEmptyAndMalformedInputsBeforeRouting() {
        for (String value : new String[]{null, "", "   ", "6211", "6211ABC", "AB6211", "0211AB", "6211-A"}) {
            assertThrows(IllegalArgumentException.class, () -> PostcodeInput.requireValid(value, "departure"));
        }
    }

    @Test
    void explainsWhichFieldNeedsAttention() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PostcodeInput.requireValid("invalid", "destination"));
        assertTrue(error.getMessage().contains("destination"));
        assertTrue(error.getMessage().contains("6211 AB"));
    }
}
