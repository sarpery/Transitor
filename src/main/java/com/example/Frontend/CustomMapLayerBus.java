package com.example.Frontend;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.LinkedList;
import java.util.List;

import com.example.Backend.RequestDatabase;
import com.example.Backend.Graph.Trip;
import com.example.Controller.HashController;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class CustomMapLayerBus extends MapLayer implements HashController {

    private List<double[]> coordinatesList = new ArrayList<>();


    private LinkedList<Trip.TripSegment> tripSegmentList;
    private List<Circle> nodesOnMap = new ArrayList<Circle>();
    private List<Line> linesOnMap = new ArrayList<Line>();

   // private List<Node> startEndNodes = new ArrayList();
    private List <double[]> startEndCoordinatesList = new ArrayList();

    private List<Boolean> busOrNotList = new ArrayList<>();
    private List<String> busNumList = new ArrayList<>();

    private List<Circle> walkCircles = new ArrayList<>();

//    Node startNode;
//    Line startLine;
//    Node endNode;
//    Line endLine;


    public Trip trip;


    public CustomMapLayerBus(String startCord, String endCord ) throws Exception {
        RequestDatabase requestDatabase = new RequestDatabase();
        trip = requestDatabase.findBusRoute(startCord, endCord);

        tripSegmentList = trip.tripStructure;

        for (int ac = 0; ac < tripSegmentList.size(); ac++){
            List<double[]> currentPartofList = tripSegmentList.get(ac).getPoints();
            boolean isBus = tripSegmentList.get(ac).isBus();
            String busNum = tripSegmentList.get(ac).getBusNumber();

           for (int i = 0; i < currentPartofList.size(); i++) {
               busOrNotList.add(isBus);
               busNumList.add(busNum);
               Circle icon;
               Line line;
               if (isBus){
                   icon = new Circle(4, Color.RED);
                   line = new Line();
                   icon.setVisible(false);


               } else {
                   icon = new Circle(4, Color.BLUE);
                   line = new Line();
                   line.setStrokeWidth(1.6);

               }
               coordinatesList.add(currentPartofList.get(i));
               getChildren().add(icon);
               nodesOnMap.add(icon);
               getChildren().add(line);
               linesOnMap.add(line);

           }
        }
    }
        int counter = 0;
    @Override
    protected void layoutLayer() {
            for (int i = 0; i < coordinatesList.size(); i++) {
                Circle currentNode = nodesOnMap.get(i);
                Line currentLine = linesOnMap.get(i);

                double lon = coordinatesList.get(i)[1];
                double lat = coordinatesList.get(i)[0];


                MapPoint mapPointCur = new MapPoint(lat, lon);
                Point2D pointCur = getMapPoint(mapPointCur.getLatitude(), mapPointCur.getLongitude());

                currentNode.setTranslateX(pointCur.getX());
                currentNode.setTranslateY(pointCur.getY());

                if (!(coordinatesList.size() == i+1)){
                    double nextLon = coordinatesList.get(i+1)[1];
                    double nextLat = coordinatesList.get(i+1)[0];

                    MapPoint mapPointNext = new MapPoint(nextLat, nextLon);
                    Point2D pointNext = getMapPoint(mapPointNext.getLatitude(), mapPointNext.getLongitude());


                    currentLine.setStartX(pointCur.getX());
                    currentLine.setStartY(pointCur.getY());
                    currentLine.setEndX(pointNext.getX());
                    currentLine.setEndY(pointNext.getY());


                    if (busOrNotList.get(i+1)){
                        currentLine.setStrokeWidth(4.5+0.5);

                        switch (busNumList.get(i+1)){
                            case "1":
                                currentLine.setStroke(Color.DEEPPINK);
                                break;
                            case "2":
                                currentLine.setStroke(Color.PURPLE);
                                break;
                            case "3":
                                currentLine.setStroke(Color.DARKGREEN);
                            case "4":
                                currentLine.setStroke(Color.ORANGE);
                                break;
                            case "5":
                                currentLine.setStroke(Color.DARKBLUE);
                                break;
                            case "6":
                                currentLine.setStroke(Color.BLUEVIOLET);
                                break;
                            case "7":
                                currentLine.setStroke(Color.YELLOW);
                                break;
                            case "8":
                                currentLine.setStroke(Color.MEDIUMPURPLE);
                                break;
                            case "9":
                                currentLine.setStroke(Color.LIGHTGREEN);
                                break;
                            case "10":
                                currentLine.setStroke(Color.RED);
                                break;
                            case "11":
                                currentLine.setStroke(Color.SADDLEBROWN);
                                break;
                            case "12":
                                currentLine.setStroke(Color.DARKOLIVEGREEN);
                                break;
                            case "15":
                                currentLine.setStroke(Color.TURQUOISE);
                                break;
                            case "57":
                                currentLine.setStroke(Color.BROWN);
                                break;
                            default:
                                System.out.println("NO MATCH");
                                System.out.println(busNumList.get(i+1));
                                currentLine.setStroke(Color.BLUE);

                        }
                    }

                    if (i > 0 && busOrNotList.get(i-1) && !(busOrNotList.get(i))){
                        currentNode.setFill(Color.YELLOW);
                        currentNode.setRadius(6.7);
                        currentNode.setVisible(true);
                        currentNode.setStrokeWidth(1);
                        currentNode.setStroke(Color.BLACK);
                        currentNode.setViewOrder(-1);
                    }

                    if (i < busOrNotList.size() && busOrNotList.get(i+1) && !(busOrNotList.get(i))){
                        currentNode.setFill(Color.YELLOW);
                        currentNode.setRadius(6.7);
                        currentNode.setVisible(true);
                        currentNode.setStrokeWidth(1);
                        currentNode.setStroke(Color.BLACK);
                        currentNode.setViewOrder(-1);

                    }

                    if (i < busNumList.size() && busNumList.get(i) != busNumList.get(i+1) && busNumList.get(i) != "null" && busNumList.get(i+1) != "null"){
                        currentNode.setFill(Color.ORANGE);
                        currentNode.setRadius(6.7);
                        currentNode.setVisible(true);
                        currentNode.setStrokeWidth(1);
                        currentNode.setStroke(Color.BLACK);
                        currentNode.setViewOrder(-1);

                    }
                    if (i == 0){
                        currentNode.setFill(Color.GREEN);
                        currentNode.setStrokeWidth(1);
                        currentNode.setStroke(Color.BLACK);
                        currentNode.setRadius(9);
                        currentNode.setVisible(true);
                        currentNode.setViewOrder(-1);

                    }

                }
                if(coordinatesList.size() == i+1){
                    currentNode.setFill(Color.RED);
                    currentNode.setStrokeWidth(1);
                    currentNode.setStroke(Color.BLACK);
                    currentNode.setRadius(9);
                    currentNode.setVisible(true);
                    currentNode.setViewOrder(-1);
                }




            }

    }
    }