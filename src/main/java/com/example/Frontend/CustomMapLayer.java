package com.example.Frontend;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Node;

import javafx.scene.shape.Line;
import javafx.util.Pair;
import static com.example.Frontend.UIController.*;

public class CustomMapLayer extends MapLayer {
    
     private final Node node1;
    private final Node node2;
    private Line line;

    public CustomMapLayer() {

        node1 = new Circle(5,Color.RED);
        getChildren().add(node1);

        node2 = new Circle(5,Color.BLUE);
        getChildren().add(node2);

        line = new Line(ax, ay, bx, by);
        getChildren().add(line);
    }

    @Override
    protected void layoutLayer() {

        MapPoint mapPoint1 = new MapPoint(ax, ay);
        Point2D point1 = getMapPoint(mapPoint1.getLatitude(), mapPoint1.getLongitude());
        MapPoint mapPoint2 = new MapPoint(bx, by);
        Point2D point2 = getMapPoint(mapPoint2.getLatitude(), mapPoint2.getLongitude());
        node1.setTranslateX(point1.getX());
        node1.setTranslateY(point1.getY());
        node2.setTranslateX(point2.getX());
        node2.setTranslateY(point2.getY());

        line.setStartX(point1.getX());
        line.setStartY(point1.getY());
        line.setEndX(point2.getX());
        line.setEndY(point2.getY());
    }
}
