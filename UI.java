import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;

public class UI extends Application {
    public static Scene mainScene;
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ui.fxml"));

        Pane mainPane = loader.load();
//
        mainScene = new Scene(mainPane, 900, 500);

        primaryStage.setTitle("Transitor");
        primaryStage.setScene(this.mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}