package com.example.Frontend;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.example.Controller.AlgoConnecter;
import com.example.Controller.SearchConnector;
import com.example.Controller.GraphController;
import com.example.Controller.HashController;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;


public class UIController implements Initializable, AlgoConnecter, HashController, GraphController, SearchConnector {
 private double initX, initY;
    private double aAX = 0;
    private double aAY = 0;

    private double zipX = 5.710646925;
    private double zipY = 50.83646594;

    private double zipXX = 5.721714464;
    private double zipYY = 50.82346493;

    @FXML
    private MapView mapView;

    @FXML
    private Circle circle1;

    @FXML
    private Line distanceLine;

    @FXML
    private Circle circle2;

    @FXML
    private Polygon map;

    @FXML
    private Label speed;

    @FXML
    private Label timeLabel;

    @FXML
    private Label distance;

    @FXML
    private Button walkButton;

    @FXML
    private Button bikeButton;

    @FXML
    private Button transportButton;

    @FXML
    private Button findPath;

    @FXML
    private Button locationToolTip;

    @FXML
    private TextField startZip;

    @FXML
    private TextField endZip;

    @FXML
    public Pane mapPane;

    @FXML
    MapPoint point = new MapPoint(50.849365,5.694548);

    public CustomMapLayer cL = new CustomMapLayer();


    @FXML
    public MapPoint pointStart;
    public MapPoint pointEnd;




    public String startText;
    public String endText;




    public void walkBikeCalcs(String startText, String endText, String input) throws Exception {
        System.out.println(startText);
        System.out.println(endText);
        int time = 0;

        double distance1 = getDistance(startText, endText);
        double roundedNumber = Math.round(distance1 * 1000.0) / 1000.0;
        distance.setText("Distance: " + String.valueOf(roundedNumber) + " km");
        System.out.println(getDistance(startText, endText));

        if(input.equals("walk")) {
            time = (int) walkingTime(startText, endText);
            timeLabel.setText("Walking time: " + String.valueOf(time) + " min");
            System.out.println("Walking time: " + time);
        }else if(input.equals("bike")) {
            time = (int) cyclingTime(startText, endText);
            timeLabel.setText("Biking time: " + String.valueOf(time) + " min");
            System.out.println("Biking time: " + time);
        }
    }

    public void setupToolTip(){
        locationToolTip.setTooltip(
                new Tooltip(
                        "Popular locations:\n" +
                                "- DACS: 6229EN\n" +
                                "- Vrijthof: 6211LE\n\n" + "GROUP 10\n"));
    }

    @FXML
    private void setupWalkButton() {
        walkButton.setOnAction(e -> {
            startPath("walk");
        });
    }

    @FXML
    private void setupBikeButton() {
        bikeButton.setOnAction(e -> {
            startPath("bike");
        });
    }

    public static double ax;
    public static double ay;
    public static double bx;
    public static double by;

    @FXML
    private void pathDrawBut(List<String> ls) throws Exception {
        String start;
        String end;

        for (int i = 0; i < ls.size() - 1; i++) {
            start = ls.get(i);
            end = ls.get(i+1);
            
            mapView.removeLayer(cL);

         ax = getLat(start);
         ay = getLon(start);

         bx = getLat(end);
         by = getLon(end);

         pointStart = new MapPoint(ax, ay);
         pointEnd = new MapPoint(bx, by);


        cL = new CustomMapLayer();
        mapView.addLayer(cL);

        mapView.setZoom(mapView.getZoom()+0.01); // This a dirty trick to make the map refresh after pressing the button


        System.out.println(ax);
        System.out.println(ay);

        }
    }

    public void startPath(String input){
        startText = startZip.getText();
        endText = endZip.getText();

        try {
            System.out.println(searchDijkstra(getGraph(), startText, endText));
            pathDrawBut(searchDijkstra(getGraph(), startText, endText));

            if(input.equals("walk")) {
                walkBikeCalcs(startText, endText, "walk");
            }else if(input.equals("bike")) {
                walkBikeCalcs(startText, endText, "bike");
            } else if(input.equals("transport")) {
                //add transport logic
            }
        }
        catch (Exception e) {
            ErrorPopup popup = new ErrorPopup(e.getMessage());
            popup.display();
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupToolTip();
        hashTheData();
        graphTheData();
        mapView = createMapView();
        mapPane.getChildren().add(mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);
        System.out.println(mapView.getZoom());


    }


    private MapView createMapView() {
        MapView newMapView = new MapView();
        newMapView.setPrefSize(667,528);
        newMapView.flyTo(0,point,0.1);
        newMapView.setZoom(12);
        return newMapView;
    }


}