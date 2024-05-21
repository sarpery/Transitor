package com.example.Backend;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RequestDatabase {
    private static final double CORD_DISTANCE = 0.1;

    public static ResultSet findNearestStops(double x_cord, double y_cord){
        try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "DacsDacs1-2");

            PreparedStatement nearestStopsRequest = myCon.prepareStatement("SELECT stop_id " +
            "FROM stops_maastricht " +
            "WHERE (stop_lon BETWEEN " + (x_cord - CORD_DISTANCE) + " AND " + (x_cord + CORD_DISTANCE) + ") " +
            "AND (stop_lon BETWEEN " + (y_cord - CORD_DISTANCE) + " AND " + (y_cord + CORD_DISTANCE) + ") ");

            return nearestStopsRequest.executeQuery();
        }
        catch(Exception e)
        {
            System.out.println(e);
            return null;
        }
    }
}
