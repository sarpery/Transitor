package com.example.Backend;

import java.util.List;

import com.example.Backend.Algorithms.AstarAlgorithm;
import com.example.Backend.Algorithms.Dijkstra;
import com.example.Backend.Graph.AdjacencyListGraph;

public class PathSearch<T> {
    AstarAlgorithm<T> as;
    Dijkstra<T> dj;
    

    public List<String> searchAStar(AdjacencyListGraph<String> graph, String start, String target){
         as = new AstarAlgorithm<>(graph, new DataToHashMap<T>("src/main/java/com/example/Data/MassZipLatLon.txt"));

        return as.aStarSearch(start, target);

    }


    public List<String> dijkstraSearch(AdjacencyListGraph<String> graph, String start, String target){
        dj = new Dijkstra<>(graph);

       return dj.dijkstra(start, target);
    }


}
