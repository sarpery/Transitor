package Transitor.Backend;

public class Algorithms {
    private static final double WALKING_SPEED = 0.075; //kpm
    private static final double CYCLING_SPEED = 0.4; //kpm
    private static final int EARTH_RADIUS = 6371;
    private static final double LAT_TO_KM_RATIO = 110.574;
    private static final double LON_TO_KM_RATIO = 111.32*Math.cos(deg2rad(50.8483));//50.8483 - Maastricht latitude

    public static RouteInfo getRouteInfo(String startPostalCode, String endPostalCode){
        //double[] startCords = getCords(startPostalCode); DataStructure request
        //double[] endCords = getCords(endPostalCode); DataStructure request
        double[] startCords = null; double[] endCords = null; // for demo

        RouteInfo routeInfo = new RouteInfo();
        
        routeInfo.setDistance(calculateDistancePythagorian(startCords, endCords));
        routeInfo.setWalkingTime(calculateTime(startCords, endCords, WALKING_SPEED));
        routeInfo.setCyclingTime(calculateTime(startCords, endCords, CYCLING_SPEED));
        routeInfo.setCost(0);

        return routeInfo;
    }

    //in kilometers
    private static double calculateDistancePythagorian(double[] startCords, double[] endCords){
        double dLat = (startCords[0] - endCords[0])*LAT_TO_KM_RATIO;
        double dLon = (startCords[1] - endCords[1])*LON_TO_KM_RATIO;

        Double result = Math.sqrt(Math.pow(dLat, 2) - Math.pow(dLon, 2));
        double roundedResult = Math.round(result*100)/100;

        return roundedResult;
    }

    private static double deg2rad(double deg){
        return deg * (Math.PI/180);
    }

    //in minutes
    private static int calculateTime(double startCords[], double[] endCords, double transportSpeed){
        double result = calculateDistancePythagorian(startCords, endCords)/transportSpeed;
        return (int)Math.round(result);
    }

    public static double getDistance(String startPostalCode, String endPostalCode){
        //double[] startCords = getCords(startPostalCode); DataStructure request
        //double[] endCords = getCords(endPostalCode); DataStructure request
        double[] startCords = null; double[] endCords = null; // for demo

        return calculateDistancePythagorian(startCords, endCords);
    }

    public static double getWalkingTime(String startPostalCode, String endPostalCode){
        //double[] startCords = getCords(startPostalCode); DataStructure request
        //int[] endCords = getCords(endPostalCode); DataStructure request
        double[] startCords = null; double[] endCords = null; // for demo

        return calculateTime(startCords, endCords, WALKING_SPEED);
    }

    public static double getCyclingTime(String startPostalCode, String endPostalCode){
        //double[] startCords = getCords(startPostalCode); DataStructure request
        //double[] endCords = getCords(endPostalCode); DataStructure request
        double[] startCords = null; double[] endCords = null; // for demo

        return calculateTime(startCords, endCords, CYCLING_SPEED);
    }

    public static double getCost(){
        return 0;
    }


    private static class RouteInfo{
        private double distance;
        private double walkingTime;
        private double cyclingTime;
        private double cost;

        public void setDistance(double distance){ this.distance = distance; }
        public void setWalkingTime(double walkingTime){ this.walkingTime = walkingTime; }
        public void setCyclingTime(double cyclingTime){ this.cyclingTime = cyclingTime; }
        public void setCost(double cost){ this.cost = cost; }

        public double getDistance(){ return distance; }
        public double getWalkingTime(){ return walkingTime; }
        public double getCyclingTime(){ return cyclingTime; }
        public double getCost(){ return cost; }
    }
}