package com.example.Backend.Graph;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.example.Backend.RequestDatabase;
import com.example.Backend.Algorithms.Algorithms;

public class Trip{
    private Algorithms<double[]> alg = new Algorithms<double[]>();
    private RequestDatabase<String> rDB = new RequestDatabase<String>();
    public long travelTime;
    public double traveledDistance;
    public String tripNumber;
    public String departure;
    public String arrival;
    public LinkedList<double[]> travelPoints;

    public Trip(List<String> travelList, long time){
        traveledDistance = 0;
        travelTime = time;
        travelPoints = new LinkedList<double[]>();

        String pointer1;
        String pointer2;
        for(int i = 0; i < travelList.size() - 1 ; i++ ){
            pointer1 = travelList.get(i);
            String prts[] = pointer1.split("-");
            String startPoint = prts[0];
            pointer2 = travelList.get(i+1);
            String parts[] = pointer2.split("-");
            String endPoint = parts[0];
            String part[] = parts[1].split(":");
            String tripId = part[0];
            System.out.println(i+1 + "/" + travelList.size() + "-" + tripId);
            travelPoints.addAll(createPointList(tripId, startPoint, endPoint));
        }
        traveledDistance = ((int)(traveledDistance*100))/100.0;
    }

    private LinkedList<double[]> createPointList(String tripID, String startPointName, String endPointName){
        LinkedList<double[]> travelPoints = new LinkedList<double[]>();;
        double startPointBestDistance = Integer.MAX_VALUE;
        double endPointBestDistance = Integer.MAX_VALUE;
        int startPointIndex = 0;
        int endPointIndex = 0;
        double[] startPoint = rDB.getPointCords(startPointName);
        double[] endPoint = rDB.getPointCords(endPointName);

        int index = 0;

        ArrayList<double[]> tempTravelPoints = new ArrayList<double[]>();
        ArrayList<Double> shapeDist = new ArrayList<Double>();

        if(!tripID.startsWith("null"))
        {
            ResultSet shape = rDB.getShape(tripID);
            try{
                while(shape.next())
                {
                    double[] LonLan = new double[2];
                    LonLan[0] = shape.getDouble("lan");
                    LonLan[1] = shape.getDouble("lon");
                    shapeDist.add(shape.getDouble("distance"));

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

                for(int i = startPointIndex; i <= endPointIndex; ++i)
                {
                    travelPoints.add(tempTravelPoints.get(i));
                }

                traveledDistance += (shapeDist.get(endPointIndex) - shapeDist.get(startPointIndex))/1000;
            } catch(Exception e){ System.out.println(e); return null;}
        }
        else
        {
            travelPoints.add(startPoint);
            travelPoints.add(endPoint);
            traveledDistance += alg.calculateDistanceHaversine(startPoint, endPoint);
        }

        return travelPoints;
    }
}
