package com.example.Backend;

import java.io.BufferedReader;
import java.io.IOException;

import com.example.Backend.Graph.AdjacencyListGraph;

public class DataToGraph<T> {
    
    AdjacencyListGraph<String> graph = new AdjacencyListGraph<String>();

    public void graphTheData(String fileName){

        

        try (BufferedReader reader = DataResources.reader(fileName)) {
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
            throw new java.io.UncheckedIOException("Could not load application data: " + fileName, e);
        }
       
    }

    public AdjacencyListGraph<String> getGraph() {
        return graph;
    }
}
