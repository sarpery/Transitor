package com.example.Backend.Algorithms;

import com.example.Backend.DataToHashMap;

public class Algorithms<T> {
    private final double WALKING_SPEED = 0.075; //kpm
    private final double CYCLING_SPEED = 0.4; //kpm
    private final int EARTH_RADIUS = 6371;

    DataToHashMap<T> dataReader = new DataToHashMap<T>();

    //in km 
    public double calculateDistanceHaversine(double[] startCords, double[] endCords){
        double dLat = deg2rad(startCords[0] - endCords[0]);
        double dLon = deg2rad(startCords[1] - endCords[1]);
        double h = Math.pow(Math.sin(dLat/2), 2) + Math.cos(deg2rad(startCords[0]))*Math.cos(deg2rad(endCords[0]))*Math.pow(Math.sin(dLon/2), 2);

        double distance = 2*EARTH_RADIUS*Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return distance;
    }

    public double calculateDistanceHaversine2(double aLat, double aLon,  double bLat, double bLon){
        double dLat = deg2rad(aLat - bLat);
        double dLon = deg2rad(aLon - bLon);
        double h = Math.pow(Math.sin(dLat/2), 2) + Math.cos(deg2rad(aLat))*Math.cos(deg2rad(bLat))*Math.pow(Math.sin(dLon/2), 2);

        return 2*EARTH_RADIUS*Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }


    private double deg2rad(double deg){
        return deg * (Math.PI/180);
    }

    //in minutes
    private int calculateTime(double startCords[], double[] endCords, double transportSpeed){
        double result = calculateDistanceHaversine(startCords, endCords)/transportSpeed;
        return (int)Math.round(result);
    }

    public double getDistance(T from, T to){

        dataReader.hashMapConverter();
        double[] startCords = dataReader.getCoordinates(from);
        double[] endCords = dataReader.getCoordinates(to);

        return calculateDistanceHaversine(startCords, endCords);
    }

    public double getWalkingTime(T startPostalCode, T endPostalCode){

        dataReader.hashMapConverter();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateTime(startCords, endCords, WALKING_SPEED);
    }

    public int getWalkingTime(double[] startCords, double[] endCords){
        return calculateTime(startCords, endCords, WALKING_SPEED);
    }

    public double getCyclingTime(T startPostalCode, T endPostalCode){

        dataReader.hashMapConverter();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateTime(startCords, endCords, CYCLING_SPEED);
    }

    public double getCost(){
        return 0;
    }
}