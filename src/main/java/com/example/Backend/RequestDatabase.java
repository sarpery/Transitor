package com.example.Backend;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.ArrayList;

import com.example.Backend.Algorithms.Algorithms;
import com.example.Backend.Graph.Trip;

public class RequestDatabase {
    private static final double CORD_DISTANCE = 0.01;
    DataToHashMap<String> dataReader = new DataToHashMap<String>();

    private static ResultSet findNearestStops(double x_cord, double y_cord){
        try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "DacsDacs1-2");

            PreparedStatement nearestStopsRequest = myCon.prepareStatement("SELECT stop_id " +
            "FROM stops_maastricht " +
            "WHERE (stop_lan BETWEEN " + (x_cord - CORD_DISTANCE) + " AND " + (x_cord + CORD_DISTANCE) + ") " +
            "AND (stop_lon BETWEEN " + (y_cord - CORD_DISTANCE) + " AND " + (y_cord + CORD_DISTANCE) + ") ");

            return nearestStopsRequest.executeQuery();
        }
        catch(Exception e)
        {
            System.out.println(e);
            return null;
        }
    }

    private static Trip computeBusRoute(double[] startPoint, double[] endPoint){
        Trip trip;
        String resultTripID = "";
        String resultTripName = "";
        double distance = 0;

        ArrayList<double[]> resultList = new ArrayList<double[]>();

        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "DacsDacs1-2");


            ResultSet startPoints = findNearestStops(startPoint[0], startPoint[1]);
            ResultSet endPoints = findNearestStops(endPoint[0], endPoint[1]);

            String startString = "(";
            String endString = "(";
            while(startPoints.next()) startString += "'" + startPoints.getString(1) + "', ";
            while(endPoints.next()) endString += "'" + endPoints.getString(1) + "', ";
            startString = startString.substring(0, startString.length() - 2);
            endString = endString.substring(0, endString.length() - 2);
            startString += ")";
            endString += ")";


            String currentTime = LocalTime.now().toString();

            PreparedStatement nearestStopsRequest = myCon.prepareStatement(
                "SELECT stm1.trip_id, stm1.stop_id as departure_point, stm2.stop_id as arrival_point, stm1.departure_time, stm2.arrival_time, stm1.fare_units_traveled as start_distance, stm2.fare_units_traveled as end_distance " +
                "FROM (SELECT * FROM stop_times_maastricht WHERE stop_id IN " + startString + ") as stm1 " +
                "JOIN (SELECT * FROM stop_times_maastricht WHERE stop_id IN " + endString  +") as stm2 " +
                "ON stm1.trip_id = stm2.trip_id and stm1.stop_sequence < stm2.stop_sequence and stm1.departure_time >= '" + currentTime +"'");

            ResultSet trips = nearestStopsRequest.executeQuery();
            long bestTime = Integer.MAX_VALUE;

            int c = 0;
            while(trips.next())
            {
                String startStop = trips.getString("departure_point");
                String endStop = trips.getString("arrival_point");

                PreparedStatement startCordsQuery = myCon.prepareStatement("SELECT stop_lon, stop_lan " +
                "FROM stops " +
                "WHERE stop_id = " + startStop);
                PreparedStatement endCordsQuery = myCon.prepareStatement("SELECT stop_lon, stop_lan " +
                "FROM stops " +
                "WHERE stop_id = " + endStop);

                ResultSet startCords = startCordsQuery.executeQuery();
                ResultSet endCords = endCordsQuery.executeQuery();
                startCords.next();
                endCords.next();

                double[] startLonLan = new double[2]; double[] endLonLan = new double[2];
                for(int i = 0; i < 2; ++i)
                {
                    startLonLan[i] = startCords.getDouble(i +1);
                    endLonLan[i] = endCords.getDouble(i + 1);
                }

                Algorithms<String> alg = new Algorithms<>();
                Date time_1 = trips.getDate("departure_time");
                Date time_2 = trips.getDate("arrival_time");
                long travelTime = alg.getWalkingTime(startLonLan, startPoint) + alg.getWalkingTime(endLonLan, endPoint) + ((time_1.getTime()/1000) - (time_2.getTime()/1000));

                if(travelTime < bestTime)
                {
                    bestTime = travelTime;
                    resultTripID = trips.getString("trip_id");
                    distance = ((double)(trips.getInt("start_distance") - trips.getInt("end_distance")))/1000;
                }
            }


            PreparedStatement tripNameQuery = myCon.prepareStatement("SELECT trip_short_name " +
            "FROM trips " +
            "WHERE trip_id = '" + resultTripID + "'");
            ResultSet tripName = tripNameQuery.executeQuery();
            tripName.next();

            int tripNum = Integer.parseInt(tripName.getString(1));

            resultList.add(new double[]{bestTime, 0});

            PreparedStatement tripQuery = myCon.prepareStatement("SELECT shape_pt_lon as pt_lon, shape_pt_lat as pt_lat " +
            "FROM shapes_maastricht " +
            "WHERE shape_id IN (" +
            "SELECT shape_id " +
            "FROM trips_maastricht " +
            "WHERE trip_id = '" + resultTripID + "')");
            ResultSet resultTrip = tripQuery.executeQuery();
            
            trip = new Trip(resultTrip, bestTime, distance, tripNum);
        }
        catch(Exception e)
        {
            System.out.println(e);
            return null;
        }
        return trip;
    }

    public Trip findBusRoute(String startPostalCode, String endPostalCode){
        dataReader.hashMapConverter();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return computeBusRoute(startCords, endCords);
    }
}