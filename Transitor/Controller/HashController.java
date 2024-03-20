package Transitor.Controller;

import Transitor.Backend.DataToHashMap;

public interface HashController {

    DataToHashMap map = new DataToHashMap();
    

    default void hashTheData(){
        
        
        map.hashMapConverter();

    }

    

    


}