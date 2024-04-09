module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;

    opens com.example.Frontend to javafx.fxml;
    exports com.example.Frontend;
}
