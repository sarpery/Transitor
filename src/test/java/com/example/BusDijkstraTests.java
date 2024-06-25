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
import com.example.Backend.Algorithms.BusDijkstra;
import com.example.Backend.Algorithms.Dijkstra;
import com.example.Backend.Graph.AdjacencyListGraph;

public class BusDijkstraTests {
    // The graph contains both walking edges and bus edges. Walking edges have special names with 'null:' prefix
    // For walking time between two points the weight from the graph isn't used, and the distance based on data
    // read by Algorithms.java is used instead

    BusDijkstra<String> dijkstra;
    DataToHashMap<String> dataReader;

    @TempDir
    static Path tempCoordsDir;
    static Path tempCoordsFile;
    @TempDir
    static Path tempDistancesDir;
    static Path tempDistancesFile;

    @BeforeAll
    public static void init() throws IOException {
        createCoordsFile();
    }

    private static void createCoordsFile() throws IOException {
        tempCoordsFile = Files.createFile(tempCoordsDir.resolve("temp1.txt"));
        ArrayList<String> lines = new ArrayList<String>();

        lines.add("ALPHA,50.857,5.694");
        lines.add("BETA,50.858065604744,5.6940574444944");
        lines.add("GAMMA,50.8577644439073,5.6908605201489");

        lines.add("Sorbo,50.83336375455239,5.709641862094127");
        lines.add("UMC,50.83544644485597,5.714699777553216");
        lines.add("UMSports,50.835615307564126,5.71053312473009");
        lines.add("MECC,50.83887986649725,5.712226523738419");

        Files.write(tempCoordsFile, lines);
    }

    private AdjacencyListGraph<String> getTheGraph() {
        dataReader = new DataToHashMap<>(tempCoordsFile.toString());
        dataReader.hashMapConverter();
        AdjacencyListGraph<String> graph = new AdjacencyListGraph<>();

        graph.addVertex("ALPHA");
        graph.addVertex("BETA");
        graph.addVertex("GAMMA");
        graph.addVertex("Sorbo");
        graph.addVertex("UMC");
        graph.addVertex("UMSports");
        graph.addVertex("MECC");

        addWalkEdge(graph, "ALPHA", "BETA");
        addWalkEdge(graph, "GAMMA", "BETA");
        addWalkEdge(graph, "GAMMA", "ALPHA");
        addWalkEdge(graph, "GAMMA", "Sorbo");
        addWalkEdge(graph, "MECC", "ALPHA");

        addBusEdge(graph, "1", "Sorbo", "UMC", "12:00:00", "12:15:00");
        addBusEdge(graph, "2", "UMC", "UMSports", "13:00:00", "13:15:00");
        addBusEdge(graph, "3", "UMSports", "MECC", "13:30:00", "13:45:00");
        addBusEdge(graph, "4", "MECC", "UMC", "15:30:00", "15:45:00");
        addBusEdge(graph, "5", "UMSports", "BETA", "7:0:00", "7:05:00");
        addBusEdge(graph, "6", "UMSports", "BETA", "19:0:00", "19:10:00");

        return graph;
    }

    private void addWalkEdge(AdjacencyListGraph<String> graph, String startName, String endName) {
        String name1 = "null:" + startName + "-" + endName;
        String name2 = "null:" + endName + "-" + startName;

        double[] coordsStart = dataReader.getCoordinates(startName);
        double[] coordsEnd = dataReader.getCoordinates(endName);

        String departure = coordsStart[0] + ":" + coordsStart[1];
        String arrival = coordsEnd[0] + ":" + coordsEnd[1];
        Integer distance = 0;

        graph.addEdge(startName, endName, arrival, departure,  distance, name1);
        graph.addEdge(endName, startName, departure, arrival,  distance, name2);
    }

    private void addBusEdge(AdjacencyListGraph<String> graph, String tripID, String startName, String endName, String departureTime, String arrivalTime) {
        graph.addEdge(startName, endName, arrivalTime, departureTime,  0, tripID);
    }

    @BeforeEach
    void setUp() {
        dijkstra = new BusDijkstra<String>(getTheGraph(), new DataToHashMap<>(tempCoordsFile.toString()));
    } 

    @Test
    void test1() {
        List<String> path = dijkstra.dijkstra("Sorbo", "UMSports", "11:55:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 3);
        assertTrue(time == 80);
    }

    @Test
    void test2() {
        List<String> path = dijkstra.dijkstra("Sorbo", "UMSports", "11:49:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 3);
        assertTrue(time == 86);
    }

    @Test
    void test3() {
        List<String> path = dijkstra.dijkstra("BETA", "MECC", "8:30:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 4);
        assertTrue(time == 45);
    }

    @Test
    void test4() {
        assertThrows(Exception.class, () -> {
            dijkstra.dijkstra("Sorbo", "UMC", "16:00:00");
        });
    }

    @Test
    void test5() {
        List<String> path = dijkstra.dijkstra("MECC", "UMC", "00:30:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 5);
        assertTrue(time == 705);
    }

    @Test
    void test6() {
        assertThrows(Exception.class, () -> {
            dijkstra.dijkstra("UMSports", "Beta", "22:00:00");
        });
    }

    @Test
    void test7() {
        assertThrows(Exception.class, () -> {
            dijkstra.dijkstra("Beta", "UMSports", "22:00:00");
        });
    }

    @Test
    void test8() {
        assertThrows(Exception.class, () -> {
            dijkstra.dijkstra("UMC", "MECC", "22:00:00");
        });
    }

    @Test
    void test9() {
        assertThrows(Exception.class, () -> {
            dijkstra.dijkstra("FSKDKDSFKSFD", "MECC", "22:00:00");
        });
    }

    @Test
    void test10() {
        assertThrows(Exception.class, () -> {
            dijkstra.dijkstra("FSKDKDSFKSFD", "ASDKASKDK", "22:00:00");
        });
    }

    @Test
    void test11() {
        List<String> path = dijkstra.dijkstra("ALPHA", "UMSports", "00:55:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 5);
        assertTrue(time == 740);
    }

    @Test
    void test12() {
        List<String> path = dijkstra.dijkstra("UMSports", "BETA", "00:55:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 2);
        assertTrue(time == 370);
    }

    @Test
    void test13() {
        List<String> path = dijkstra.dijkstra("GAMMA", "ALPHA", "19:55:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 3);
        assertTrue(time == 4);
    }

    @Test
    void test14() {
        List<String> path = dijkstra.dijkstra("MECC", "Sorbo", "12:55:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 5);
        assertTrue(time == 102);
    }

    @Test
    void test15() {
        List<String> path = dijkstra.dijkstra("Sorbo", "ALPHA", "24:55:00");
        double time = dijkstra.getTime();

        assertTrue(path.size() == 4);
        assertTrue(time == 59);
    }

}
