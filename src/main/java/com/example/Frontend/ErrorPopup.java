package com.example.Frontend;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorPopup {

    public ErrorPopup(String message) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Error");

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(180);

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
