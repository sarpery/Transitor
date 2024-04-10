package com.example.Controller;

import com.example.Backend.DataToHashMap;

public interface HashController {

    DataToHashMap map = new DataToHashMap();
    

    default void hashTheData(){
        
        
        map.hashMapConverter();

    }

    default double getLat(String code) throws Exception {
        return map.getLat(code);
    }

    default double getLon(String code) throws Exception {
        return map.getLon(code);
    }
}