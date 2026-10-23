package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.time.Duration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.example.Backend.DataToHashMap;
import com.example.Backend.Algorithms.Algorithms;

public class AlgorithmsTests {
    Algorithms<String> algorithms;

    @TempDir
    static Path tempDir;
    static Path tempFile;

    @BeforeAll
    public static void init() throws IOException {
        tempFile = Files.createFile(tempDir.resolve("temp.txt"));
        ArrayList<String> lines = new ArrayList<String>();

        lines.add("ALPHA,15,63");
        lines.add("BETA,50.858065604744,5.6940574444944");
        lines.add("GAMMA,50.8577644439073,5.6908605201489");

        Files.write(tempFile, lines);
    }


    @BeforeEach                                         
    void setUp() {
        algorithms = new Algorithms<String>(new DataToHashMap<>(tempFile.toString()));
    }

    @Test                                     
    void test1() {
        assertThrows(NullPointerException.class, () -> algorithms.getDistance("MISSING", "ALPHA"));
    }

    @Test
    void test2() {
        double distance = algorithms.getDistance("GAMMA", "BETA");
        assertTrue(Math.abs(distance - 0.2269)  < 0.0001);
    }

    @Test
    void test3() {
        double distance = algorithms.getDistance("ALPHA", "BETA");
        assertTrue(Math.abs(distance - 6448.0)  < 1);
    }

    @Test
    void test4() {
        double time = algorithms.getCyclingTime("ALPHA", "BETA");
        double expectedTime = 6448 / Algorithms.CYCLING_SPEED;
        assertTrue(time == expectedTime);
    }

    @Test
    void test5() {
        assertTrue(algorithms.getCost() == 0);
    }

    @Test
    void test6() {
        assertThrows(IllegalArgumentException.class, () -> {
            algorithms.parseDuration("HAHAHAH");
        });
    }

    @Test
    void test7() {
        assertThrows(IllegalArgumentException.class, () -> {
            algorithms.parseDuration("");
        });
    }

    @Test
    void test8() {
        assertThrows(NullPointerException.class, () -> {
            algorithms.parseDuration(null);
        });
    }

    @Test
    void test9() {
        assertThrows(IllegalArgumentException.class, () -> {
            algorithms.parseDuration("11:1D:02");
        });
    }

    @Test
    void test10() {
        assertThrows(IllegalArgumentException.class, () -> {
            algorithms.parseDuration("1:2:3:4");
        });
    }

    @Test
    void test11() {
        Duration duration = algorithms.parseDuration("29:0:0");
        assertTrue(duration.getSeconds() == 29 * 3600);
    }

    @Test
    void test12() {
        Duration duration = algorithms.parseDuration("29:3:09");
        assertTrue(duration.getSeconds() == 29 * 3600 + 3 * 60 + 9);
    }

    @Test
    void test13() {
        Duration duration = algorithms.parseDuration("301:05:0");
        assertTrue(duration.getSeconds() == 301 * 3600 + 5 * 60);
    }

    @Test
    void test14() {
        Duration duration = algorithms.parseDuration("0:0:03");
        assertTrue(duration.getSeconds() == 3);
    }

    @Test
    void test15() {
        Duration duration = algorithms.parseDuration("1:0:1");
        assertTrue(duration.getSeconds() == 1 * 3600 + 1);
    }

}
