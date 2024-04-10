package com.example.Frontend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.example.Frontend.ErrorPopup;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorPopup {

    public ErrorPopup(String message) {
        System.out.println(message);
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Error");

        Label label = new Label(message);

        VBox popupLayout = new VBox(10);
        popupLayout.getChildren().addAll(label);
        popupLayout.setAlignment(Pos.CENTER);

        Scene popupScene = new Scene(popupLayout, 200, 100);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }

    public void display() {

    }
}
