package com.example.Controller;

import java.util.List;

import com.example.Backend.PathSearch;
import com.example.Backend.Algorithms.AstarAlgorithm;
import com.example.Backend.Graph.AdjacencyListGraph;

public interface SearchConnector {

    PathSearch<String> as = new PathSearch<String>();
    

    default List<String> searchAstar(AdjacencyListGraph<String> graph, String start, String target){

        return as.searchAStar(graph, start, target);

    }

    default List<String> searchDijkstra(AdjacencyListGraph<String> graph, String start, String target){

        return as.dijkstraSearch(graph, start, target);

    }

    
} 
