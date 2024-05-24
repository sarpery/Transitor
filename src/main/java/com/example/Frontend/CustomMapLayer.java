package com.example.Frontend;

import com.example.Controller.HashController;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Node;

import javafx.scene.shape.Line;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.example.Frontend.UIController.*;

public class CustomMapLayer extends MapLayer implements HashController{


    private List<String> coordinatesList;
    private List<Node> nodesOnMap = new ArrayList<Node>();
    private List<Line> linesOnMap = new ArrayList<Line>();


    public CustomMapLayer(List<String> ls) {
        coordinatesList = ls;
        for (int i = 0; i < ls.size(); i++){
            Node icon;
            Line line;
            if (i==0){
                icon = new Circle(5,Color.GREEN);
            }
            else if (i == ls.size()-1){
                icon = new Circle(5,Color.RED);
                line = new Line();
                getChildren().add(line);
                linesOnMap.add(line);

            }
            else {
                icon = new Circle(5,Color.BLUE);
                line = new Line();
                getChildren().add(line);
                linesOnMap.add(line);
            }
            getChildren().add(icon);
            nodesOnMap.add(i, icon);
        }
    }

    public CustomMapLayer(){

    }

    @Override
    protected void layoutLayer() {
        for (int a = 0; a < coordinatesList.size(); a++ ){
            try {
                String coordinate = coordinatesList.get(a);
                Node currentNode = nodesOnMap.get(a);
                double lat = getLat(coordinate);
                double lon = getLon(coordinate);

                if (a>0) {
                    Line currentLine = linesOnMap.get(a - 1);
                    String prevCoord = coordinatesList.get(a - 1);
                    double prevLat = getLat(prevCoord);
                    double prevLon = getLon(prevCoord);
                    MapPoint mapPointPrev = new MapPoint(prevLat, prevLon);
                    Point2D pointPrev = getMapPoint(mapPointPrev.getLatitude(), mapPointPrev.getLongitude());


                    MapPoint mapPointCur = new MapPoint(lat, lon);
                    Point2D pointCur = getMapPoint(mapPointCur.getLatitude(), mapPointCur.getLongitude());

                    currentLine.setStartX(pointPrev.getX());
                    currentLine.setStartY(pointPrev.getY());
                    currentLine.setEndX(pointCur.getX());
                    currentLine.setEndY(pointCur.getY());
                    currentNode.setTranslateX(pointCur.getX());
                    currentNode.setTranslateY(pointCur.getY());


                }else {
                    MapPoint mapPoint = new MapPoint(lat, lon);
                    Point2D pointOnMap = getMapPoint(mapPoint.getLatitude(), mapPoint.getLongitude());
                    currentNode.setTranslateX(pointOnMap.getX());
                    currentNode.setTranslateY(pointOnMap.getY());
                }




            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }
}
