package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class asistenciasController {

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label entryTimeLabel;

    @FXML
    private Label exitTimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button registerEntryButton;

    @FXML
    private Button registerExitButton;

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    public void initialize() {
        startDateTimeUpdater();  // Actualiza la fecha y hora en tiempo real
        loadUserRecords();       // Cargar los registros del usuario
        loadFingerprintImage();  // Cargar la imagen de huella
    }

    private void startDateTimeUpdater() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTimeLabel.setText(currentTime.format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void loadUserRecords() {
        // Aquí va el código para cargar los registros del usuario desde la base de datos
        entryTimeLabel.setText("Te registraste a las: 9:00 AM");
        exitTimeLabel.setText("Saliste a las: 6:00 PM");
    }

    private void loadFingerprintImage() {
        Image fingerprintImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/Huella.jpg")));
        fingerprintImageView.setImage(fingerprintImage);
    }
}
