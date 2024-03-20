package Transitor.Backend;

public class Algorithms {
    private final double WALKING_SPEED = 0.075; //kpm
    private final double CYCLING_SPEED = 0.4; //kpm
    private final int EARTH_RADIUS = 6371;

    public RouteInfo getRouteInfo(String startPostalCode, String endPostalCode){
        DataToHashMap dataReader = new DataToHashMap();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        RouteInfo routeInfo = new RouteInfo();
        
        routeInfo.setDistance(calculateDistanceHaversine(startCords, endCords));
        routeInfo.setWalkingTime(calculateTime(startCords, endCords, WALKING_SPEED));
        routeInfo.setCyclingTime(calculateTime(startCords, endCords, CYCLING_SPEED));
        routeInfo.setCost(0);

        return routeInfo;
    }

    //in km 
    private double calculateDistanceHaversine(double[] startCords, double[] endCords){
        double dLat = deg2rad(startCords[0] - endCords[0]);
        double dLon = deg2rad(startCords[1] - endCords[1]);
        double h = Math.pow(Math.sin(dLat/2), 2) + Math.cos(deg2rad(startCords[0]))*Math.cos(deg2rad(endCords[0]))*Math.pow(Math.sin(dLon/2), 2);

        double distance = 2*EARTH_RADIUS*Math.atan2(Math.sqrt(h), Math.sqrt(h - 1));
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
        DataToHashMap dataReader = new DataToHashMap();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateDistanceHaversine(startCords, endCords);
    }

    public double getWalkingTime(String startPostalCode, String endPostalCode){
        DataToHashMap dataReader = new DataToHashMap();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateTime(startCords, endCords, WALKING_SPEED);
    }

    public double getCyclingTime(String startPostalCode, String endPostalCode){
        DataToHashMap dataReader = new DataToHashMap();
        double[] startCords = dataReader.getCoordinates(startPostalCode);
        double[] endCords = dataReader.getCoordinates(endPostalCode);

        return calculateTime(startCords, endCords, CYCLING_SPEED);
    }

    public double getCost(){
        return 0;
    }


    private class RouteInfo{
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