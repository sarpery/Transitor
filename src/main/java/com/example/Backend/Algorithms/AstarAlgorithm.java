package com.example.Backend.Algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.example.Backend.Graph.AdjacencyListGraph;
import com.example.Backend.Graph.EdgeNode;

public class AstarAlgorithm<T> {

private AdjacencyListGraph<T> graph;
Algorithms<T> alg = new Algorithms<T>();

public AstarAlgorithm(AdjacencyListGraph<T> graph){

    this.graph = graph;
}



public List<T> aStarSearch(T start, T goal) {
    Map<T, Double> gScore = new HashMap<>();

    // Initialize gScore for start node
    gScore.put(start, 0.0);
    double fScore = gScore.get(start) + heuristicCost(start, goal);

    // Define a comparator using a lambda expression
    Comparator<T> comparator = Comparator.comparingDouble(node -> getTotalCost(node, gScore, goal));

    // Use PriorityQueue constructor with the defined comparator
    PriorityQueue<T> openSet = new PriorityQueue<>(comparator);

    Set<T> closedSet = new HashSet<>();
    Map<T, T> cameFrom = new HashMap<>();

    // Add start node to open set
    openSet.add(start);

    while (!openSet.isEmpty()) {
        T current = openSet.poll();

        if (current.equals(goal)) {
            // Reconstruct path
            return reconstructPath(cameFrom, goal);
        }

        closedSet.add(current);

        for (EdgeNode<T> edge : graph.neighbors(current)) {
            T neighbor = edge.getName();
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

private int heuristicCost(T from, T to) {
    
    
    double dis = 100000 * alg.getDistance(from, to);
    int cal = (int)dis;

    return cal;
}

private List<T> reconstructPath(Map<T, T> cameFrom, T current) {
    List<T> path = new ArrayList<>();
    path.add(current);
    while (cameFrom.containsKey(current)) {
        current = cameFrom.get(current);
        path.add(0, current);
    }
    return path;
}

public double getTotalCost(T element, Map<T, Double> gScore, T goal) {
    return gScore.getOrDefault(element, Double.MAX_VALUE) + heuristicCost(element, goal);
}

}