package com.example.Backend.Algorithms;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geojson.FeatureCollection;

import com.example.Controller.AlgoConnecter;
import com.example.Controller.GraphController;
import com.example.Controller.HashController;
import com.example.Controller.SearchConnector;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.animation.KeyValue;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AccesibilityFormula implements AlgoConnecter, HashController, GraphController, SearchConnector {
    final double centerLatitude = 50.851368;
    final double centerLongitude = 5.690973;

    public double accessibility(String postalCode) throws Exception {
        double score = 0;
        score += (calculateDistanceHaversine2(getLat(postalCode), getLon(postalCode), centerLatitude, centerLongitude));

        InputStream inputStream = new FileInputStream("src/main/java/com/example/Frontend/tourism.geojson");
        InputStream inputStream2 = new FileInputStream("src/main/java/com/example/Frontend/amenity.geojson");
        InputStream inputStream3 = new FileInputStream("src/main/java/com/example/Frontend/shop.geojson");

        FeatureCollection featureCollectionAll = new ObjectMapper().readValue(inputStream, FeatureCollection.class);
        FeatureCollection featureCollection2 = new ObjectMapper().readValue(inputStream2, FeatureCollection.class);
        FeatureCollection featureCollection3 = new ObjectMapper().readValue(inputStream3, FeatureCollection.class);

        featureCollectionAll.addAll(featureCollection2.getFeatures());
        featureCollectionAll.addAll(featureCollection3.getFeatures());

        String regex = "longitude=([\\d.]+), latitude=([\\d.]+)";
        Pattern p = Pattern.compile(regex);

        for (int i = 0; i < featureCollectionAll.getFeatures().size()-1; i++){
            //String a = featureCollectionAll.getFeatures().get(i).toString();
            Map<String, Object> as = featureCollectionAll.getFeatures().get(i).getProperties();

            String type = null;
            if (as.containsKey("tourism")) {
                type = as.get("tourism").toString();
            }
            if (as.containsKey("amenity")) {
                type = as.get("amenity").toString();
            }
            if (as.containsKey("shop")) {
                type = as.get("shop").toString();
            }

            String geoCord = featureCollectionAll.getFeatures().get(i).getGeometry().toString();
            Matcher m = p.matcher(geoCord);
            m.find();

            double longitudeLoop = Double.parseDouble(m.group(1));
            double latitudeLoop = Double.parseDouble(m.group(2));

            double havDistance = calculateDistanceHaversine2(getLat(postalCode), getLon(postalCode), latitudeLoop, longitudeLoop);
            double benefit = fallofFunction(havDistance);

            if(benefit > 0.0){
                //System.out.println(calculateDistanceHaversine2(getLat(postalCode), getLon(postalCode), latitudeLoop, longitudeLoop));
                //System.out.println(a);
                score += benefit * amenityBenefit(type);
            }
        }

        return score;
    }

    public Circle createHeatmapPoint(String postalCode) throws Exception {
        double score = accessibility(postalCode);
        Circle icon;

        if (score > 5) {
            icon = new Circle(5, Color.GREEN);
        } else if (score > 3) {
            icon = new Circle(5, Color.YELLOW);
        } else if (score > 1) {
            icon = new Circle(5, Color.ORANGE);
        } else {
            icon = new Circle(5, Color.RED);
        }
        return icon;
    }

    private double fallofFunction(double distance) {
        //1 distance is 1 km
        if (distance <= 0.5) {
            return 1.0;
        }

        return 0.0;
    }

    private double amenityBenefit(String name) {
        switch (name) {
            // Restaurants and bars
            case "bar":
            case "restaurant":
            case "pub":
                return 6;

            // Takeout
            case "fast_food":
                return 4;

            // Shops
            case "clothes":
            case "shoes":
            case "accessories":
            case "books":
            case "mall":
            case "alcohol":
            case "electronics":
            case "gift":
            case "bicycle":
                return 2;

            // Place of worship
            case "place_of_worship":
                return 3;

            // Grocery shopping
            case "supermarket":
            case "market":
                return 5;

            // Medical
            case "hospital":
            case "clinic":
            case "drugs_paraphernalia":
            case "soft_drugs":
            case "pharmacy":
            case "nutrition_supplements":
                return 2;

            // Gym
            case "sports":
                return 2;

            // Default
            default:
                return 1;
        }
    }
}
