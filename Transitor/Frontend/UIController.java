package Transitor.Frontend;

import Transitor.Backend.Algorithms;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import java.net.URL;
import java.util.ResourceBundle;


public class UIController implements Initializable {

    Robot robot = new Robot();
    private double initX, initY;
    private double aAX = 0;
    private double aAY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    private double zipX = 5.710646925;
    private double zipY = 50.83646594;

    private double zipXX = 5.721714464;
    private double zipYY = 50.82346493;


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
    private Label time;

    @FXML
    private Label distance;

    @FXML
    private ToggleButton walktog;

    @FXML
    private ToggleButton biketog;

    @FXML
    private Button findPath;

    @FXML
    private Slider slider;

    @FXML
    private TextField startZip;

    @FXML
    private TextField endZip;

    @FXML
    public Pane mapPane;

    private String startText;
    private String endText;

    ToggleGroup group2;

    public void findPathSetup(){

        group2 = new ToggleGroup();
        biketog.setToggleGroup(group2);
        walktog.setToggleGroup(group2);

        EventHandler<ActionEvent> eventHandler1 = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (walktog.isSelected()) {
                    walktog.setStyle("-fx-background-color: #E84E10");
                    biketog.setStyle("-fx-background-color: #E88A10");
                } else if (biketog.isSelected()){
                    biketog.setStyle("-fx-background-color: #E84E10");
                    walktog.setStyle("-fx-background-color: #E88A10");
                }
                else{
                    biketog.setStyle("-fx-background-color: #E84E10");
                    walktog.setStyle("-fx-background-color: #E84E10");
                }
            }
        };

        biketog.setOnAction(eventHandler1);
        walktog.setOnAction(eventHandler1);

        EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                startText = startZip.getText();
                endText = endZip.getText();
                System.out.println(startText);
                System.out.println(endText);
                Algorithms algorithms = new Algorithms();

                double distance1 = algorithms.getDistance(startText, endText);
                double roundedNumber = Math.round(distance1 * 1000.0) / 1000.0;
                distance.setText("Distance: " + String.valueOf(roundedNumber) + " km");
                System.out.println(algorithms.getDistance(startText, endText));

                int walkingTime = (int) algorithms.getWalkingTime(startText, endText);
                int bikingTime = (int) algorithms.getCyclingTime(startText, endText);

                if(walktog.isSelected()) {
                    time.setText("Time: " + String.valueOf(walkingTime) + " min");
                }
                else if(biketog.isSelected()){
                    time.setText("Time: " + String.valueOf(bikingTime) + " min");
                }

                System.out.println("Walking time: " + walkingTime);
                System.out.println("Biking time: " + bikingTime);

            }
        };

        findPath.setOnAction(eventHandler);
    }

    @FXML
    private void zoomIn() { // delete later
        setSceneEvents(mapPane.getScene());
    }



    private double zipToPosX(double x){
        double res = (x - 5.635000228881836)*3400;
        double resultX = res + 93.30105590820312;
        return resultX;
    }

    private double zipToPosY(double y){
        double res = (50.91600036621094 - y)*4000;
        double resultY = res + 10.855741500854492;
        return resultY;
    }

    private void setSceneEvents(final Scene scene) {

        scene.setOnScroll(event -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 2.0 - zoomFactor;
            }
            System.out.println(zoomFactor);
            mapPane.setScaleX(mapPane.getScaleX() * zoomFactor);
            mapPane.setScaleY(mapPane.getScaleY() * zoomFactor);
            event.consume();
        });
        scene.setOnMousePressed(event -> {
            aAX = event.getSceneX();
            aAY = event.getSceneY();
            initX = mapPane.getLayoutX();
            initY = mapPane.getLayoutY();
        });

        scene.setOnMouseDragged(event -> {
            mapPane.setTranslateX((event.getSceneX() - aAX));
            mapPane.setTranslateY(event.getSceneY() - aAY);
        });

        scene.setOnMouseReleased(event -> {
            mapPane.setLayoutX(initX + mapPane.getTranslateX());
            mapPane.setLayoutY(initY + mapPane.getTranslateY());

            mapPane.setTranslateX(0);
            mapPane.setTranslateY(0);
            System.out.println(map.getBoundsInLocal());
            System.out.println(map.getBoundsInParent());

        });

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initX = mapPane.getLayoutX();
        initY = mapPane.getLayoutY();

        System.out.println(map.getBoundsInLocal());
        System.out.println(map.getBoundsInParent());
        System.out.println(circle1.getLayoutX());
        System.out.println(circle1.getLayoutY());
        circle1.setLayoutX(zipToPosX(zipX));
        circle1.setLayoutY(zipToPosY(zipY));

        circle2.setLayoutX(zipToPosX(zipXX));
        circle2.setLayoutY(zipToPosY(zipYY));

        distanceLine.setLayoutX(0);
        distanceLine.setLayoutY(0);

        distanceLine.setStartX(circle1.getLayoutX());
        distanceLine.setStartY(circle1.getLayoutY());
        distanceLine.setEndX(circle2.getLayoutX());
        distanceLine.setEndY(circle2.getLayoutY());

        distanceLine.setOpacity(1);

        findPathSetup();
    }
}