package com.example.Controller;

import com.example.Backend.Algorithms.Algorithms;

public interface AlgoConnecter {
    
    
    Algorithms<String> algo = new Algorithms<String>();

    default double getDistance(String startCords, String endCords) throws Exception{
        return algo.getDistance(startCords, endCords);
    }

    default double calculateDistanceHaversine2(double a1, double a2, double b1, double b2) throws Exception{
        return algo.calculateDistanceHaversine2( a1, a2, b1, b2);
    }



    default double walkingTime(String startCords, String endCords) throws Exception{
        return algo.getWalkingTime(startCords, endCords);
    }


    default double cyclingTime(String startPostalCode, String endPostalCode) throws Exception{
        return algo.getCyclingTime(startPostalCode, endPostalCode);
    }

    

}
