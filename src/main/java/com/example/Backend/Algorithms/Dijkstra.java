package com.example.Backend.Algorithms;



import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.example.Backend.Graph.AdjacencyListGraph;
import com.example.Backend.Graph.EdgeNode;

public class Dijkstra<T> {
    
    private AdjacencyListGraph<String> graph;
    
    public Dijkstra(AdjacencyListGraph<String> graph1){

        this.graph = graph1;
    }


    public List<String> dijkstra(String start, String target) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> priorityQueue = new PriorityQueue<>((u, v) -> distance.get(u) - distance.get(v));
        Set<String> visited = new HashSet<>();

        // Initialize distances
        for (String vertex : graph.verteces.keySet()) {
            distance.put(vertex, Integer.MAX_VALUE);
            previous.put(vertex, null);
        }
        distance.put(start, 0);
        priorityQueue.add(start);

        while (!priorityQueue.isEmpty()) {
            String current = priorityQueue.poll();
            if (current.equals(target)) {
                break; // Reached the target
            }
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            for (EdgeNode<String> neighbor : graph.neighbors(current)) {
                String next = neighbor.getName();
                Integer newDistance = distance.get(current) + neighbor.getWeight();
                if (newDistance < distance.get(next)) {
                    distance.put(next, newDistance);
                    previous.put(next, current);
                    priorityQueue.add(next);
                }
            }
        }

        // Reconstruct the path from start to target
        List<String> path = new LinkedList<>();
        String current = target;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        return path;
    }

}
