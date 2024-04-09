package com.example.Backend;

public class Algorithms {
    private final double WALKING_SPEED = 0.075; //kpm
    private final double CYCLING_SPEED = 0.4; //kpm
    private final int EARTH_RADIUS = 6371;

    DataToHashMap dataReader = new DataToHashMap();

    //in km 
    private double calculateDistanceHaversine(double[] startCords, double[] endCords){
        double dLat = deg2rad(startCords[0] - endCords[0]);
        double dLon = deg2rad(startCords[1] - endCords[1]);
        double h = Math.pow(Math.sin(dLat/2), 2) + Math.cos(deg2rad(startCords[0]))*Math.cos(deg2rad(endCords[0]))*Math.pow(Math.sin(dLon/2), 2);

        double distance = 2*EARTH_RADIUS*Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return distance;
    }

    private double deg2rad(double deg){
        return deg * (Math.PI/180);
    }

    //in minutes
    private int calculateTime(double startCords[], double[] endCords, double transportSpeed){
        double result = calculateDistanceHaversine(startCords, endCords)/transportSpeed;
        return (int)Math.round(result);
    }

    public double getDistance(String startPostalCode, String endPostalCode){

        dataReader.hashMapConverter();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateDistanceHaversine(startCords, endCords);
    }

    public double getWalkingTime(String startPostalCode, String endPostalCode){

        dataReader.hashMapConverter();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateTime(startCords, endCords, WALKING_SPEED);
    }

    public double getCyclingTime(String startPostalCode, String endPostalCode){

        dataReader.hashMapConverter();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateTime(startCords, endCords, CYCLING_SPEED);
    }

    public double getCost(){
        return 0;
    }

}