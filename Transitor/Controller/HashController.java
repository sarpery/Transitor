package com.example.Controllers;

import com.example.Backend.DataToHashMap;

public interface HashController {

    DataToHashMap map = new DataToHashMap();
    

    default void hashTheData(){
        
        
        map.hashMapConverter();

    }

    

    


}