module com.example.chatclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens application to javafx.fxml;
    exports application;
}