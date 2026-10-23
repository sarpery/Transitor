package com.example.Frontend;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UI extends Application {
    private static UIController controller;

    public void start(Stage primaryStage) throws IOException {
        scene = new Scene(loadFXML("Ui"), 901, 528);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnHidden(event -> controller.close());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Scene scene;

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UI.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        return root;
    }
}