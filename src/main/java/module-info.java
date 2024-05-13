module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;
    requires com.gluonhq.maps;

    opens com.example.Frontend to javafx.fxml;
    exports com.example.Frontend;
}
