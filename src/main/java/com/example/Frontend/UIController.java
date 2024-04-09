package com.example.Frontend;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.Controller.AlgoConnecter;
import com.example.Controller.HashController;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;


public class UIController implements Initializable, AlgoConnecter, HashController {

    private double initX, initY;
    private double aAX = 0;
    private double aAY = 0;

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
    private Label bikeTime;

    @FXML
    private Label walkTime;

    @FXML
    private Label distance;

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

    public String startText;
    public String endText;


    public void walkBikeCalcs(String startText, String endText){
            System.out.println(startText);
            System.out.println(endText);

            double distance1 = getDistance(startText, endText);
            double roundedNumber = Math.round(distance1 * 1000.0) / 1000.0;
            distance.setText("Distance: " + String.valueOf(roundedNumber) + " km");
            System.out.println(getDistance(startText, endText));

            int walkingTime = (int) walkingTime(startText, endText);
            int bikingTime = (int) cyclingTime(startText, endText);

            bikeTime.setText("Biking time: " + String.valueOf(bikingTime) + " min");
            walkTime.setText("Walking time: " + String.valueOf(walkingTime) + " min");


            System.out.println("Walking time: " + walkingTime);
            System.out.println("Biking time: " + bikingTime);

    }

    public void setupToolTip(){
        locationToolTip.setTooltip(
                new Tooltip(
                        "Popular locations:\n" +
                                "- DACS: 6229EN\n" +
                                "- Vrijthof: 6211LE\n\n" + "GROUP 10\n"));
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

    public void drawPointStart(double a, double b){
        circle1.setLayoutX(zipToPosX(a));
        circle1.setLayoutY(zipToPosY(b));
        circle1.setOpacity(1);
    }

    public void drawPointEnd(double a, double b){
        circle2.setLayoutX(zipToPosX(a));
        circle2.setLayoutY(zipToPosY(b));
        circle2.setOpacity(1);
    }

    public void drawLine(){
        distanceLine.setLayoutX(0);
        distanceLine.setLayoutY(0);
        distanceLine.setStartX(circle1.getLayoutX());
        distanceLine.setStartY(circle1.getLayoutY());
        distanceLine.setEndX(circle2.getLayoutX());
        distanceLine.setEndY(circle2.getLayoutY());

        distanceLine.setOpacity(1);
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

    @FXML
    private void pathDrawBut(String startText, String endText) {
        walkBikeCalcs(startText, endText);

        double ax = getLat(startZip.getText());

        double ay = getLon(startZip.getText());
        double bx = getLat(endZip.getText());
        double by = getLon(endZip.getText());
        System.out.println(ax);
        System.out.println(ay);

           drawPointStart(ay,ax);
           drawPointEnd(by,bx);
           drawLine();
    }

    public void startPath(){
        startText = startZip.getText();
        endText = endZip.getText();


        pathDrawBut(startText,endText);
        walkBikeCalcs(startText, endText);

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupToolTip();
        hashTheData();
    }

}