package Transitor.Controller;

import Transitor.Backend.Algorithms;

public interface AlgoConnecter {
    
    Algorithms algo = new Algorithms();

    default double getDistance(String startCords, String endCords){


        return algo.getDistance(startCords, endCords);
    }



    default double walkingTime(String startCords, String endCords){

        return algo.getWalkingTime(startCords, endCords);
    }


    default double cyclingTime(String startPostalCode, String endPostalCode){


        return algo.getCyclingTime(startPostalCode, endPostalCode);
    }

    

}
