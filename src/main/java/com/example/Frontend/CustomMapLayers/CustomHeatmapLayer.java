package com.example.Frontend.CustomMapLayers;

import com.example.Backend.Algorithms.AccesibilityFormula;
import com.example.Controller.HashController;
import com.example.Frontend.UIController;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class CustomHeatmapLayer extends MapLayer implements HashController{

    private List<String> postalCodesList;
    private List<Node> nodesOnMap = new ArrayList<Node>();
    private UIController uiController;
    private AccesibilityFormula accesibilityFormula = new AccesibilityFormula();


    public CustomHeatmapLayer(List<String> ls, UIController uiController) throws Exception {
        this.postalCodesList = ls;
        this.uiController = uiController;

        for (String postalCode : ls) {
            drawHeatmapPoints(postalCode);
        }
        uiController.deactivateButtons();
    }

    public CustomHeatmapLayer(UIController uiController) {
        this.uiController = uiController;
    }

    public void drawHeatmapPoints(String postalCode) throws Exception {
        Circle icon = accesibilityFormula.createHeatmapPoint(postalCode);
        double lat = getLat(postalCode);
        double lon = getLon(postalCode);

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
