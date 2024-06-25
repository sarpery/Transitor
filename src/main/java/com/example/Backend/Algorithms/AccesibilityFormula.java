package com.example.Backend.Algorithms;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geojson.FeatureCollection;

import com.example.Controller.AlgoConnecter;
import com.example.Controller.GraphController;
import com.example.Controller.HashController;
import com.example.Controller.SearchConnector;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AccesibilityFormula implements AlgoConnecter, HashController, GraphController, SearchConnector {

    private double minScore = Integer.MAX_VALUE;
    private double maxScore = Integer.MIN_VALUE;

    private double score = 0;

    private int totalScore = 0;
    private int averageScore = 0;
    private String minZip = "";
    private String maxZip = "";
    List<String> connectorZips = Arrays.asList("9999TT", "9999ZZ", "9999AA", "9999GG","6200AA");

    public double accessibility(String postalCode) throws Exception {
        double score = 0;
        InputStream inputStream = new FileInputStream("src/main/java/com/example/Data/GeoJSON/tourism.geojson");
        InputStream inputStream2 = new FileInputStream("src/main/java/com/example/Data/GeoJSON/amenity.geojson");
        InputStream inputStream3 = new FileInputStream("src/main/java/com/example/Data/GeoJSON/shop.geojson");

        FeatureCollection featureCollectionAll = new ObjectMapper().readValue(inputStream, FeatureCollection.class);
        FeatureCollection featureCollection2 = new ObjectMapper().readValue(inputStream2, FeatureCollection.class);
        FeatureCollection featureCollection3 = new ObjectMapper().readValue(inputStream3, FeatureCollection.class);

        featureCollectionAll.addAll(featureCollection2.getFeatures());
        featureCollectionAll.addAll(featureCollection3.getFeatures());

        String regex = "longitude=([\\d.]+), latitude=([\\d.]+)";
        Pattern p = Pattern.compile(regex);

        for (int i = 0; i < featureCollectionAll.getFeatures().size()-1; i++){
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
            double havDistanceMeters = havDistance * 1000;
            double benefit = fallofFunction(havDistanceMeters);

            if(benefit > 0.0){
                score += benefit * amenityBenefit(type);
            }
        }

        return score;
    }

    public Circle createHeatmapPoint(String postalCode) throws Exception {
        Circle icon;
        score = accessibility(postalCode);
        if(!connectorZips.contains(postalCode)) {
            if (score > 1000) {
                icon = new Circle(5, Color.GREEN);
            } else if (score > 500) {
                icon = new Circle(5, Color.YELLOW);
            } else if (score > 150) {
                icon = new Circle(5, Color.ORANGE);
            } else {
                icon = new Circle(5, Color.RED);
            }
        }else {
            icon = new Circle(0, Color.TRANSPARENT);
        }
        // calculations(score, postalCode); //method used for finding the highest, lowest and average score

        return icon;
    }

    public void calculations(double score, String postalCode){

        if(score < minScore){
            minScore = (int)score;
            minZip = postalCode;
        }

        if(score > maxScore){
            maxScore = (int)score;
            maxZip = postalCode;
        }
        System.out.println("Min score: " + minZip + ": " + minScore);
        System.out.println("Max score: " + maxZip + ": " + maxScore);

        totalScore+= (int)score;
        averageScore = totalScore/3051;
        System.out.println(averageScore);
    }
    private static final double K = -Math.log(0.05) / 2000;

    public static double fallofFunction(double distance) {
        return Math.exp(-K * distance);
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
                return 0;
        }
    }
}
