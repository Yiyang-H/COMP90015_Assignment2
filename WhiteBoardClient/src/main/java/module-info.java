module com.example.whiteboardclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires json.simple;

    opens com.example.whiteboardclient to javafx.fxml;
    exports com.example.whiteboardclient;
    exports Draw;
    opens Draw to javafx.fxml;
}