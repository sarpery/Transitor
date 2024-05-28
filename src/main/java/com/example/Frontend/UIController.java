package com.example.Frontend;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import com.example.Controller.AlgoConnecter;
import com.example.Controller.GraphController;
import com.example.Controller.HashController;
import com.example.Controller.SearchConnector;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


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
    public CustomMapLayerBus cLB;




    @FXML
    public MapPoint pointStart;
    public MapPoint pointEnd;




    public String startText;
    public String endText;

    public void calculateDistance(List<String> list) throws Exception {
        System.out.println(startText);
        System.out.println(endText);

        String start ;
        String end ;
        double distance1 = 0;

        for (int i = 0; i < list.size()-1; i++) {
            start = list.get(i);
            end = list.get(i+1);
             distance1 = distance1 + getDistance(start, end);

        }

        
        double roundedNumber = Math.round(distance1 * 1000.0) / 1000.0;
        distance.setText("Distance: " + roundedNumber + " km");
        System.out.println(distance1);
    }

    public void calculateWalkTime(List<String> ls) throws Exception {

        String start ;
        String end ;
        double distance = 0;

        for (int i = 0; i < ls.size() - 1 ; i++) {
            start = ls.get(i);
            end = ls.get(i + 1);
            distance =  distance + getDistance(start, end);
        }
        int time = (int) (distance/0.075);
            
            timeLabel.setText("Walking time: " + time + " min");
            System.out.println("Walking time: " + time);
    }

    public void calculateBikeTime(List<String> ls) throws Exception {

        String start ;
        String end ;
        double distance = 0;

        for (int i = 0; i < algos.length; i++) {
            start = ls.get(i);
            end = ls.get(i + 1);
            distance =  distance + getDistance(start, end);
        }
            int time = (int) (distance/0.4);
            timeLabel.setText("Biking time: " + time + " min");
            System.out.println("Biking time: " + time);
    }

    public void calculateBusTime(String startText, String endText) throws Exception {
        //add bus time logic
    }

    @FXML
    private void setupWalkButton() {
        walkButton.setOnAction(e -> {
            startPath("walk");
        });
    }

    /*
    not working action listener with button disabling
    @FXML
    private void setupWalkButton() {
        walkButton.setOnAction(e -> {
            walkButton.setDisable(true);
            bikeButton.setDisable(true);
            transportButton.setDisable(true);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    startPath("walk");
                    return null;
                }

                @Override
                protected void succeeded() {
                    walkButton.setDisable(false);
                    bikeButton.setDisable(false);
                    transportButton.setDisable(false);
                }

                @Override
                protected void failed() {
                    walkButton.setDisable(false);
                    bikeButton.setDisable(false);
                    transportButton.setDisable(false);
                }

                @Override
                protected void cancelled() {
                    walkButton.setDisable(false);
                    bikeButton.setDisable(false);
                    transportButton.setDisable(false);
                }
            };

            new Thread(task).start();
        });
    }
*/

    @FXML
    private void setupBikeButton() {
        bikeButton.setOnAction(e -> {
            startPath("bike");
        });
    }

    @FXML
    private void setupBusButton() {
        transportButton.setOnAction(e -> {
            startPath("transport");
        });
    }

    public static double ax;
    public static double ay;
    public static double bx;
    public static double by;

    @FXML
    private void pathDrawBut(List<String> ls)  {
        mapView.removeLayer(cL);
        mapView.removeLayer(cLB);
        cL = new CustomMapLayer(ls);
        mapView.addLayer(cL);
        mapView.setZoom(mapView.getZoom()+0.01); // This a dirty trick to make the map refresh after pressing the button
    }

    private void pathDrawBus(String start, String end){
        mapView.removeLayer(cL);
        mapView.removeLayer(cLB);
        cLB = new CustomMapLayerBus(start, end);
        mapView.addLayer(cLB);
        mapView.setZoom(mapView.getZoom()+0.01); // This a dirty trick to make the map refresh after pressing the button

        distance.setText("Distance: " + cLB.trip.traveledDistance + " km");
        System.out.println(cLB.trip.traveledDistance);

        timeLabel.setText("Transport time: " + cLB.trip.travelTime + " min");
            System.out.println("Transport time: " + cLB.trip.travelTime);
    }

    public void startPath(String input) {
        String startText = startZip.getText();
        String endText = endZip.getText();

        try {
            if (input.equals("walk")) {
                List<String> list = getAlgorithm(startText, endText);
                
                calculateDistance(list);
                calculateWalkTime(list);
            } else if (input.equals("bike")) {
                List<String> list1 = getAlgorithm(startText, endText);
                calculateDistance(list1);
                calculateBikeTime(list1);
            } else if (input.equals("transport")) {
                pathDrawBus(startText, endText);
            }
        } catch (Exception e) {
            // Show error popup on the JavaFX Application Thread
            Platform.runLater(() -> {
                ErrorPopup popup = new ErrorPopup(e.getMessage());
                popup.display();
            });
        }
    }

    public List<String> getAlgorithm(String startText, String endText){
        if (Objects.equals(choiceBox.getSelectionModel().getSelectedItem(), "Dijkstra")) {
            List<String> list = searchDijkstra(getGraph(), startText, endText);
            System.out.println("Calculating the way with Dijkstra...");
            pathDrawBut(list);
            return list;
        } else {
            List<String> list = searchAstar(getGraph(), startText, endText);
            System.out.println("Calculating the way with Astar...");
            pathDrawBut(list);
            return list;
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