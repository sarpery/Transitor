package com.example.Backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataToHashMap<T> {
    
    private HashMap<String, double[]> coordinatesData = new HashMap<String, double[]>();

    private String fileName;
    public DataToHashMap(String fileName){
        this.fileName = fileName;
    }
    
    public void hashMapConverter(){

        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String postalCode = parts[0].trim();
                double lat = Double.parseDouble(parts[1].trim());
                double lon = Double.parseDouble(parts[2].trim());
                double[] coordinates = {lat, lon};

                coordinatesData.put(postalCode, coordinates);
                
                
            }
            
        
        }catch(IOException e){
            e.printStackTrace();
           
        }

    }

    public double getLat(T key){

        double[] coord = getCoordinates(key);
        double lat = coord[0];

        return lat;

    }

    public double getLon(T key){

        double[] coord = getCoordinates(key);
        double lon = coord[1];

        return lon;

    }


    public boolean mapContains(T key) {
       
        if (coordinatesData.containsKey(key)) {
            return true;
        }
        else{
            System.out.println("Not Found");
            return false;
        }



    }



    public double[] getCoordinates(T key){

        double[] coord = null;

        if (mapContains(key)) {

            coord = coordinatesData.get(key);

        }
        
        return coord;

    }

    public List<String> getAllZipCodes() {
        return new ArrayList<>(coordinatesData.keySet());
    }
}

