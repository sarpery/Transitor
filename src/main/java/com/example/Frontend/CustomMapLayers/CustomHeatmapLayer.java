package com.example.Frontend.CustomMapLayers;

import com.example.Backend.Algorithms.AccesibilityFormula;
import com.example.Controller.HashController;
import com.example.Frontend.UIController;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.util.Arrays;

import java.util.ArrayList;
import java.util.List;

public class CustomHeatmapLayer extends MapLayer implements HashController{

    private List<String> postalCodesList = new ArrayList<>();
    private List<Node> nodesOnMap = new ArrayList<Node>();
    private UIController uiController;
    private AccesibilityFormula accesibilityFormula = new AccesibilityFormula();


    public CustomHeatmapLayer(List<String> ls, UIController uiController) throws Exception {
        this.postalCodesList = ls;
        this.uiController = uiController;

        for (String postalCode : ls) {
            drawHeatmapPoints(postalCode);
        }
    }

    public CustomHeatmapLayer(UIController uiController) {
        this.uiController = uiController;
    }

    /** Builds the original heatmap after scores have been calculated in the background. */
    public CustomHeatmapLayer(List<String> postcodes, List<Double> scores, UIController uiController) {
        this.postalCodesList = postcodes;
        this.uiController = uiController;
        if (postcodes.size() != scores.size()) throw new IllegalArgumentException("Incomplete accessibility scores.");
        List<String> connectorCodes = Arrays.asList("9999TT", "9999ZZ", "9999AA", "9999GG", "6200AA");
        for (int index = 0; index < postcodes.size(); index++) {
            double score = scores.get(index);
            Circle icon;
            if (connectorCodes.contains(postcodes.get(index))) {
                icon = new Circle(0, Color.TRANSPARENT);
            } else {
                Color color = score > 1000 ? Color.GREEN : score > 500 ? Color.YELLOW
                        : score > 150 ? Color.ORANGE : Color.RED;
                icon = new Circle(5, color);
            }
            getChildren().add(icon);
            nodesOnMap.add(icon);
        }
    }

    public void drawHeatmapPoints(String postalCode) throws Exception {
        Circle icon = accesibilityFormula.createHeatmapPoint(postalCode);
        getChildren().add(icon);
        nodesOnMap.add(icon);
    }

    @Override
    protected void layoutLayer() {
        for (int i = 0; i < postalCodesList.size(); i++) {
            try {
                String postalCode = postalCodesList.get(i);
                Node currentNode = nodesOnMap.get(i);
                double lat = uiController.getLat(postalCode);
                double lon = uiController.getLon(postalCode);

                MapPoint mapPoint = new MapPoint(lat, lon);
                Point2D pointOnMap = getMapPoint(mapPoint.getLatitude(), mapPoint.getLongitude());

                currentNode.setTranslateX(pointOnMap.getX());
                currentNode.setTranslateY(pointOnMap.getY());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
