package com.example.Frontend;

import java.net.URL;
import java.util.List;
import java.util.Objects;
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
import javafx.scene.control.*;
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

    public static boolean algo = true;

    @FXML
    private MapView mapView;

    @FXML
    private ChoiceBox<String> choiceBox;

    private String[] algos = {"Dijkstra", "Astar"};

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
    private void pathDrawBut(List<String> ls)  {
        mapView.removeLayer(cL);
        cL = new CustomMapLayer(ls);
        mapView.addLayer(cL);
        mapView.setZoom(mapView.getZoom()+0.01); // This a dirty trick to make the map refresh after pressing the button
    }

    public void startPath(String input){
        startText = startZip.getText();
        endText = endZip.getText();

        try {
            if (Objects.equals(choiceBox.getSelectionModel().getSelectedItem(), "Dijkstra")) {
                System.out.println("Calculating the way with Dijkstra...");
                pathDrawBut(searchDijkstra(getGraph(), startText, endText));
            }else {
                System.out.println("Calculating the way with Astar...");
                pathDrawBut(searchAstar(getGraph(), startText, endText));

            }

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
        hashTheData();
        graphTheData();
        mapView = createMapView();
        mapPane.getChildren().add(mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);
        choiceBox.getItems().addAll(algos);
        choiceBox.setValue("Dijkstra");

        choiceBox.show();

    }


    private MapView createMapView() {
        MapView newMapView = new MapView();
        newMapView.setPrefSize(667,538);
        newMapView.flyTo(0,point,0.1);
        newMapView.setZoom(12);
        return newMapView;
    }


}