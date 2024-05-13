package com.example.Controller;

import com.example.Backend.Algorithms.Algorithms;

public interface AlgoConnecter {
    
    
    Algorithms<String> algo = new Algorithms<String>();

    default double getDistance(String startCords, String endCords) throws Exception{
        return algo.getDistance(startCords, endCords);
    }



    default double walkingTime(String startCords, String endCords) throws Exception{
        return algo.getWalkingTime(startCords, endCords);
    }


    default double cyclingTime(String startPostalCode, String endPostalCode) throws Exception{
        return algo.getCyclingTime(startPostalCode, endPostalCode);
    }

    

}
