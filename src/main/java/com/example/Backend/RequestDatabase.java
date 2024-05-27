package com.example.Backend;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalTime;

import com.example.Backend.Algorithms.Algorithms;
import com.example.Backend.Graph.Trip;;

public class RequestDatabase {
    private static final double CORD_DISTANCE = 0.035;
    DataToHashMap<String> dataReader = new DataToHashMap<String>();

    private static ResultSet findNearestStops(double x_cord, double y_cord){
        try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "BCS1510_BCS1510");

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

    private Trip computeBusRoute(double[] startPoint, double[] endPoint){
        Trip trip;
        String resultTripID = "";
        double distance = 0;

        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "BCS1510_BCS1510");


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
                "SELECT stm1.trip_id, stm1.stop_id as departure_point, stm2.stop_id as arrival_point, stm1.departure_time, stm2.arrival_time, stm1.shape_dist_traveled as start_distance, stm2.shape_dist_traveled as end_distance " +
                "FROM (SELECT * FROM stop_times_maastricht WHERE stop_id IN " + startString + ") as stm1 " +
                "JOIN (SELECT * FROM stop_times_maastricht WHERE stop_id IN " + endString  +") as stm2 " +
                "ON stm1.trip_id = stm2.trip_id and stm1.stop_sequence < stm2.stop_sequence and stm1.departure_time >= '" + currentTime +"'");

            ResultSet trips = nearestStopsRequest.executeQuery();
            long bestTime = Integer.MAX_VALUE;
            String finalStartStop = "";
            String finalEndStop = "";

            int c = 0;
            while(trips.next())
            {
                String startStop = trips.getString("departure_point");
                String endStop = trips.getString("arrival_point");

                PreparedStatement startCordsQuery = myCon.prepareStatement("SELECT stop_lan, stop_lon " +
                "FROM stops " +
                "WHERE stop_id = " + startStop);
                PreparedStatement endCordsQuery = myCon.prepareStatement("SELECT stop_lan, stop_lon " +
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
                String time_1 = trips.getString("departure_time");
                String time_2 = trips.getString("arrival_time");
                Duration time = parseDuration(time_2).minus(parseDuration(time_1));
                long travelTime = time.toMinutes() + alg.getWalkingTime(startLonLan, startPoint) + alg.getWalkingTime(endLonLan, endPoint);

                if(travelTime < bestTime)
                {
                    bestTime = travelTime;
                    resultTripID = trips.getString("trip_id");
                    distance = ((double)(trips.getInt("end_distance") - trips.getInt("start_distance")))/1000 +
                            alg.calculateDistanceHaversine(startPoint, startLonLan) + alg.calculateDistanceHaversine(endLonLan, endPoint);
                    distance = Math.round(distance*1000.0)/1000.0;
                    finalStartStop = startStop;
                    finalEndStop = endStop;
                }

                if(++c > 300) break;
            }


            PreparedStatement tripNameQuery = myCon.prepareStatement("SELECT trip_short_name " +
            "FROM trips " +
            "WHERE trip_id = '" + resultTripID + "'");
            ResultSet tripName = tripNameQuery.executeQuery();
            tripName.next();

            int tripNum = Integer.parseInt(tripName.getString(1));

            PreparedStatement tripQuery = myCon.prepareStatement("SELECT shape_pt_lon as pt_lon, shape_pt_lat as pt_lat " +
            "FROM shapes_maastricht " +
            "WHERE shape_id IN (" +
            "SELECT shape_id " +
            "FROM trips_maastricht " +
            "WHERE trip_id = '" + resultTripID + "')");
            ResultSet resultTrip = tripQuery.executeQuery();
            
            trip = new Trip(resultTrip, bestTime, distance, tripNum, finalStartStop, finalEndStop);
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

    private Duration parseDuration(String durationStr) {
        // Split the input string by ":"
        String[] parts = durationStr.split(":");
        
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid duration format, expected HH:MM:SS");
        }

        try {
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = Long.parseLong(parts[2]);

            // Validate the parsed values
            if (minutes < 0 || minutes >= 60) {
                throw new IllegalArgumentException("Minutes must be between 0 and 59");
            }
            if (seconds < 0 || seconds >= 60) {
                throw new IllegalArgumentException("Seconds must be between 0 and 59");
            }

            // Create the Duration object
            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in duration string", e);
        }
    }
}