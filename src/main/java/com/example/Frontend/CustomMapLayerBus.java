package com.example.Frontend;

import com.example.Backend.Graph.Trip;
import com.example.Backend.RequestDatabase;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class CustomMapLayerBus extends MapLayer {

    private List<double[]> coordinatesList;
    private List<Node> nodesOnMap = new ArrayList<Node>();
    private List<Line> linesOnMap = new ArrayList<Line>();


    public CustomMapLayerBus(String startCord, String endCord ) {
        RequestDatabase requestDatabase = new RequestDatabase();
        Trip trip = requestDatabase.findBusRoute(startCord, endCord);
        coordinatesList = trip.travelPoints;
        for (int i = 0; i < coordinatesList.size(); i++){
            Node icon;
            Line line;
            if (i==0){
                icon = new Circle(5, Color.GREEN);
            }
            else if (i == coordinatesList.size()-1){
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

    @Override
    protected void layoutLayer() {
        for (int a = 0; a < coordinatesList.size(); a++ ){
            try {
                double[] coordinate = coordinatesList.get(a);
        //        System.out.println(coordinate);
                Node currentNode = nodesOnMap.get(a);

                double lon = coordinatesList.get(a)[0];
                double lat = coordinatesList.get(a)[1];

                if (a>0) {
                    Line currentLine = linesOnMap.get(a - 1);
                   // String prevCoord = coordinatesList.get(a - 1);
                    double prevLon = coordinatesList.get(a - 1)[0];
                    double prevLat = coordinatesList.get(a - 1)[1];
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
