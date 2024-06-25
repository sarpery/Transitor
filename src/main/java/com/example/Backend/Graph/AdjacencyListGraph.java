package com.example.Backend.Graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AdjacencyListGraph<T> implements Graph<T> {

    public HashMap<T, List<EdgeNode<T>>> verteces;

    public AdjacencyListGraph() {
        verteces = new HashMap<T, List<EdgeNode<T>>>();
    }


    public List<EdgeNode<T>> neighbors(T x) {
        List<EdgeNode<T>> neighboursElements = new LinkedList<EdgeNode<T>>();
        if (!verteces.containsKey(x)) {

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

        } else {
            List<EdgeNode<T>> newList = new LinkedList<EdgeNode<T>>();
            verteces.put(x, newList);
        }
    }

    @Override
    public void removeVertex(T x) {
        if (verteces.containsKey(x)) {
            removeIngoingEdges(x);
            verteces.remove(x);
        }
    }

    

    @Override
    public void addEdge(T x, String y, Integer dis) {
        if (!verteces.containsKey(x)) {
            System.out.println("The vertex " + x + " is not in the Graph.");
        } else if (!verteces.containsKey(y)) {
            System.out.println("The vertex " + y + " is not in the Graph.");
        } else {
            List<EdgeNode<T>> adjList = verteces.get(x);
            if (adjList.contains(new EdgeNode<T>(y, dis))) {

            } else {
                adjList.add(new EdgeNode<T>(y, dis));
            }
        }
    }


     public void addEdge(T x, T y, String arr, String dep, int dis, String name) {
        if (!verteces.containsKey(x)) {

        } else if (!verteces.containsKey(y)) {
            System.out.println("The vertex " + y + " is not in the Graph.");
        } else {
            List<EdgeNode<T>> adjList = verteces.get(x);
            if (adjList.contains(new EdgeNode<T>(y, name, arr, dep, dis))) {

            } else {
                adjList.add(new EdgeNode<T>(y, name, arr, dep, dis));
            }
        }
    }

    public Integer getEdgeWeight(T source, T target) {
        List<EdgeNode<T>> adjacentVertices = verteces.get(source);
        if (adjacentVertices != null) {
            for (EdgeNode<T> edgeNode : adjacentVertices) {
                if (edgeNode.getName().equals(target)) {
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
                result += edge.getName() + "->";
            }
            result += "null\n";
        }
        return result;
    }

   

    @Override
    public List<T> getVertecesList() {
        return new LinkedList<T>(verteces.keySet());
    }

    private void removeIngoingEdges(T vertex) {
        for (Map.Entry<T, List<EdgeNode<T>>> entry : verteces.entrySet()) {
            List<EdgeNode<T>> adj = entry.getValue();
            adj.remove(new EdgeNode<T>(vertex.toString(), null));
        }
    }
}
