package com.example.Frontend;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.function.Consumer;
import com.example.Backend.Graph.Trip;
import com.example.Backend.RequestDatabase;
import javafx.scene.control.Tooltip;

import com.example.Backend.Algorithms.AccesibilityFormula;
import com.example.Frontend.CustomMapLayers.CustomHeatmapLayer;
import com.example.Frontend.CustomMapLayers.CustomMapLayer;
import com.example.Frontend.CustomMapLayers.CustomMapLayerBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.ImageView;

import com.example.Controller.AlgoConnecter;
import com.example.Controller.GraphController;
import com.example.Controller.HashController;
import com.example.Controller.SearchConnector;
import com.example.Frontend.PathStrategy.BikingPath;
import com.example.Frontend.PathStrategy.Path;
import com.example.Frontend.PathStrategy.TransportPath;
import com.example.Frontend.PathStrategy.WalkingPath;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class UIController implements Initializable, AlgoConnecter, HashController, GraphController, SearchConnector {
    private AccesibilityFormula accesibilityFormula = new AccesibilityFormula();

    @FXML
    private MapView mapView;

    @FXML
    private ChoiceBox<String> hourSelection;

    @FXML
    private ChoiceBox<String> minuteSelection;

    private int startScore = 0;
    private int endScore = 0;

    private int score = 0;

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

    @FXML
    public Button currentTime;

    @FXML
    private Label startScoreLabel;

    @FXML
    private Label endScoreLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label distance;

    @FXML
    private Label scoreLabel;

    @FXML
    private Button walkButton;

    @FXML
    private Button bikeButton;

    @FXML
    private Button transportButton;

    @FXML
    private Button heatmapButton;

    @FXML
    private Button calculateScoreButton;

    @FXML
    private TextField startZip;

    @FXML
    private TextField endZip;

    @FXML
    private TextField checkZip;

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
    private Task<?> activeTask;
    private Task<Boolean> databaseTask;
    private boolean closed;


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
    public void onCurrentTimePressed(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = LocalTime.now().format(formatter);
        String hour = time.substring(0,2);
        String minute = time.substring(3,5);
        hourSelection.setValue(hour);
        minuteSelection.setValue(minute);
    }


    public void setUpTimeSelection(){
        ObservableList<String> obListHour = FXCollections.observableArrayList();
        for(int i = 0; i < 24; i++){
            if (0<=i && i<10){
                obListHour.add("0" + Integer.toString(i));
            }
            else {
                obListHour.add(Integer.toString(i));
            }
        }
        hourSelection.setItems(obListHour);
        hourSelection.setValue("12");

        ObservableList<String> obListMin = FXCollections.observableArrayList();
        for(int i = 0; i < 60; i++){
            if (0<=i && i<10){
                obListMin.add("0" + Integer.toString(i));
            }
            else {
                obListMin.add(Integer.toString(i));
            }
        }
        minuteSelection.setItems(obListMin);
        minuteSelection.setValue("00");
    }


    public void calculateDistance(String startText, String endText) throws Exception {
        double roundedNumber = Math.round(getDistance(startText, endText) * 1000.0) / 1000.0;
        distance.setText("Distance: " + roundedNumber + " km");
    }

    @FXML
    private void setupWalkButton() {
        routingUI();
        startPath(new WalkingPath());
    }

    @FXML
    private void setupBikeButton() {
        routingUI();
        startPath(new BikingPath());
    }

    @FXML
    private void setupBusButton() {
        routingUI();
        startPath(new TransportPath(this));
    }

    @FXML
    private void setupHeatmapButton() {
        if (activeTask != null) return;
        hideAllBusSigns();
        drawHeatmap(getAllZipCodes());
    }

    @FXML
    private void setupCalculateButton() {
        if (activeTask != null) return;
        final String postcode;
        try {
            postcode = validatedPostcode(checkZip, "Maastricht");
        } catch (IllegalArgumentException exception) {
            showError(exception);
            return;
        }
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                return (int) accesibilityFormula.accessibility(postcode);
            }
        };
        runTask(task, value -> {
            score = value;
            scoreLabel.setText("Score: " + score);
        });
    }

    @FXML
    private void pathDrawBut(List<String> postcodes) {
        clearMapLayers();
        cL = new CustomMapLayer(postcodes);
        mapView.addLayer(cL);
        mapView.setZoom(mapView.getZoom() + 0.01);
    }

    public void pathDrawBus(String start, String end) {
        if (activeTask != null) return;
        if (!dbg.isAvailable()) {
            showError(new IllegalArgumentException("Transit database is unavailable."));
            return;
        }
        hideAllBusSigns();
        String time = hourSelection.getValue() + ":" + minuteSelection.getValue() + ":00";
        Task<RouteResult> task = new Task<>() {
            @Override protected RouteResult call() throws Exception {
                Trip trip = new RequestDatabase<String>().findBusRoute(start, end, time);
                if (trip == null || trip.tripStructure == null || trip.tripStructure.isEmpty()) {
                    throw new IllegalArgumentException("No transit route was found for that departure time.");
                }
                return new RouteResult(null, trip, trip.traveledDistance, trip.travelTime,
                        (int) accesibilityFormula.accessibility(start), (int) accesibilityFormula.accessibility(end));
            }
        };
        runTask(task, result -> {
            CustomMapLayerBus layer = new CustomMapLayerBus(result.trip, this);
            clearMapLayers();
            cLB = layer;
            mapView.addLayer(cLB);
            mapView.setZoom(mapView.getZoom() + 0.01);
            distance.setText("Distance: " + result.distance + " km");
            timeLabel.setText("Transport time: " + result.minutes + " min");
            showScores(result);
        });
    }

    private void drawHeatmap(List<String> postcodes) {
        Task<List<Double>> task = new Task<>() {
            @Override protected List<Double> call() throws Exception {
                List<Double> scores = new ArrayList<>();
                for (String postcode : postcodes) {
                    if (isCancelled()) return null;
                    scores.add(accesibilityFormula.accessibility(postcode));
                }
                return scores;
            }
        };
        runTask(task, scores -> {
            CustomHeatmapLayer layer = new CustomHeatmapLayer(postcodes, scores, this);
            clearMapLayers();
            cLH = layer;
            mapView.addLayer(cLH);
            mapView.setZoom(mapView.getZoom() + 0.01);
            heatmapUI();
        });
    }

    private void clearMapLayers() {
        if (cL != null) mapView.removeLayer(cL);
        if (cLB != null) mapView.removeLayer(cLB);
        if (cLH != null) mapView.removeLayer(cLH);
    }

    @FXML
    public void deactivateButtons() {
        walkButton.setDisable(true);
        bikeButton.setDisable(true);
        transportButton.setDisable(true);
        startZip.setDisable(true);
        endZip.setDisable(true);
        heatmapButton.setDisable(true);
        hourSelection.setDisable(true);
        minuteSelection.setDisable(true);
        currentTime.setDisable(true);
        checkZip.setDisable(true);
        calculateScoreButton.setDisable(true);
    }

    @FXML
    public void activateButtons() {
        walkButton.setDisable(false);
        bikeButton.setDisable(false);
        transportButton.setDisable(!dbg.isAvailable());
        startZip.setDisable(false);
        endZip.setDisable(false);
        heatmapButton.setDisable(false);
        hourSelection.setDisable(false);
        minuteSelection.setDisable(false);
        currentTime.setDisable(false);
        checkZip.setDisable(false);
        calculateScoreButton.setDisable(false);
    }

    private void heatmapUI() {
        startScoreLabel.setVisible(false);
        endScoreLabel.setVisible(false);
        timeLabel.setVisible(false);
        distance.setVisible(false);
        checkZip.setVisible(true);
        scoreLabel.setVisible(true);
        calculateScoreButton.setVisible(true);
    }

    private void routingUI() {
        startScoreLabel.setVisible(true);
        endScoreLabel.setVisible(true);
        timeLabel.setVisible(true);
        distance.setVisible(true);
        checkZip.setVisible(false);
        scoreLabel.setVisible(false);
        calculateScoreButton.setVisible(false);
    }

    private void startPath(Path strategy) {
        if (activeTask != null) return;
        final String start;
        final String end;
        try {
            start = validatedPostcode(startZip, "departure");
            end = validatedPostcode(endZip, "destination");
            if (start.equals(end)) {
                throw new IllegalArgumentException("Choose two different postcodes.");
            }
        } catch (IllegalArgumentException exception) {
            showError(exception);
            return;
        }
        if (strategy instanceof TransportPath) {
            pathDrawBus(start, end);
        } else {
            calculateLocalRoute(start, end, strategy instanceof BikingPath);
        }
    }

    private String validatedPostcode(TextField field, String name) {
        String postcode = PostcodeInput.requireValid(field.getText(), name);
        if (!mapContains(postcode)) {
            throw new IllegalArgumentException("The " + name + " postcode is not in the Maastricht dataset.");
        }
        field.setText(postcode);
        return postcode;
    }

    public void getAlgorithm(String start, String end) {
        calculateLocalRoute(start, end, false);
    }

    private void calculateLocalRoute(String start, String end, boolean biking) {
        if (activeTask != null) return;
        hideAllBusSigns();
        Task<RouteResult> task = new Task<>() {
            @Override protected RouteResult call() throws Exception {
                List<String> path = searchDijkstra(getGraph(), start, end);
                if (path == null || path.isEmpty()) {
                    throw new IllegalArgumentException("No route connects those postcodes. Try another pair.");
                }
                return new RouteResult(path, null, getDistance(start, end),
                        (long) (biking ? cyclingTime(start, end) : walkingTime(start, end)),
                        (int) accesibilityFormula.accessibility(start), (int) accesibilityFormula.accessibility(end));
            }
        };
        runTask(task, result -> {
            pathDrawBut(result.path);
            distance.setText("Distance: " + Math.round(result.distance * 1000.0) / 1000.0 + " km");
            timeLabel.setText((biking ? "Biking" : "Walking") + " time: " + result.minutes + " min");
            showScores(result);
        });
    }

    private void showScores(RouteResult result) {
        startScore = result.startScore;
        endScore = result.endScore;
        startScoreLabel.setText("Score: " + startScore);
        endScoreLabel.setText("Score: " + endScore);
    }

    private <T> void runTask(Task<T> task, Consumer<T> onSuccess) {
        if (closed || activeTask != null) return;
        activeTask = task;
        deactivateButtons();
        task.setOnSucceeded(event -> {
            if (closed || activeTask != task) return;
            Exception failure = null;
            try {
                onSuccess.accept(task.getValue());
            } catch (Exception exception) {
                failure = exception;
            } finally {
                finishTask(task);
            }
            if (failure != null) showError(failure);
        });
        task.setOnFailed(event -> {
            if (closed || activeTask != task) return;
            finishTask(task);
            showError(task.getException());
        });
        task.setOnCancelled(event -> finishTask(task));
        Thread calculation = new Thread(task, "transitor-calculation");
        calculation.setDaemon(true);
        calculation.start();
    }

    private void finishTask(Task<?> task) {
        if (closed || activeTask != task) return;
        activeTask = null;
        activateButtons();
    }

    private void showError(Throwable failure) {
        String message = failure instanceof IllegalArgumentException ? failure.getMessage()
                : "The calculation failed. Try another postcode or departure time.";
        new ErrorPopup(message);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpTimeSelection();
        hashTheData();
        graphTheData();
        mapView = createMapView();
        mapPane.getChildren().add(mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);
        transportButton.setDisable(true);
        databaseTask = new Task<>() {
            @Override protected Boolean call() {
                return dbg.graphTheDatabase();
            }
        };
        databaseTask.setOnSucceeded(event -> {
            if (closed) return;
            transportButton.setDisable(activeTask != null || !databaseTask.getValue());
            if (!databaseTask.getValue()) transportButton.setTooltip(new Tooltip(dbg.getUnavailableReason()));
        });
        databaseTask.setOnFailed(event -> {
            if (!closed) transportButton.setTooltip(new Tooltip("Transit database is unavailable."));
        });
        Thread databaseLoader = new Thread(databaseTask, "transitor-database");
        databaseLoader.setDaemon(true);
        databaseLoader.start();
    }

    public void close() {
        closed = true;
        if (activeTask != null) activeTask.cancel(true);
        if (databaseTask != null) databaseTask.cancel(true);
    }

    private static final class RouteResult {
        private final List<String> path;
        private final Trip trip;
        private final double distance;
        private final long minutes;
        private final int startScore;
        private final int endScore;
        private RouteResult(List<String> path, Trip trip, double distance, long minutes, int startScore, int endScore) {
            this.path = path;
            this.trip = trip;
            this.distance = distance;
            this.minutes = minutes;
            this.startScore = startScore;
            this.endScore = endScore;
        }
    }

    private MapView createMapView() {
        MapView newMapView = new MapView();
        newMapView.setPrefSize(667,538);
        newMapView.flyTo(0,point,0.1);
        newMapView.setZoom(12);
        return newMapView;
    }

}
