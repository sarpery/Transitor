package com.example.Backend.Graph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.example.Backend.Algorithms.Algorithms;

public class Trip{
    private Algorithms<double[]> alg = new Algorithms<double[]>();;
    public ArrayList<double[]> travelPoints;
    public long travelTime;
    public double traveledDistance;
    public int tripNumber;

    public Trip(ResultSet shape, long travelTime, double traveledDistance, int tripNumber, String startStop, String endStop){
        this.travelTime = travelTime;
        this.traveledDistance = traveledDistance;
        this.tripNumber = tripNumber;

        double[] startPoint = new double[2]; double[] endPoint = new double[2];
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection myCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/gtfs", "project1-2", "BCS1510_BCS1510");
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

                for(int i = 0; i < 2; i++)
                {
                    startPoint[i] = startCords.getDouble(i +1);
                    endPoint[i] = endCords.getDouble(i + 1);
                }
        } catch(Exception e){System.out.println(e);}
        createPointList(shape, startPoint, endPoint);
    }

    private void createPointList(ResultSet shape, double[] startPoint, double[] endPoint){
        double startPointBestDistance = Integer.MAX_VALUE;
        double endPointBestDistance = Integer.MAX_VALUE;
        int startPointIndex = 0;
        int endPointIndex = 0;

        int index = 0;

        ArrayList<double[]> tempTravelPoints = new ArrayList<double[]>();
        try{
            while(shape.next())
            {
                double[] LonLan = new double[2];
                for(int i = 0; i < 2; ++i) LonLan[i] = shape.getDouble(i + 1);

                tempTravelPoints.add(LonLan);

                if(alg.calculateDistanceHaversine(startPoint, LonLan) < startPointBestDistance)
                {
                    startPointBestDistance = alg.calculateDistanceHaversine(startPoint, LonLan);
                    startPointIndex = index;
                }
                if(alg.calculateDistanceHaversine(endPoint, LonLan) < endPointBestDistance)
                {
                    endPointBestDistance = alg.calculateDistanceHaversine(endPoint, LonLan);
                    endPointIndex = index;
                }

                ++index;
            }

            travelPoints = new ArrayList<double[]>();

            for(int i = startPointIndex; i <= endPointIndex; ++i)
            {
                travelPoints.add(tempTravelPoints.get(i));
            }
        } catch(Exception e){ System.out.println(e); }
    }
}
