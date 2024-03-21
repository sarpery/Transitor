package Transitor.Controller;

import Transitor.Backend.DataToHashMap;

public interface HashController {

    DataToHashMap map = new DataToHashMap();
    

    default void hashTheData(){
        
        
        map.hashMapConverter();

    }

    default  double getLat(String code){

        return map.getLat(code);
    }

    default  double getLon(String code){

        return map.getLon(code);
    }
}