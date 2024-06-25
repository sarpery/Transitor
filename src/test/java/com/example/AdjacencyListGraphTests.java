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
import com.example.Backend.Graph.AdjacencyListGraph;

public class AdjacencyListGraphTests {
    AdjacencyListGraph<String> graph;

    @BeforeEach
    void setUp() {
        graph = new AdjacencyListGraph<>();
    } 

    @Test
    void test1() {
        graph.addVertex("A");
        graph.addVertex("B");

        graph.addEdge("A", "B", 5);

        assertArrayEquals(graph.getVertecesList().toArray(), new String[] {"A", "B"});
    }

    @Test
    void test2() {
        graph.addVertex("A");
        graph.addVertex("B");

        graph.addEdge("A", "B", 5);

        assertTrue(graph.getEdgeWeight("A", "B") == 5);
    }

    @Test
    void test3() {
        graph.addVertex("Al");
        graph.addVertex("Ba");

        graph.addEdge("Al", "Ba", 5);

        assertTrue(graph.getEdgeWeight("Ba", "Al") == null);
    }

    @Test
    void test4() {
        assertTrue(graph.getEdgeWeight("adskff", "asdasd") == null);
    }

    @Test
    void test5() {
        graph.addVertex("A");
        graph.addVertex("B");

        graph.addEdge("A", "B", 5);
        graph.removeVertex("B");

        assertArrayEquals(graph.getVertecesList().toArray(), new String[] {"A"});
        assertTrue(graph.getEdgeWeight("A", "B") == null);
        assertTrue(graph.neighbors("A").size() == 0);
    }
}
