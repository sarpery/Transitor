package com.example.Frontend.CustomMapLayers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.example.Backend.RequestDatabase;
import com.example.Backend.Graph.Trip;
import com.example.Controller.HashController;
import com.example.Frontend.UIController;
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
    public UIController uic;


    public CustomMapLayerBus(String startCord, String endCord, UIController ui, String time ) throws Exception {
        uic = ui;
        RequestDatabase requestDatabase = new RequestDatabase();
        trip = requestDatabase.findBusRoute(startCord, endCord, time);

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
                                uic.one.setVisible(true);

                                break;
                            case "2":
                                currentLine.setStroke(Color.PURPLE);
                                uic.two.setVisible(true);

                                break;
                            case "3":
                                currentLine.setStroke(Color.DARKGREEN);
                                uic.three.setVisible(true);

                            case "4":
                                currentLine.setStroke(Color.ORANGE);
                                uic.four.setVisible(true);

                                break;
                            case "5":
                                currentLine.setStroke(Color.DARKBLUE);
                                uic.five.setVisible(true);

                                break;
                            case "6":
                                currentLine.setStroke(Color.CYAN);
                                uic.six.setVisible(true);

                                break;
                            case "7":
                                currentLine.setStroke(Color.YELLOW);
                                uic.seven.setVisible(true);

                                break;
                            case "8":
                                currentLine.setStroke(Color.MEDIUMPURPLE);
                                uic.eight.setVisible(true);

                                break;
                            case "9":
                                currentLine.setStroke(Color.LIGHTGREEN);
                                uic.nine.setVisible(true);

                                break;
                            case "10":
                                currentLine.setStroke(Color.RED);
                                uic.ten.setVisible(true);

                                break;
                            case "11":
                                currentLine.setStroke(Color.SADDLEBROWN);
                                uic.eleven.setVisible(true);

                                break;
                            case "12":
                                currentLine.setStroke(Color.DARKOLIVEGREEN);
                                uic.twelve.setVisible(true);

                                break;
                            case "15":
                                currentLine.setStroke(Color.TURQUOISE);
                                uic.fifteen.setVisible(true);

                                break;
                            case "57":
                                currentLine.setStroke(Color.BROWN);
                                uic.fiftySeven.setVisible(true);

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