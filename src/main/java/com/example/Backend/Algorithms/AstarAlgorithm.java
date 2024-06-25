package com.example.Backend.Algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.example.Backend.DataToHashMap;
import com.example.Backend.Graph.AdjacencyListGraph;
import com.example.Backend.Graph.EdgeNode;

public class AstarAlgorithm<T> {

private AdjacencyListGraph<String> graph;
Algorithms<String> alg;

public AstarAlgorithm(AdjacencyListGraph<String> graph){
    this.graph = graph;
    alg = new Algorithms<>();
}

public AstarAlgorithm(AdjacencyListGraph<String> graph, DataToHashMap<String> dataReader) {
    this.graph = graph;
    alg = new Algorithms<String>(dataReader);
}



public List<String> aStarSearch(String start, String goal) {
    Map<String, Double> gScore = new HashMap<>();

    // Initialize gScore for start node
    gScore.put(start, 0.0);
    double fScore = gScore.get(start) + heuristicCost(start, goal);

    // Define a comparator using a lambda expression
    Comparator<String> comparator = Comparator.comparingDouble(node -> getTotalCost(node, gScore, goal));

    // Use PriorityQueue constructor with the defined comparator
    PriorityQueue<String> openSet = new PriorityQueue<>(comparator);

    Set<String> closedSet = new HashSet<>();
    Map<String, String> cameFrom = new HashMap<>();

    // Add start node to open set
    openSet.add(start);

    while (!openSet.isEmpty()) {
        String current = openSet.poll();

        if (current.equals(goal)) {
            // Reconstruct path
            return reconstructPath(cameFrom, goal);
        }

        closedSet.add(current);

        for (EdgeNode<String> edge : graph.neighbors(current)) {
            String neighbor = edge.getName();
            if (closedSet.contains(neighbor)) {
                continue; // Neighbor already evaluated
            }

            double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + edge.getWeight();

            if (!openSet.contains(neighbor)) {
                openSet.add(neighbor);
            } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                continue; // This is not a better path
            }

            // This path is the best until now. Record it!
            cameFrom.put(neighbor, current);
            gScore.put(neighbor, tentativeGScore);
            openSet.add(neighbor);
        }
    }

    // Open set is empty but goal was never reached
    return null;
}

private int heuristicCost(String from, String to) {
    
    
    double dis = 100000 * alg.getDistance(from, to);
    int cal = (int)dis;

    return cal;
}

private List<String> reconstructPath(Map<String, String> cameFrom, String current) {
    List<String> path = new ArrayList<>();
    path.add(current);
    while (cameFrom.containsKey(current)) {
        current = cameFrom.get(current);
        path.add(0, current);
    }
    return path;
}

public double getTotalCost(String element, Map<String, Double> gScore, String goal) {
    return gScore.getOrDefault(element, Double.MAX_VALUE) + heuristicCost(element, goal);
}

}
