package com.example.Controller;

import com.example.Backend.DataToGraph;
import com.example.Backend.DatabaseToGraph;
import com.example.Backend.Graph.AdjacencyListGraph;

public interface GraphController {
    DataToGraph<String> dg = new DataToGraph<String>();
    DatabaseToGraph dbg = new DatabaseToGraph();


    default void graphTheData(){
        dg.graphTheData("classpath:data/FilteredDistances.txt");
    }

     default void graphTheDatabase(){
        dbg.graphTheDatabase();
    }




    default AdjacencyListGraph<String> getGraph(){
        return dg.getGraph();
    }

}
