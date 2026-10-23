package com.example.Frontend.CustomMapLayers;

import com.example.Controller.HashController;
import com.gluonhq.maps.MapLayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/** Displays already computed accessibility scores without doing work in layout. */
public final class CustomHeatmapLayer extends MapLayer implements HashController {
    private final List<double[]> coordinates = new ArrayList<>();
    private final List<Circle> markers = new ArrayList<>();

    public CustomHeatmapLayer(Map<String, Double> scores) throws Exception {
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            coordinates.add(new double[]{getLat(entry.getKey()), getLon(entry.getKey())});
            Circle marker = new Circle(5, colorForScore(entry.getValue()));
            marker.setOpacity(0.78);
            markers.add(marker);
        }
        getChildren().addAll(markers);
        setMouseTransparent(true);
    }

    private Color colorForScore(double score) {
        if (score > 1000) return Color.web("#1b917b");
        if (score > 500) return Color.web("#95b950");
        if (score > 150) return Color.web("#e9b64d");
        return Color.web("#d86d56");
    }

    @Override
    protected void layoutLayer() {
        for (int index = 0; index < coordinates.size(); index++) {
            double[] coordinate = coordinates.get(index);
            Point2D point = getMapPoint(coordinate[0], coordinate[1]);
            markers.get(index).setCenterX(point.getX());
            markers.get(index).setCenterY(point.getY());
        }
    }
}
