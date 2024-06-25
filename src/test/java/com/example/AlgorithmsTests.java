package com.example;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

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

}
