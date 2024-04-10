package com.example.Backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.example.Frontend.ErrorPopup;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DataToHashMap {
    
    private  HashMap<String, double[]> coordinatesData = new HashMap<String, double[]>();

    public DataToHashMap(){

    }
    
    public void hashMapConverter(){
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\java\\com\\example\\Data\\MassZipLatLon.txt"))) {
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

    public double getLat(String key) throws Exception{
        double[] coord = getCoordinates(key);
        double lat = coord[0];

        return lat;
    }

    public double getLon(String key) throws Exception{
        double[] coord = getCoordinates(key);
        double lon = coord[1];

        return lon;
    }


    private boolean mapContains(String key) {
        if (coordinatesData.containsKey(key)) {
            return true;
        }
        else{
            System.out.println("Not Found");
            return false;
        }
    }

    double[] getCoordinates(String key) throws Exception{
        double[] coord = null;

        if (mapContains(key)) {
            coord = coordinatesData.get(key);
        }
        else {
            coord = RequestAPI.request(key);
            coordinatesData.put(key,coord);
        }
        return coord;

    }
}
