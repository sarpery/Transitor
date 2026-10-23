package com.example.Frontend;

import com.example.Backend.Algorithms.AccesibilityFormula;
import com.example.Backend.Graph.Trip;
import com.example.Backend.RequestDatabase;
import com.example.Controller.AlgoConnecter;
import com.example.Controller.GraphController;
import com.example.Controller.HashController;
import com.example.Controller.SearchConnector;
import com.example.Frontend.CustomMapLayers.CustomHeatmapLayer;
import com.example.Frontend.CustomMapLayers.CustomMapLayer;
import com.example.Frontend.CustomMapLayers.CustomMapLayerBus;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import java.net.URL;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/** Coordinates the view while keeping routing and scoring off the JavaFX thread. */
public class UIController implements Initializable, AlgoConnecter, HashController, GraphController, SearchConnector {
    private static final Logger LOG = Logger.getLogger(UIController.class.getName());
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");
    private static final Set<String> CONNECTOR_CODES = Set.of("9999TT", "9999ZZ", "9999AA", "9999GG", "6200AA");
    private final AccesibilityFormula accessibility = new AccesibilityFormula();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "transitor-calculation");
        thread.setDaemon(true);
        return thread;
    });

    @FXML private TextField startZip;
    @FXML private TextField endZip;
    @FXML private TextField checkZip;
    @FXML private ChoiceBox<String> hourSelection;
    @FXML private ChoiceBox<String> minuteSelection;
    @FXML private Button currentTime;
    @FXML private Button walkButton;
    @FXML private Button bikeButton;
    @FXML private Button transportButton;
    @FXML private Button heatmapButton;
    @FXML private Button calculateScoreButton;
    @FXML private Button exampleButton;
    @FXML private Button swapButton;
    @FXML private Button cancelButton;
    @FXML private Label distance;
    @FXML private Label timeLabel;
    @FXML private Label startScoreLabel;
    @FXML private Label endScoreLabel;
    @FXML private Label scoreLabel;
    @FXML private Label summaryTitle;
    @FXML private Label routeDescription;
    @FXML private Label estimateNote;
    @FXML private Label statusLabel;
    @FXML private Label transitNote;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox accessibilityPane;
    @FXML private VBox emptyState;
    @FXML private HBox heatmapLegend;
    @FXML private FlowPane routeLegend;
    @FXML private StackPane mapPane;

    private MapView mapView;
    private MapLayer activeLayer;
    private Task<?> activeTask;
    private Task<Boolean> databaseTask;
    private boolean closed;
    private boolean busy;
    private boolean transitAvailable;
    private Map<String, Double> cachedHeatmap;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        for (int hour = 0; hour < 24; hour++) hourSelection.getItems().add(String.format(Locale.ROOT, "%02d", hour));
        for (int minute = 0; minute < 60; minute++) minuteSelection.getItems().add(String.format(Locale.ROOT, "%02d", minute));
        onCurrentTimePressed();
        startZip.setOnAction(event -> endZip.requestFocus());
        endZip.setOnAction(event -> setupWalkButton());
        checkZip.setOnAction(event -> setupCalculateButton());
        accessibilityPane.managedProperty().bind(accessibilityPane.visibleProperty());
        heatmapLegend.managedProperty().bind(heatmapLegend.visibleProperty());
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
        transitNote.managedProperty().bind(transitNote.visibleProperty());
        mapView = new MapView();
        mapView.prefWidthProperty().bind(mapPane.widthProperty());
        mapView.prefHeightProperty().bind(mapPane.heightProperty());
        mapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mapPane.getChildren().add(0, mapView);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(mapPane.widthProperty());
        clip.heightProperty().bind(mapPane.heightProperty());
        mapPane.setClip(clip);
        resetMap();
        showRouteLegend();
        hashTheData();
        graphTheData();
        setBusy(false);
        setStatus("Choose two Maastricht postcodes, or try the example route.", false);
        initializeTransit();
    }

    private void initializeTransit() {
        transitNote.setText("Checking optional transit data. Walking, cycling and accessibility are available.");
        databaseTask = new Task<>() {
            @Override protected Boolean call() {
                return dbg.graphTheDatabase();
            }
        };
        databaseTask.setOnSucceeded(event -> {
            if (closed) return;
            transitAvailable = databaseTask.getValue();
            transitNote.setVisible(!transitAvailable);
            if (!transitAvailable) {
                transitNote.setText("Walking, cycling and accessibility work offline. Connect the transit database to enable buses.");
                transportButton.setTooltip(new Tooltip(dbg.getUnavailableReason()));
            }
            transportButton.setDisable(busy || !transitAvailable);
        });
        databaseTask.setOnFailed(event -> {
            if (closed) return;
            transitNote.setText("Transit data could not be loaded. Walking, cycling and accessibility are still available.");
            transportButton.setTooltip(new Tooltip("Check the local transit database configuration, then restart Transitor."));
            LOG.log(Level.WARNING, "Optional transit data could not be loaded", databaseTask.getException());
        });
        Thread databaseLoader = new Thread(databaseTask, "transitor-database");
        databaseLoader.setDaemon(true);
        databaseLoader.start();
    }

    @FXML
    public void onCurrentTimePressed() {
        LocalTime now = LocalTime.now();
        hourSelection.setValue(String.format(Locale.ROOT, "%02d", now.getHour()));
        minuteSelection.setValue(String.format(Locale.ROOT, "%02d", now.getMinute()));
    }

    @FXML
    private void useExample() {
        startZip.setText("6211 AB");
        endZip.setText("6211 AS");
        setupWalkButton();
    }

    @FXML
    private void swapPostcodes() {
        String previousStart = startZip.getText();
        startZip.setText(endZip.getText());
        endZip.setText(previousStart);
    }

    @FXML private void setupWalkButton() { startRoute(false, false); }
    @FXML private void setupBikeButton() { startRoute(true, false); }
    @FXML private void setupBusButton() { startRoute(false, true); }

    private void startRoute(boolean cycling, boolean transit) {
        if (busy || (transit && !transitAvailable)) return;
        final String start;
        final String end;
        try {
            start = validate(startZip, "departure");
            end = validate(endZip, "destination");
            if (start.equals(end)) throw new IllegalArgumentException("Choose two different postcodes for your route.");
        } catch (IllegalArgumentException exception) {
            setStatus(exception.getMessage(), true);
            return;
        }
        final String departureTime = hourSelection.getValue() + ":" + minuteSelection.getValue() + ":00";
        String mode = transit ? "Transit" : cycling ? "Cycling" : "Walking";
        accessibilityPane.setVisible(false);
        Task<RouteResult> task = new Task<>() {
            @Override
            protected RouteResult call() throws Exception {
                List<String> postcodes = null;
                Trip trip = null;
                double kilometers;
                long minutes;
                if (transit) {
                    trip = new RequestDatabase<String>().findBusRoute(start, end, departureTime);
                    if (trip == null || trip.tripStructure == null || trip.tripStructure.isEmpty()) {
                        throw new IllegalArgumentException("No transit connection was found at that time. Try another departure.");
                    }
                    kilometers = trip.traveledDistance;
                    minutes = trip.travelTime;
                } else {
                    postcodes = searchDijkstra(getGraph(), start, end);
                    if (postcodes == null || postcodes.isEmpty()) {
                        throw new IllegalArgumentException("These postcodes are not connected in the bundled route network. Try another pair.");
                    }
                    kilometers = getDistance(start, end);
                    minutes = (long) (cycling ? cyclingTime(start, end) : walkingTime(start, end));
                }
                return new RouteResult(postcodes, trip, kilometers, minutes,
                        (int) accessibility.accessibility(start), (int) accessibility.accessibility(end));
            }
        };
        submit(task, "Finding your " + mode.toLowerCase(Locale.ROOT) + " route…", result -> {
            MapLayer layer;
            if (transit) layer = new CustomMapLayerBus(result.trip);
            else layer = new CustomMapLayer(result.postcodes);
            replaceLayer(layer);
            showRouteLegend();
            if (layer instanceof CustomMapLayerBus) {
                for (String bus : ((CustomMapLayerBus) layer).getBusRoutes()) {
                    Label badge = new Label("Bus " + bus);
                    badge.getStyleClass().add("bus-badge");
                    badge.setTextFill(CustomMapLayerBus.routeColor(bus));
                    routeLegend.getChildren().add(badge);
                }
            }
            summaryTitle.setText(mode + " route");
            routeDescription.setText(formatPostcode(start) + "  →  " + formatPostcode(end));
            distance.setText("Distance: " + formatDistance(result.kilometers) + " km");
            timeLabel.setText((transit ? "Transit" : cycling ? "Cycling" : "Walking") + " time: " + result.minutes + " min");
            startScoreLabel.setText("Score: " + result.startScore);
            endScoreLabel.setText("Score: " + result.endScore);
            estimateNote.setText(transit ? "Based on the bundled 2024 timetable, not live departures."
                    : "Distance and time are straight-line estimates; the line shows the network route.");
            heatmapLegend.setVisible(false);
            emptyState.setVisible(false);
            try {
                mapView.flyTo(0, new MapPoint((getLat(start) + getLat(end)) / 2, (getLon(start) + getLon(end)) / 2), 0.1);
                mapView.setZoom(result.kilometers > 4 ? 12 : result.kilometers > 1 ? 13 : 15);
            } catch (Exception exception) {
                LOG.log(Level.FINE, "Could not center the map on the route", exception);
            }
            setStatus("Your " + mode.toLowerCase(Locale.ROOT) + " route is ready.", false);
        });
    }

    @FXML
    private void setupHeatmapButton() {
        if (busy) return;
        Task<Map<String, Double>> task = new Task<>() {
            @Override
            protected Map<String, Double> call() throws Exception {
                if (cachedHeatmap != null) return cachedHeatmap;
                List<String> postcodes = getAllZipCodes();
                Map<String, Double> scores = new LinkedHashMap<>();
                for (int index = 0; index < postcodes.size(); index++) {
                    if (isCancelled()) return null;
                    String postcode = postcodes.get(index);
                    if (!CONNECTOR_CODES.contains(postcode)) scores.put(postcode, accessibility.accessibility(postcode));
                    updateProgress(index + 1, postcodes.size());
                }
                return scores;
            }
        };
        submit(task, "Calculating accessibility across Maastricht…", scores -> {
            try {
                replaceLayer(new CustomHeatmapLayer(scores));
            } catch (Exception exception) {
                throw new IllegalStateException("The accessibility map could not be drawn.", exception);
            }
            cachedHeatmap = scores;
            accessibilityPane.setVisible(true);
            heatmapLegend.setVisible(true);
            routeLegend.getChildren().clear();
            emptyState.setVisible(false);
            summaryTitle.setText("Accessibility map");
            routeDescription.setText("Explore nearby amenities across the city");
            distance.setText("Postcodes: " + scores.size());
            timeLabel.setText("Higher score · more nearby amenities");
            estimateNote.setText("A student-built heuristic using nearby shops, services and points of interest.");
            resetMap();
            setStatus("Accessibility map ready. Enter a postcode below to inspect its score.", false);
        });
    }

    @FXML
    private void setupCalculateButton() {
        if (busy) return;
        final String postcode;
        try {
            postcode = validate(checkZip, "Maastricht");
        } catch (IllegalArgumentException exception) {
            setStatus(exception.getMessage(), true);
            return;
        }
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                return (int) accessibility.accessibility(postcode);
            }
        };
        submit(task, "Calculating the postcode score…", score -> {
            scoreLabel.setText("Score: " + score);
            try {
                mapView.flyTo(0, new MapPoint(getLat(postcode), getLon(postcode)), 0.1);
            } catch (Exception exception) {
                LOG.log(Level.FINE, "Could not center the postcode", exception);
            }
            setStatus("Accessibility score for " + formatPostcode(postcode) + " is ready.", false);
        });
    }

    private String validate(TextField field, String name) {
        String postcode = PostcodeInput.requireValid(field.getText(), name);
        if (CONNECTOR_CODES.contains(postcode) || !mapContains(postcode)) {
            field.requestFocus();
            throw new IllegalArgumentException("The " + name + " postcode is outside the bundled Maastricht dataset.");
        }
        field.setText(formatPostcode(postcode));
        return postcode;
    }

    private <T> void submit(Task<T> task, String message, Consumer<T> onSuccess) {
        activeTask = task;
        setBusy(true);
        setStatus(message, false);
        progressIndicator.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(event -> {
            if (activeTask != task || closed) return;
            try {
                onSuccess.accept(task.getValue());
            } catch (Exception exception) {
                showFailure(exception);
            } finally {
                finishTask(task);
            }
        });
        task.setOnFailed(event -> {
            if (activeTask != task || closed) return;
            showFailure(task.getException());
            finishTask(task);
        });
        task.setOnCancelled(event -> {
            if (activeTask != task || closed) return;
            setStatus("Calculation cancelled. You can try another route.", false);
            finishTask(task);
        });
        worker.execute(task);
    }

    private void finishTask(Task<?> task) {
        if (activeTask != task) return;
        progressIndicator.progressProperty().unbind();
        activeTask = null;
        setBusy(false);
    }

    private void showFailure(Throwable failure) {
        boolean expectedInputFailure = failure instanceof IllegalArgumentException;
        if (expectedInputFailure) {
            LOG.fine(failure.getMessage());
        } else {
            LOG.log(Level.WARNING, "Route planner operation failed", failure);
        }
        String message = expectedInputFailure ? failure.getMessage()
                : "That calculation could not be completed. Try another postcode or departure time.";
        setStatus(message, true);
    }

    private void setBusy(boolean value) {
        busy = value;
        for (Node control : Arrays.asList(startZip, endZip, checkZip, walkButton, bikeButton,
                heatmapButton, calculateScoreButton, exampleButton, swapButton,
                hourSelection, minuteSelection, currentTime)) control.setDisable(value);
        transportButton.setDisable(value || !transitAvailable);
        progressIndicator.setVisible(value);
        cancelButton.setVisible(value);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.pseudoClassStateChanged(ERROR, error);
    }

    private void replaceLayer(MapLayer layer) {
        if (activeLayer != null) mapView.removeLayer(activeLayer);
        activeLayer = layer;
        mapView.addLayer(layer);
        mapView.requestLayout();
    }

    private void showRouteLegend() {
        routeLegend.getChildren().clear();
        routeLegend.getChildren().add(legendItem("Origin", Color.web("#147d78")));
        routeLegend.getChildren().add(legendItem("Destination", Color.web("#e3794a")));
    }

    private HBox legendItem(String text, Color color) {
        Label label = new Label(text);
        label.getStyleClass().add("map-caption");
        HBox item = new HBox(7, new Circle(4, color), label);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(3, 5, 3, 0));
        return item;
    }

    @FXML private void zoomIn() { mapView.setZoom(Math.min(18, mapView.getZoom() + 1)); }
    @FXML private void zoomOut() { mapView.setZoom(Math.max(10, mapView.getZoom() - 1)); }
    @FXML private void resetMap() {
        mapView.flyTo(0, new MapPoint(50.849365, 5.694548), 0.1);
        mapView.setZoom(12);
    }
    @FXML private void cancelCalculation() {
        if (activeTask != null) activeTask.cancel(true);
    }

    public void close() {
        closed = true;
        if (activeTask != null) activeTask.cancel(true);
        if (databaseTask != null) databaseTask.cancel(true);
        worker.shutdownNow();
    }

    private static String formatPostcode(String postcode) {
        return postcode.substring(0, 4) + " " + postcode.substring(4);
    }

    private static String formatDistance(double kilometers) {
        return String.format(Locale.ROOT, "%.3f", kilometers).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static final class RouteResult {
        private final List<String> postcodes;
        private final Trip trip;
        private final double kilometers;
        private final long minutes;
        private final int startScore;
        private final int endScore;
        private RouteResult(List<String> postcodes, Trip trip, double kilometers, long minutes, int startScore, int endScore) {
            this.postcodes = postcodes;
            this.trip = trip;
            this.kilometers = kilometers;
            this.minutes = minutes;
            this.startScore = startScore;
            this.endScore = endScore;
        }
    }
}
