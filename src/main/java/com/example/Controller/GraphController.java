package com.example.Controller;

import com.example.Backend.DataToGraph;
import com.example.Backend.Graph.AdjacencyListGraph;

public interface GraphController {
    DataToGraph<String> dg = new DataToGraph<String>();


    default void graphTheData(){
        dg.graphTheData();
    }

    default AdjacencyListGraph<String> getGraph(){
        return dg.getGraph();
    }

}
