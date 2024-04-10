package com.example.Controller;

import com.example.Backend.Algorithms;

public interface AlgoConnecter {
    
    Algorithms algo = new Algorithms();

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
