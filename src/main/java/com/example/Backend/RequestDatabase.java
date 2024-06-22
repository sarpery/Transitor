package com.example.Backend;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.example.Backend.Algorithms.BusDijkstra;
import com.example.Backend.Graph.Trip;;

public class RequestDatabase<T> {
    private static final double CORD_DISTANCE_STAP = 0.001;
    private static Connection myCon;
    DataToHashMap<String> dataReader = new DataToHashMap<String>();

    public RequestDatabase(){
        try{
        Class.forName("com.mysql.cj.jdbc.Driver");
        myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "BCS1510_BCS1510");
        }catch(Exception e){ System.out.println(e); }
    }

    private static ResultSet findNearestStopsDinamic(double x_cord, double y_cord){
        try
        {
            double searchRadius = 0;
            Statement nearestStopsRequest = myCon.createStatement();
            ResultSet stops;
            do
            {
                stops = nearestStopsRequest.executeQuery("SELECT COUNT(*) " +
                "FROM stops_maastricht " +
                "WHERE (stop_lan BETWEEN " + (x_cord - searchRadius) + " AND " + (x_cord + searchRadius) + ") " +
                "AND (stop_lon BETWEEN " + (y_cord - searchRadius) + " AND " + (y_cord + searchRadius) + ") ");
                stops.next();

                searchRadius += CORD_DISTANCE_STAP;
            } while(stops.getInt(1) < 10);


            return nearestStopsRequest.executeQuery("SELECT stop_id " +
            "FROM stops_maastricht " +
            "WHERE (stop_lan BETWEEN " + (x_cord - searchRadius) + " AND " + (x_cord + searchRadius) + ") " +
            "AND (stop_lon BETWEEN " + (y_cord - searchRadius) + " AND " + (y_cord + searchRadius) + ") ");
        }
        catch(Exception e)
        {
            System.out.println(e);
            return null;
        }
    }

    DatabaseToGraph dbg = new DatabaseToGraph();
    BusDijkstra<String> bs = new BusDijkstra<String>(dbg.getGraph());
    private Trip computeBusRoute(String startPoint, String endPoint){
        Trip trip;
        
        
        List<String> tripList = bs.dijkstra(startPoint, endPoint);
        long tripTime = bs.getTime();
        System.out.println("dijkstra done");
        trip = new Trip(tripList, tripTime);
        System.out.println("trip construct done");
        return trip;
    }

    public ResultSet getShape(String tripID){
        try{
        PreparedStatement shapeQuery = myCon.prepareStatement("SELECT shape_pt_sequence as sequence, shape_pt_lat as lan, shape_pt_lon as lon, shape_dist_travelled as distance " +
            "FROM shapes_maastricht WHERE shape_id IN " +
            "(SELECT shape_id FROM trips_maastricht WHERE trip_id = '" + tripID + "')");
        return shapeQuery.executeQuery();
        } catch(Exception e){e.printStackTrace(); return null;}
    }

    public double[] getPointCords(String pointID){
        try{
            PreparedStatement shapeQuery = myCon.prepareStatement("SELECT lon, lan " +
                "FROM points WHERE point_id = '" + pointID + "'");
            ResultSet cords =  shapeQuery.executeQuery();
            cords.next();

            double[] ptCords = new double[2];
            ptCords[0] = cords.getDouble("lan");
            ptCords[1] = cords.getDouble("lon");

            return ptCords;
        } catch(Exception e){e.printStackTrace(); return null;}
    }

    public Trip findBusRoute(String startPostalCode, String endPostalCode){
        return computeBusRoute(startPostalCode, endPostalCode);
    }
}