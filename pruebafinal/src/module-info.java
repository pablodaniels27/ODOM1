module pruebafinal {
    requires javafx.controls;
    requires javafx.fxml;


    requires java.desktop;

    requires dpotapi;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.sql;


    opens Lector to javafx.fxml;


    opens controllers to javafx.fxml;
    exports controllers;
    exports views;
    exports Lector;
}