package Transitor.Backend;

public class Algorithms {
    public static RouteInfo getRouteInfo(String startPostalCode, String endPostalCode){
        //int[] startCords = getCords(startPostalCode); DataStructure request
        //int[] endCords = getCords(endPostalCode); DataStructure request
        int[] startCords = null; int[] endCords = null; // for demo

        RouteInfo routeInfo = new RouteInfo();
        
        routeInfo.setDistance(calculateDistance(startCords, endCords));
        routeInfo.setWalkingTime(calculateTime(startCords, endCords, "WALKING"));
        routeInfo.setCyclingTime(calculateTime(startCords, endCords, "CYCLING"));
        routeInfo.setCost(0);

        return routeInfo;
    }

    private static int calculateDistance(int[] startCords, int[] endCords){
        return 0;
        //TO DO
    }

    private static int calculateTime(int startCords[], int[] endCords, String Transport){
        return 0;
        //TO DO
    }

    public static int getDistance(String startPostalCode, String endPostalCode){
        //int[] startCords = getCords(startPostalCode); DataStructure request
        //int[] endCords = getCords(endPostalCode); DataStructure request
        int[] startCords = null; int[] endCords = null; // for demo

        return calculateDistance(startCords, endCords);
    }

    public static int getWalkingTime(String startPostalCode, String endPostalCode){
        //int[] startCords = getCords(startPostalCode); DataStructure request
        //int[] endCords = getCords(endPostalCode); DataStructure request
        int[] startCords = null; int[] endCords = null; // for demo

        return calculateTime(startCords, endCords, "WALKING");
    }

    public static int getCyclingTime(String startPostalCode, String endPostalCode){
        //int[] startCords = getCords(startPostalCode); DataStructure request
        //int[] endCords = getCords(endPostalCode); DataStructure request
        int[] startCords = null; int[] endCords = null; // for demo

        return calculateTime(startCords, endCords, "CYCLING");
    }

    public static int getCost(){
        return 0;
    }


    private static class RouteInfo{
        private int distance;
        private int walkingTime;
        private int cyclingTime;
        private int cost;

        public void setDistance(int distance){ this.distance = distance; }
        public void setWalkingTime(int walkingTime){ this.walkingTime = walkingTime; }
        public void setCyclingTime(int cyclingTime){ this.cyclingTime = cyclingTime; }
        public void setCost(int cost){ this.cost = cost; }

        public int getDistance(){ return distance; }
        public int getWalkingTime(){ return walkingTime; }
        public int getCyclingTime(){ return cyclingTime; }
        public int getCost(){ return cost; }
    }
}