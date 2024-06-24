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

    private  void connect(){
        try{
            
            myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "root", "1234");


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public  void graphTheDatabase(){
        try{
        connect();
        PreparedStatement busEdgeQuery = myCon.prepareStatement("SELECT * FROM single_bus_rides");
        ResultSet busEdges = busEdgeQuery.executeQuery();

        
        getVertexes();
        addVertexes();

        while(busEdges.next())
        {
            String name = busEdges.getString("trip_id");
            String start_point = busEdges.getString("start_stop");
            String end_point = busEdges.getString("end_stop");
            String departure = busEdges.getString("departure_time");
            String arrival = busEdges.getString("arrival_time");
            Integer distance = busEdges.getInt("distance");

            graph.addEdge(start_point, end_point, arrival, departure,  distance, name);
        }

        PreparedStatement walkEdgeQuery = myCon.prepareStatement("SELECT * FROM walking_ages");
        ResultSet walkEdges = walkEdgeQuery.executeQuery();

        while(walkEdges.next())
        {
            String name1 = "null:" + walkEdges.getString("pt1") + "-" + walkEdges.getString("pt2");
            String name2 = "null:" + walkEdges.getString("pt2") + "-" + walkEdges.getString("pt1");
            String start_point = walkEdges.getString("pt1");
            String end_point = walkEdges.getString("pt2");
            String departure = walkEdges.getDouble("pt1_lan") + ":" + walkEdges.getDouble("pt1_lon");
            String arrival = walkEdges.getDouble("pt2_lan") + ":" + walkEdges.getDouble("pt2_lon");
            Integer distance = 0;

            graph.addEdge(start_point, end_point, arrival, departure,  distance, name1);
            graph.addEdge(end_point, start_point, departure, arrival,  distance, name2);
        }
        } catch(Exception e) { e.printStackTrace(); }
    }
    
    private  void addVertexes(){
        
        for(int i = 0; i < bus_stops.size();i++){

            graph.addVertex(bus_stops.get(i));
        }
    }

    private  void getVertexes() throws SQLException {
        PreparedStatement vertexQuery = myCon.prepareStatement("SELECT point_id FROM points");

        
        ResultSet vertexes = vertexQuery.executeQuery();

        while(vertexes.next())
        {
            bus_stops.add(vertexes.getString("point_id"));
        }


    }

    public  AdjacencyListGraph<String> getGraph() {
        return graph;
    }

    // public static void main(String[] args){
    //     graphTheDatabase();
    //     BusDijkstra<String> bs = new BusDijkstra<String>(getGraph());
    //      System.out.println(bs.dijkstra("6229VP", "6218TL"));
    // }
}