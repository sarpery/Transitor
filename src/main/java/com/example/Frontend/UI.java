package com.example.Frontend;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Desktop entry point for the Transitor route planner. */
public class UI extends Application {
    private UIController controller;

    @Override
    public void start(Stage stage) throws IOException {
        URL layout = UI.class.getResource("Ui.fxml");
        if (layout == null) {
            throw new IOException("The Transitor interface resource could not be found.");
        }
        FXMLLoader loader = new FXMLLoader(layout);
        Scene scene = new Scene(loader.load(), 1180, 800);
        scene.getStylesheets().add(UI.class.getResource("transitor.css").toExternalForm());
        controller = loader.getController();
        stage.setTitle("Transitor · Maastricht route planner");
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.setOnHidden(event -> controller.close());
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
