package com.example.Backend;



public class Algorithms {
    private static final double WALKING_SPEED = 0.075; //kpm
    private static final double CYCLING_SPEED = 0.4; //kpm

    public Algorithms(){
        
    }

    //in kilometers
    public  double calculateDistance(double[] startCords, double[] endCords){
        double horizontal = startCords[0] - endCords[0];
        double vertical = startCords[1] - endCords[1];

        Double result = Math.sqrt(Math.pow(horizontal, 2) - Math.pow(vertical, 2));
        double roundedResult = Math.round(result*100)/100;

        return roundedResult;
    }

    //in minutes
    public  int calculateTime(double startCords[], double[] endCords, double transportSpeed){
        double result = calculateDistance(startCords, endCords)/transportSpeed;
        return (int)Math.round(result);
    }

    public  double getDistance(String startPostalCode, String endPostalCode){
        //double[] startCords = getCords(startPostalCode); DataStructure request
        //double[] endCords = getCords(endPostalCode); DataStructure request
        double[] startCords = null; double[] endCords = null; // for demo

        return calculateDistance(startCords, endCords);
    }

    public  double getWalkingTime(String startPostalCode, String endPostalCode){
        //double[] startCords = getCords(startPostalCode); DataStructure request
        //int[] endCords = getCords(endPostalCode); DataStructure request
        double[] startCords = null; double[] endCords = null; // for demo

        return calculateTime(startCords, endCords, WALKING_SPEED);
    }

    public  double getCyclingTime(String startPostalCode, String endPostalCode){
        //double[] startCords = getCords(startPostalCode); DataStructure request
        //double[] endCords = getCords(endPostalCode); DataStructure request
        double[] startCords = null; double[] endCords = null; // for demo

        return calculateTime(startCords, endCords, CYCLING_SPEED);
    }

    public  double getCost(){
        return 0;
    }


   
}