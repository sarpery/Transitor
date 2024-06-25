package com.example.Backend.Algorithms;

import java.time.Duration;

import com.example.Backend.DataToHashMap;

public class Algorithms<T> {
    private final double WALKING_SPEED = 0.055; //kpm
    public static final double CYCLING_SPEED = 0.4; //kpm
    private final int EARTH_RADIUS = 6371;

    DataToHashMap<T> dataReader;
    public Algorithms(DataToHashMap<T> dataReader) {
        this.dataReader = dataReader;
    }

    public Algorithms() {
        dataReader = new DataToHashMap<T>("src/main/java/com/example/Data/MassZipLatLon.txt");
    }

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

        public Duration parseDuration(String durationStr) {
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
