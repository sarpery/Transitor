package com.example.Backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.example.Backend.Graph.AdjacencyListGraph;

public class DatabaseToGraph {
    private static AdjacencyListGraph<String> graph = new AdjacencyListGraph<String>();
    private  static LinkedList<String> bus_stops = new LinkedList<String>();
    private static Connection myCon;

    private static void connect(){
        try{
            
            myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "BCS1510_BCS1510");


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void graphTheData(){
        try{
        connect();
        getVertexes();
        PreparedStatement edgeQuery = myCon.prepareStatement("SELECT * FROM single_bus_rides");
        ResultSet edges = edgeQuery.executeQuery();

        
        getVertexes();
        addVertexes();

        while(edges.next())
        {
            String name = edges.getString("trip_id");
            String start_point = edges.getString("start_stop");
            String end_point = edges.getString("end_stop");
            String departure = edges.getString("departure_time");
            String arrival = edges.getString("arrival_time");
            Integer distance = edges.getInt("distance");

            graph.addEdge(start_point, end_point, arrival, departure,  distance, name);
        }
        } catch(Exception e) { e.printStackTrace(); }
    }
    
    private static void addVertexes(){
        
        for(int i = 0; i < bus_stops.size();i++){

            graph.addVertex(bus_stops.get(i));
        }
    }

    private static void getVertexes() throws SQLException {
        PreparedStatement vertexQuery = myCon.prepareStatement("SELECT stop_id FROM stops_maastricht");

        
        ResultSet vertexes = vertexQuery.executeQuery();

        while(vertexes.next())
        {
            bus_stops.add(vertexes.getString("stop_id"));
        }
    }

    public AdjacencyListGraph<String> getGraph() {
        return graph;
    }

    public static void main(String[] args){
        graphTheData();
        System.out.println(graph.toString());
    }
}