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
    
    private AdjacencyListGraph<T> graph;
    
    public Dijkstra(AdjacencyListGraph<T> graph1){

        this.graph = graph1;
    }


    public List<T> dijkstra(T start, T target) {
        Map<T, Integer> distance = new HashMap<>();
        Map<T, T> previous = new HashMap<>();
        PriorityQueue<T> priorityQueue = new PriorityQueue<>((u, v) -> distance.get(u) - distance.get(v));
        Set<T> visited = new HashSet<>();

        // Initialize distances
        for (T vertex : graph.verteces.keySet()) {
            distance.put(vertex, Integer.MAX_VALUE);
            previous.put(vertex, null);
        }
        distance.put(start, 0);
        priorityQueue.add(start);

        while (!priorityQueue.isEmpty()) {
            T current = priorityQueue.poll();
            if (current.equals(target)) {
                break; // Reached the target
            }
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            for (EdgeNode<T> neighbor : graph.neighbors(current)) {
                T next = neighbor.getName();
                Integer newDistance = distance.get(current) + neighbor.getWeight();
                if (newDistance < distance.get(next)) {
                    distance.put(next, newDistance);
                    previous.put(next, current);
                    priorityQueue.add(next);
                }
            }
        }

        // Reconstruct the path from start to target
        List<T> path = new LinkedList<>();
        T current = target;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        return path;
    }

}
