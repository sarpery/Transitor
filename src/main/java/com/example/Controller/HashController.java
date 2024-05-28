package com.example.Controller;

import com.example.Backend.DataToHashMap;

public interface HashController {

    DataToHashMap<String> map = new DataToHashMap<String>();
    

    default void hashTheData(){
        
        
        map.hashMapConverter();

    }

    default double getLat(String code) throws Exception {
        return map.getLat(code);
    }

    default double getLon(String code) throws Exception {
        return map.getLon(code);
    }

    default boolean mapContains(String code){
        return map.mapContains(code);
    }
}