package com.example.Backend.Graph;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Trip{
    public ArrayList<double[]> travelPoints;
    public long travelTime;
    public double traveledDistance;
    public int tripNumber;

    public Trip(ResultSet shape, long travelTime, double traveledDistance, int tripNumber){
        this.travelTime = travelTime;
        this.traveledDistance = traveledDistance;
        this.tripNumber = tripNumber;
        createPointList(shape);
    }

    private void createPointList(ResultSet shape){
        travelPoints = new ArrayList<double[]>();
        try{
            while(shape.next())
            {
                double[] LonLan = new double[2];
                for(int i = 0; i < 2; ++i) LonLan[i] = shape.getDouble(i + 1);

                travelPoints.add(LonLan);
            }
        } catch(Exception e){ System.out.println(e); }
    }
}
