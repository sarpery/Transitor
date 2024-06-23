package com.example.Frontend;

import java.util.ArrayList;
import java.util.List;

import com.example.Backend.RequestDatabase;
import com.example.Backend.Graph.Trip;
import com.example.Controller.HashController;
import com.gluonhq.maps.MapLayer;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class CustomMapLayerBus extends MapLayer implements HashController {

    private List<Trip.TripSegment> coordinatesList;
    private List<Node> nodesOnMap = new ArrayList<Node>();
    private List<Line> linesOnMap = new ArrayList<Line>();

    private List<Node> startEndNodes = new ArrayList();
    private List <double[]> startEndCoordinatesList = new ArrayList();

    Node startNode;
    Line startLine;
    Node endNode;
    Line endLine;


    public Trip trip;


    public CustomMapLayerBus(String startCord, String endCord ) throws Exception {
        RequestDatabase requestDatabase = new RequestDatabase();
        trip = requestDatabase.findBusRoute(startCord, endCord);
        coordinatesList = trip.tripStructure;
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
                icon.setVisible(false);
                line = new Line();
                line.setStrokeWidth(4.5);
                line.setStroke(Color.BLUE);
                getChildren().add(line);
                linesOnMap.add(line);

            }
            getChildren().add(icon);
            nodesOnMap.add(i, icon);
        }

        double lonS = getLon(startCord);
        double latS = getLat(startCord);
        double[] st = new double[]{lonS, latS};
        double lonE = getLon(endCord);
        double latE = getLat(endCord);
        double [] ed = new double[]{lonE, latE};

        startEndCoordinatesList.add(0, st);
        startEndCoordinatesList.add(1, ed);

        startNode = new Circle(10,Color.GREEN);
        startLine = new Line();
        getChildren().add(startLine);
        getChildren().add(startNode);

        endNode = new Circle(10,Color.RED);
        endLine = new Line();
        getChildren().add(endLine);
        getChildren().add(endNode);


    }

    // @Override
    // protected void layoutLayer() {
    //     for (int a = 0; a < coordinatesList.size(); a++ ){
    //         try {
    //             Node currentNode = nodesOnMap.get(a);

    //             double lon = coordinatesList.get(a)[1];
    //             double lat = coordinatesList.get(a)[0];
    //             if (a == 0){
    //                 MapPoint mapPoint = new MapPoint(lat, lon);
    //                 Point2D pointOnMap = getMapPoint(mapPoint.getLatitude(), mapPoint.getLongitude());

    //                 MapPoint mapPointSt = new MapPoint(startEndCoordinatesList.get(0)[1], startEndCoordinatesList.get(0)[0]);
    //                 Point2D pointSt = getMapPoint(mapPointSt.getLatitude(), mapPointSt.getLongitude());

    //                 startNode.setTranslateX(pointSt.getX());
    //                 startNode.setTranslateY(pointSt.getY());

    //                 startLine.setStartX(pointSt.getX());
    //                 startLine.setStartY(pointSt.getY());
    //                 startLine.setEndX((pointOnMap.getX()));
    //                 startLine.setEndY((pointOnMap.getY()));

    //             }

    //             if (a>0) {
    //                 Line currentLine = linesOnMap.get(a - 1);
    //                 double prevLon = coordinatesList.get(a - 1)[1];
    //                 double prevLat = coordinatesList.get(a - 1)[0];
    //                 MapPoint mapPointPrev = new MapPoint(prevLat, prevLon);
    //                 Point2D pointPrev = getMapPoint(mapPointPrev.getLatitude(), mapPointPrev.getLongitude());


    //                 MapPoint mapPointCur = new MapPoint(lat, lon);
    //                 Point2D pointCur = getMapPoint(mapPointCur.getLatitude(), mapPointCur.getLongitude());

    //                 currentLine.setStartX(pointPrev.getX());
    //                 currentLine.setStartY(pointPrev.getY());
    //                 currentLine.setEndX(pointCur.getX());
    //                 currentLine.setEndY(pointCur.getY());
    //                 currentNode.setTranslateX(pointCur.getX());
    //                 currentNode.setTranslateY(pointCur.getY());




    //             }

    //             else {
    //                 MapPoint mapPoint = new MapPoint(lat, lon);
    //                 Point2D pointOnMap = getMapPoint(mapPoint.getLatitude(), mapPoint.getLongitude());
    //                 currentNode.setTranslateX(pointOnMap.getX());
    //                 currentNode.setTranslateY(pointOnMap.getY());
    //             }


    //             if ( a == coordinatesList.size()-1){
    //                 MapPoint mapPointCur = new MapPoint(lat, lon);
    //                 Point2D pointCur = getMapPoint(mapPointCur.getLatitude(), mapPointCur.getLongitude());

    //                 MapPoint mapPointEd = new MapPoint(startEndCoordinatesList.get(1)[1], startEndCoordinatesList.get(1)[0]);
    //                 Point2D pointEd = getMapPoint(mapPointEd.getLatitude(), mapPointEd.getLongitude());

    //                 endLine.setStartX(pointCur.getX());
    //                 endLine.setStartY(pointCur.getY());

    //                 endLine.setEndX(pointEd.getX());
    //                 endLine.setEndY(pointEd.getY());

    //                 endNode.setTranslateX(pointEd.getX());
    //                 endNode.setTranslateY(pointEd.getY());

    //             }




    //         } catch (Exception e) {
    //             throw new RuntimeException(e);
    //         }
    //     }


    //}





}
