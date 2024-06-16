package com.example;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.example.Backend.DataToGraph;
import com.example.Backend.DataToHashMap;
import com.example.Backend.Algorithms.Algorithms;
import com.example.Backend.Algorithms.Dijkstra;

public class DijkstraTests {
    Dijkstra<String> dijkstra;

    @TempDir
    static Path tempCoordsDir;
    static Path tempCoordsFile;
    @TempDir
    static Path tempDistancesDir;
    static Path tempDistancesFile;

    @BeforeAll
    public static void init() throws IOException {
        createCoordsFile();
        createDistancesFile();
    }

    private static void createCoordsFile() throws IOException {
        tempCoordsFile = Files.createFile(tempCoordsDir.resolve("temp1.txt"));
        ArrayList<String> lines = new ArrayList<String>();

        lines.add("ALPHA,15,63");
        lines.add("BETA,50.858065604744,5.6940574444944");
        lines.add("GAMMA,50.8577644439073,5.6908605201489");

        lines.add("Sorbo,50.83336375455239,5.709641862094127");
        lines.add("UMC,50.83544644485597,5.714699777553216");
        lines.add("UMSports,50.835615307564126,5.71053312473009");
        lines.add("MECC,50.83887986649725,5.712226523738419");

        Files.write(tempCoordsFile, lines);
    }

    private static void createDistancesFile() throws IOException {
        tempDistancesFile = Files.createFile(tempDistancesDir.resolve("temp2.txt"));
        ArrayList<String> lines = new ArrayList<String>();
        Algorithms<String> tempAlgorithms = new Algorithms<>(new DataToHashMap<>(tempCoordsFile.toString()));

        lines.add(String.format("ALPHA,(BETA-%f)", tempAlgorithms.getDistance("ALPHA", "BETA")));
        lines.add(String.format("BETA,(GAMMA-%f)", tempAlgorithms.getDistance("BETA", "GAMMA")));

        lines.add(String.format("Sorbo,(UMC-%f),(UMSports-%f)", 
            tempAlgorithms.getDistance("Sorbo", "UMC"),
            tempAlgorithms.getDistance("Sorbo", "UMSports")));
        lines.add(String.format("UMC,(UMSports-%f),(MECC-%f),(Sorbo-%f)", 
            tempAlgorithms.getDistance("UMC", "UMSports"),
            tempAlgorithms.getDistance("UMC", "MECC"),
            tempAlgorithms.getDistance("UMC", "Sorbo")));
        lines.add(String.format("UMSports,(UMC-%f),(MECC-%f),(Sorbo-%f)", 
            tempAlgorithms.getDistance("UMSports", "UMC"),
            tempAlgorithms.getDistance("UMSports", "MECC"),
            tempAlgorithms.getDistance("UMSports", "Sorbo")));
        lines.add(String.format("MECC,(UMSports-%f)", 
            tempAlgorithms.getDistance("MECC", "UMSports")));

        Files.write(tempDistancesFile, lines);
    }

    @BeforeEach
    void setUp() {
        DataToGraph<String> dataToGraph = new DataToGraph<>();
        dataToGraph.graphTheData(tempDistancesFile.toString());

        dijkstra = new Dijkstra(dataToGraph.getGraph());
    } 

    @Test
    void test1() {
        List<String> path = dijkstra.dijkstra("ALPHA", "GAMMA");
        String[] expectedPath = { "ALPHA", "BETA", "GAMMA" };

        testMatch(path, expectedPath);
    }

    @Test
    void test2() {
        List<String> path = dijkstra.dijkstra("ALPHA", "ALPHA");
        String[] expectedPath = { "ALPHA" };

        testMatch(path, expectedPath);
    }

    @Test
    void test3() {
        List<String> path = dijkstra.dijkstra("UMSports", "ALPHA");
        assertTrue(path == null);
    }

    @Test
    void test4() {
        List<String> path = dijkstra.dijkstra("Sorbo", "MECC");
        String[] expectedPath = { "Sorbo", "UMSports", "MECC" };

        testMatch(path, expectedPath);
    }

    @Test
    void test5() {
        List<String> path = dijkstra.dijkstra("MECC", "UMC");
        String[] expectedPath = { "MECC", "UMSports", "UMC" };

        testMatch(path, expectedPath);
    }

    private void testMatch(List<String> path, String[] expectedPath) {
        assertTrue(expectedPath.length == path.size());
        for (int i = 0; i < expectedPath.length; i++) {
            assertTrue(expectedPath[i].equals(path.get(i)));
        }
    }
}
