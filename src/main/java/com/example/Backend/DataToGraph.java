package com.example.Backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.example.Backend.Graph.AdjacencyListGraph;

public class DataToGraph<T> {
    
    AdjacencyListGraph<String> graph = new AdjacencyListGraph<String>();

    public void graphTheData(){

        

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/com/example/Data/FilteredDistances.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                 // Split the data string into main postal code and pairs of other postal codes with distances
                 String[] parts = line.split(",");
                 String mainPostalCode = parts[0];
                 graph.addVertex(mainPostalCode);
                 for (int i = 1; i < parts.length; i++) {
                     String[] pair = parts[i].replaceAll("[()]", "").split("-");
                     String otherPostalCode = pair[0];
                     double distance = Double.parseDouble(pair[1]);
    
                     double dis = distance*1000;
    
                     int d = (int)dis;
    
                     graph.addVertex(otherPostalCode);
                     graph.addEdge(mainPostalCode, otherPostalCode, d);
                 }
    
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
       
    }

    public AdjacencyListGraph<String> getGraph() {
        return graph;
    }
}
