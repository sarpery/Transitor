package com.example.Backend.Algorithms;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geojson.FeatureCollection;

import com.example.Backend.DataResources;
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

    private static volatile List<Amenity> cachedAmenities;

    private static final class Amenity {
        private final String type;
        private final double latitude;
        private final double longitude;

        private Amenity(String type, double latitude, double longitude) {
            this.type = type;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /** Parse the bundled feature data once; every score reuses immutable coordinates. */
    private static List<Amenity> amenities() throws IOException {
        List<Amenity> result = cachedAmenities;
        if (result != null) {
            return result;
        }
        synchronized (AccesibilityFormula.class) {
            if (cachedAmenities != null) {
                return cachedAmenities;
            }
            FeatureCollection features;
            try (InputStream tourism = DataResources.open("classpath:data/geojson/tourism.geojson");
                 InputStream amenities = DataResources.open("classpath:data/geojson/amenity.geojson");
                 InputStream shops = DataResources.open("classpath:data/geojson/shop.geojson")) {
                ObjectMapper mapper = new ObjectMapper();
                features = mapper.readValue(tourism, FeatureCollection.class);
                features.addAll(mapper.readValue(amenities, FeatureCollection.class).getFeatures());
                features.addAll(mapper.readValue(shops, FeatureCollection.class).getFeatures());
            }
            Pattern coordinates = Pattern.compile("longitude=([\\d.]+), latitude=([\\d.]+)");
            List<Amenity> parsed = new ArrayList<>();
            // Preserve the original coursework scoring range and category precedence.
            for (int i = 0; i < features.getFeatures().size() - 1; i++) {
                Map<String, Object> properties = features.getFeatures().get(i).getProperties();
                String type = null;
                for (String category : List.of("tourism", "amenity", "shop")) {
                    if (properties.containsKey(category)) {
                        type = properties.get(category).toString();
                    }
                }
                Matcher matcher = coordinates.matcher(features.getFeatures().get(i).getGeometry().toString());
                if (!matcher.find()) {
                    throw new IOException("An accessibility feature has no point coordinates.");
                }
                parsed.add(new Amenity(type, Double.parseDouble(matcher.group(2)),
                        Double.parseDouble(matcher.group(1))));
            }
            cachedAmenities = Collections.unmodifiableList(parsed);
            return cachedAmenities;
        }
    }

    public double accessibility(String postalCode) throws Exception {
        double score = 0;
        double latitude = getLat(postalCode);
        double longitude = getLon(postalCode);
        for (Amenity amenity : amenities()) {
            double distance = calculateDistanceHaversine2(latitude, longitude,
                    amenity.latitude, amenity.longitude) * 1000;
            double benefit = fallofFunction(distance);
            if (benefit > 0.0) {
                score += benefit * amenityBenefit(amenity.type);
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

    public double amenityBenefit(String name) {
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
