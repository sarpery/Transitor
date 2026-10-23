package com.example.Frontend.CustomMapLayers;

import com.example.Backend.Graph.Trip;
import com.gluonhq.maps.MapLayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/** Renders a computed trip; querying the database belongs to the controller task. */
public final class CustomMapLayerBus extends MapLayer {
    private final List<Segment> segments = new ArrayList<>();
    private final List<Marker> markers = new ArrayList<>();
    private final Set<String> busRoutes = new LinkedHashSet<>();

    public CustomMapLayerBus(Trip trip) {
        if (trip == null || trip.tripStructure == null || trip.tripStructure.isEmpty()) {
            throw new IllegalArgumentException("No transit connection was found for that departure time.");
        }
        double[] first = null;
        double[] last = null;
        for (Trip.TripSegment part : trip.tripStructure) {
            List<double[]> points = part.getPoints();
            if (points == null || points.isEmpty()) continue;
            if (first == null) first = points.get(0);
            last = points.get(points.size() - 1);
            String busNumber = part.getBusNumber();
            boolean isBus = part.isBus();
            if (isBus && busNumber != null && !"null".equals(busNumber)) busRoutes.add(busNumber);
            Color color = isBus ? routeColor(busNumber) : Color.web("#65798a");
            for (int index = 1; index < points.size(); index++) {
                Line line = new Line();
                line.setStroke(color);
                line.setStrokeWidth(isBus ? 4.5 : 2.5);
                line.setStrokeLineCap(StrokeLineCap.ROUND);
                if (!isBus) line.getStrokeDashArray().addAll(5.0, 5.0);
                segments.add(new Segment(points.get(index - 1), points.get(index), line));
                getChildren().add(line);
            }
            if (isBus) addMarker(points.get(0), 5, Color.web("#e9b64d"));
        }
        if (first == null || last == null) {
            throw new IllegalArgumentException("This transit connection has no drawable route data.");
        }
        addMarker(first, 7, Color.web("#147d78"));
        addMarker(last, 7, Color.web("#e3794a"));
        for (Marker marker : markers) getChildren().add(marker.circle);
        setMouseTransparent(true);
    }

    public Set<String> getBusRoutes() {
        return Collections.unmodifiableSet(busRoutes);
    }

    public static Color routeColor(String route) {
        Color[] palette = {Color.web("#385ba0"), Color.web("#9a4b89"), Color.web("#21856f"),
                Color.web("#b56c29"), Color.web("#7959a2"), Color.web("#147f9b")};
        return palette[Math.floorMod(route == null ? 0 : route.hashCode(), palette.length)];
    }

    private void addMarker(double[] point, double radius, Color color) {
        Circle circle = new Circle(radius, color);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2);
        markers.add(new Marker(point, circle));
    }

    @Override
    protected void layoutLayer() {
        for (Segment segment : segments) {
            Point2D from = getMapPoint(segment.from[0], segment.from[1]);
            Point2D to = getMapPoint(segment.to[0], segment.to[1]);
            segment.line.setStartX(from.getX());
            segment.line.setStartY(from.getY());
            segment.line.setEndX(to.getX());
            segment.line.setEndY(to.getY());
        }
        for (Marker marker : markers) {
            Point2D point = getMapPoint(marker.point[0], marker.point[1]);
            marker.circle.setCenterX(point.getX());
            marker.circle.setCenterY(point.getY());
        }
    }

    private static final class Segment {
        private final double[] from;
        private final double[] to;
        private final Line line;
        private Segment(double[] from, double[] to, Line line) {
            this.from = from;
            this.to = to;
            this.line = line;
        }
    }

    private static final class Marker {
        private final double[] point;
        private final Circle circle;
        private Marker(double[] point, Circle circle) {
            this.point = point;
            this.circle = circle;
        }
    }
}
