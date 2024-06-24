package com.example.Frontend;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.image.ImageView;
import org.geojson.FeatureCollection;

import com.example.Controller.AlgoConnecter;
import com.example.Controller.GraphController;
import com.example.Controller.HashController;
import com.example.Controller.SearchConnector;
import com.example.Frontend.PathStrategy.BikingPath;
import com.example.Frontend.PathStrategy.Path;
import com.example.Frontend.PathStrategy.TransportPath;
import com.example.Frontend.PathStrategy.WalkingPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


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

    private int startScore = 0;

    @FXML
    public ImageView one;

    @FXML
    public ImageView two;

    @FXML
    public ImageView three;

    @FXML
    public ImageView four;

    @FXML
    public ImageView five;

    @FXML
    public ImageView six;

    @FXML
    public ImageView seven;
    @FXML
    public ImageView eight;
    @FXML
    public ImageView nine;
    @FXML
    public ImageView ten;
    @FXML
    public ImageView eleven;
    @FXML
    public ImageView twelve;
    @FXML
    public ImageView fifteen;
    @FXML
    public ImageView fiftySeven;

    public void hideAllBusSigns(){
        one.setVisible(false);
        two.setVisible(false);
        three.setVisible(false);
        four.setVisible(false);
        five.setVisible(false);
        six.setVisible(false);
        seven.setVisible(false);
        eight.setVisible(false);
        nine.setVisible(false);
        ten.setVisible(false);
        eleven.setVisible(false);
        twelve.setVisible(false);
        fifteen.setVisible(false);
        fiftySeven.setVisible(false);
    }



    @FXML
    private Label startScoreLabel;

    @FXML
    private Label endScoreLabel;

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
    private Button heatmapButton;

    @FXML
    private TextField startZip;

    @FXML
    private TextField endZip;

    @FXML
    public Pane mapPane;

    @FXML
    MapPoint point = new MapPoint(50.849365,5.694548);


    @FXML
    public Label busInfo;

    public CustomMapLayer cL = new CustomMapLayer();

    public CustomHeatmapLayer cLH = new CustomHeatmapLayer(this);

    public UIController uiController = this;
    public CustomMapLayerBus cLB;

    @FXML
    public MapPoint pointStart;
    public MapPoint pointEnd;

    @FXML
    public void addBusInfo(String info) {
        busInfo.setText(info);
        busInfo.setVisible(false);

        String busNos = info;
        String[] arrString = busNos.split("-");
        for (String a: arrString){
            switch (a){
                case "1":
                    one.setVisible(true);
                    break;
                case "2":
                    two.setVisible(true);
                    break;
                case "3":
                    three.setVisible(true);
                case "4":
                    four.setVisible(true);
                    break;
                case "5":
                    five.setVisible(true);
                    break;
                case "6":
                    six.setVisible(true);
                    break;
                case "7":
                    seven.setVisible(true);
                    break;
                case "8":
                    eight.setVisible(true);
                    break;
                case "9":
                    nine.setVisible(true);
                    break;
                case "10":
                    ten.setVisible(true);
                    break;
                case "11":
                    eleven.setVisible(true);
                    break;
                case "12":
                    twelve.setVisible(true);
                    break;
                case "15":
                    fifteen.setVisible(true);
                    break;
                case "57":
                    fiftySeven.setVisible(true);
                    break;
                default:
                    System.out.println("NO MATCH");
                    System.out.println(a);


            }
        }
    }


    public void calculateDistance(String startText, String endText) throws Exception {
        System.out.println(startText);
        System.out.println(endText);

        double distance1 = getDistance(startText, endText);
        double roundedNumber = Math.round(distance1 * 1000.0) / 1000.0;
        distance.setText("Distance: " + roundedNumber + " km");
        System.out.println(getDistance(startText, endText));
    }

    @FXML
    private void setupWalkButton() {
            startPath(new WalkingPath());
            deactivateButtons();
    }

    @FXML
    private void setupBikeButton() {
        startPath(new BikingPath());
        deactivateButtons();
    }

    @FXML
    private void setupBusButton() {
            startPath(new TransportPath(this));
            deactivateButtons();
    }

    @FXML
    private void setupHeatmapButton() throws Exception {
            drawHeatmap(getAllZipCodes());
            deactivateButtons();
    }

    public static double ax;
    public static double ay;
    public static double bx;
    public static double by;

    @FXML
    private void pathDrawBut(List<String> ls)  {
        mapView.removeLayer(cL);
        mapView.removeLayer(cLB);
        mapView.removeLayer(cLH);
        cL = new CustomMapLayer(ls);
        mapView.addLayer(cL);
        mapView.setZoom(mapView.getZoom()+0.01); // This a dirty trick to make the map refresh after pressing the button
    }

    public void pathDrawBus(String start, String end) throws Exception {
        hideAllBusSigns();

        mapView.removeLayer(cL);
        mapView.removeLayer(cLB);
        mapView.removeLayer(cLH);

        Task task = new Task<CustomMapLayerBus>() {
            @Override
            protected CustomMapLayerBus call() throws Exception {
                return new CustomMapLayerBus(start, end);
            }
        };
        new Thread(task).start();
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                cLB = (CustomMapLayerBus) task.getValue();
                mapView.addLayer(cLB);
                mapView.setZoom(mapView.getZoom()+0.01); // This a dirty trick to make the map refresh after pressing the button
                distance.setText("Distance: " + cLB.trip.traveledDistance + " km");
                System.out.println(cLB.trip.traveledDistance);

                timeLabel.setText("Transport time: " + cLB.trip.travelTime + " min");
                System.out.println("Transport time: " + cLB.trip.travelTime);

                addBusInfo(cLB.trip.tripNumber);
                activateButtons();
            }
        });
    }

    @FXML
    private void drawHeatmap(List<String> ls) throws Exception {
        mapView.removeLayer(cL);
        mapView.removeLayer(cLB);
        mapView.removeLayer(cLH);

        Task task = new Task<CustomHeatmapLayer>() {
            @Override
            protected CustomHeatmapLayer call() throws Exception {
                return new CustomHeatmapLayer(ls, uiController);
            }
        };
        new Thread(task).start();
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                cLH = (CustomHeatmapLayer) task.getValue();
                mapView.addLayer(cLH);
                mapView.setZoom(mapView.getZoom()+0.01); // This a dirty trick to make the map refresh after pressing the button
                activateButtons();
            }
        });
    }

    @FXML
    public void deactivateButtons(){
        if (!isFail) {
            walkButton.setDisable(true);
            bikeButton.setDisable(true);
            transportButton.setDisable(true);
            startZip.setDisable(true);
            endZip.setDisable(true);
            heatmapButton.setDisable(true);
        }
        isFail = false;
    }

    @FXML
    public void activateButtons(){
        walkButton.setDisable(false);
        bikeButton.setDisable(false);
        transportButton.setDisable(false);
        startZip.setDisable(false);
        endZip.setDisable(false);
        heatmapButton.setDisable(false);
    }

