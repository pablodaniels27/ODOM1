module pruebafinal {
    requires javafx.controls;
    requires javafx.fxml;


    requires java.desktop;


    requires javafx.graphics;
    requires javafx.swing;
    requires java.sql;
    requires com.github.benmanes.caffeine;
    requires dpotapi;


    opens Lector to javafx.fxml;


    opens controllers to javafx.fxml;
    exports controllers;
    exports views;
    exports Lector;
}
