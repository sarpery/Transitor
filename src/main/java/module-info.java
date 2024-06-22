module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;
    requires java.sql;
    requires com.gluonhq.maps;
    requires geojson.jackson;
    requires com.fasterxml.jackson.databind;

    opens com.example.Frontend to javafx.fxml;
    exports com.example.Frontend;
}