private boolean isFail = false;

    private void startPath(Path strategy) {
        String startText = startZip.getText();
        String endText = endZip.getText();
        if (!mapContains(endText) || !mapContains(startText)) {
            ErrorPopup popup = new ErrorPopup("invalid postal code");
            popup.display();
            System.out.println("not found");
            isFail = true;
        } else {
            try {
                if (!(strategy instanceof TransportPath)) {
                    getAlgorithm(startText, endText);
                    calculateDistance(startText, endText);
                }
                strategy.calculateTime(startText, endText, timeLabel);
                startScoreLabel.setText("Score: " + String.valueOf(accessibility(startText)));
                endScoreLabel.setText("Score: " + String.valueOf(accessibility(endText)));
            } catch (Exception e) {
                // Show error popup on the JavaFX Application Thread
                Platform.runLater(() -> {
                    ErrorPopup popup = new ErrorPopup("algo error");
                    popup.display();
                });
            }
        }
    }


private List<String> result;

    public void getAlgorithm(String startText, String endText){
     //   busInfo.setVisible(false);
        hideAllBusSigns();
        System.out.println("Calculating the way with Dijkstra...");
            Task task = new Task<List<String>>() {
                @Override
                protected List<String> call() throws Exception {
                    return searchDijkstra(getGraph(), startText, endText);
                }
            };
            new Thread(task).start();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    pathDrawBut((List<String>) task.getValue());
                    activateButtons();
                }
            });
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hashTheData();
        graphTheData();
        graphTheDatabase();
        mapView = createMapView();
        mapPane.getChildren().add(mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);
    }

    private MapView createMapView() {
        MapView newMapView = new MapView();
        newMapView.setPrefSize(667,538);
        newMapView.flyTo(0,point,0.1);
        newMapView.setZoom(12);
        return newMapView;
    }
    final double centerLatitude = 50.851368;
    final double centerLongitude = 5.690973;

    private int accessibility(String postalCode) throws Exception {
        int score = 0;
       score += (calculateDistanceHaversine2(getLat(postalCode), getLon(postalCode), centerLatitude, centerLongitude));

        InputStream inputStream = new FileInputStream("src/main/java/com/example/Frontend/tourism.geojson");
        InputStream inputStream2 = new FileInputStream("src/main/java/com/example/Frontend/amenity.geojson");
        InputStream inputStream3 = new FileInputStream("src/main/java/com/example/Frontend/shop.geojson");

        FeatureCollection featureCollectionAll = new ObjectMapper().readValue(inputStream, FeatureCollection.class);
        FeatureCollection featureCollection2 = new ObjectMapper().readValue(inputStream2, FeatureCollection.class);
        FeatureCollection featureCollection3 = new ObjectMapper().readValue(inputStream3, FeatureCollection.class);

        featureCollectionAll.addAll(featureCollection2.getFeatures());
        featureCollectionAll.addAll(featureCollection3.getFeatures());

        String regex = "longitude=([\\d.]+), latitude=([\\d.]+)";
        Pattern p = Pattern.compile(regex);


        for (int i = 0; i < featureCollectionAll.getFeatures().size()-1; i++){
            String a = featureCollectionAll.getFeatures().get(i).toString();

            if (a.contains("museum")){
                String geoCord = featureCollectionAll.getFeatures().get(i).getGeometry().toString();
                Matcher m = p.matcher(geoCord);
                m.find();

                double longitudeLoop = Double.parseDouble(m.group(1));
                double latitudeLoop = Double.parseDouble(m.group(2));

                if(calculateDistanceHaversine2(getLat(postalCode), getLon(postalCode), latitudeLoop, longitudeLoop) < 0.5){
                    System.out.println(calculateDistanceHaversine2(getLat(postalCode), getLon(postalCode), latitudeLoop, longitudeLoop));
                    System.out.println(a);
                    score++;
                }
            }

        }

        return score;
    }

    public Circle createHeatmapPoint(String postalCode) throws Exception {
        int score = accessibility(postalCode);
        Circle icon;

        if (score > 5) {
            icon = new Circle(5, Color.GREEN);
        } else if (score > 3) {
            icon = new Circle(5, Color.YELLOW);
        } else if (score > 1) {
            icon = new Circle(5, Color.ORANGE);
        } else {
            icon = new Circle(5, Color.RED);
        }
        return icon;
    }
}
