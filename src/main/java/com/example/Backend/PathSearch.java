package com.example.Backend;

import java.util.List;

import com.example.Backend.Algorithms.AstarAlgorithm;
import com.example.Backend.Algorithms.Dijkstra;
import com.example.Backend.Graph.AdjacencyListGraph;

public class PathSearch<T> {
    AstarAlgorithm<T> as;
    Dijkstra<T> dj;
    

    public List<T> searchAStar(AdjacencyListGraph<T> graph, T start, T target){
         as = new AstarAlgorithm<>(graph);

        return as.aStarSearch(start, target);

    }


    public List<T> dijkstraSearch(AdjacencyListGraph<T> graph1, T start, T target){
        dj = new Dijkstra<>(graph1);

       return dj.dijkstra(start, target);
    }


}
