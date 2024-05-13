package com.example.Backend.Graph;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class AdjacencyListGraph<T> implements Graph<T> {

    public HashMap<T, List<EdgeNode<T>>> verteces;

    public AdjacencyListGraph() {
        verteces = new HashMap<T, List<EdgeNode<T>>>();
    }


    public List<EdgeNode<T>> neighbors(T x) {
        List<EdgeNode<T>> neighboursElements = new LinkedList<EdgeNode<T>>();
        if (!verteces.containsKey(x)) {
            System.out.println("The vertex " + x + " is not in the Graph.");
        } else {
            List<EdgeNode<T>> adjList = verteces.get(x);
            for (EdgeNode<T> edge : adjList) {
                neighboursElements.add(edge); // Add the EdgeNode itself, not its element
            }
        }
        return neighboursElements;
    }


   


    @Override
    public void addVertex(T x) {
        if (verteces.containsKey(x)) {
           // System.out.println("The vertex " + x + " is already in the Graph.");
        } else {
            List<EdgeNode<T>> newList = new LinkedList<EdgeNode<T>>();
            verteces.put(x, newList);
        }
    }

    @Override
    public void removeVertex(T x) {
        if (!verteces.containsKey(x)) {
           // System.out.println("The vertex " + x + " is not in the Graph.");
        } else {
            verteces.remove(x);
        }
    }

    

    @Override
    public void addEdge(T x, T y, Integer dis) {
        if (!verteces.containsKey(x)) {
            System.out.println("The vertex " + x + " is not in the Graph.");
        } else if (!verteces.containsKey(y)) {
            System.out.println("The vertex " + y + " is not in the Graph.");
        } else {
            List<EdgeNode<T>> adjList = verteces.get(x);
            if (adjList.contains(new EdgeNode<T>(y, dis))) {
               // System.out.println("The edge from " + x + " to " + y + " is already in the graph");
            } else {
                adjList.add(new EdgeNode<T>(y, dis));
            }
        }
    }

    public Integer getEdgeWeight(T source, T target) {
        List<EdgeNode<T>> adjacentVertices = verteces.get(source);
        if (adjacentVertices != null) {
            for (EdgeNode<T> edgeNode : adjacentVertices) {
                if (edgeNode.getElement().equals(target)) {
                    return edgeNode.getWeight();
                }
            }
        }
        return null; // Edge not found or vertices are not connected
    }

    public Integer getEdgeWeightBetweenVertices(T vertex1, T vertex2) {
        Integer weight1 = getEdgeWeight(vertex1, vertex2);
        Integer weight2 = getEdgeWeight(vertex2, vertex1);
        return (weight1 != null) ? weight1 : weight2;
    }

   

    @Override
    public String toString() {
        String result = "";

        for (T vertex : verteces.keySet()) {
            result += vertex + " neighbours: -> ";
            List<EdgeNode<T>> neighbours = verteces.get(vertex);
            for (EdgeNode<T> edge : neighbours) {
                result += edge.getElement() + "->";
            }
            result += "null\n";
        }
        return result;
    }

   

    @Override
    public List<T> getVertecesList() {
        return new LinkedList<T>(verteces.keySet());
    }



    //   public List<T> dijkstra(T start, T target) {
    //     Map<T, Integer> distance = new HashMap<>();
    //     Map<T, T> previous = new HashMap<>();
    //     PriorityQueue<T> priorityQueue = new PriorityQueue<>((u, v) -> distance.get(u) - distance.get(v));
    //     Set<T> visited = new HashSet<>();

    //     // Initialize distances
    //     for (T vertex : verteces.keySet()) {
    //         distance.put(vertex, Integer.MAX_VALUE);
    //         previous.put(vertex, null);
    //     }
    //     distance.put(start, 0);
    //     priorityQueue.add(start);

    //     while (!priorityQueue.isEmpty()) {
    //         T current = priorityQueue.poll();
    //         if (current.equals(target)) {
    //             break; // Reached the target
    //         }
    //         if (visited.contains(current)) {
    //             continue;
    //         }
    //         visited.add(current);

    //         for (EdgeNode<T> neighbor : neighbors(current)) {
    //             T next = neighbor.getElement();
    //             int newDistance = distance.get(current) + neighbor.getWeight();
    //             if (newDistance < distance.get(next)) {
    //                 distance.put(next, newDistance);
    //                 previous.put(next, current);
    //                 priorityQueue.add(next);
    //             }
    //         }
    //     }

    //     // Reconstruct the path from start to target
    //     List<T> path = new LinkedList<>();
    //     T current = target;
    //     while (current != null) {
    //         path.add(0, current);
    //         current = previous.get(current);
    //     }

    //     return path;
    // }


}
