package com.example.Frontend.CustomMapLayers;

import com.example.Controller.HashController;
import com.gluonhq.maps.MapLayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/** A route overlay. Scene graph nodes are created on the JavaFX thread. */
public final class CustomMapLayer extends MapLayer implements HashController {
    private final List<double[]> points = new ArrayList<>();
    private final List<Circle> markers = new ArrayList<>();
    private final List<Line> lines = new ArrayList<>();

    public CustomMapLayer() {
        this(Collections.emptyList());
    }

    public CustomMapLayer(List<String> postcodes) {
        if (postcodes == null) {
            throw new IllegalArgumentException("No route connects those postcodes in the bundled network.");
        }
        for (String postcode : postcodes) {
            try {
                points.add(new double[]{getLat(postcode), getLon(postcode)});
            } catch (Exception exception) {
                throw new IllegalArgumentException("A route point is missing from the postcode data.", exception);
            }
        }
        for (int index = 0; index < points.size(); index++) {
            if (index > 0) {
                Line line = new Line();
                line.setStroke(Color.web("#147d78"));
                line.setStrokeWidth(4);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                lines.add(line);
                getChildren().add(line);
            }
            boolean endpoint = index == 0 || index == points.size() - 1;
            Circle marker = new Circle(endpoint ? 7 : 3);
            marker.setFill(index == 0 ? Color.web("#147d78")
                    : index == points.size() - 1 ? Color.web("#e3794a") : Color.WHITE);
            marker.setStroke(endpoint ? Color.WHITE : Color.web("#147d78"));
            marker.setStrokeWidth(endpoint ? 3 : 1.5);
            markers.add(marker);
        }
        getChildren().addAll(markers);
        setMouseTransparent(true);
    }

    @Override
    protected void layoutLayer() {
        Point2D previous = null;
        for (int index = 0; index < points.size(); index++) {
            double[] coordinate = points.get(index);
            Point2D current = getMapPoint(coordinate[0], coordinate[1]);
            Circle marker = markers.get(index);
            marker.setCenterX(current.getX());
            marker.setCenterY(current.getY());
            if (previous != null) {
                Line line = lines.get(index - 1);
                line.setStartX(previous.getX());
                line.setStartY(previous.getY());
                line.setEndX(current.getX());
                line.setEndY(current.getY());
            }
            previous = current;
        }
    }
}
