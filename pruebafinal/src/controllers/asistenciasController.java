package controllers;

import DAO.BaseDAO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private Label actionMessageLabel;  // Nuevo label para mostrar mensajes adicionales

    @FXML
    private Button registerEntryButton;

    @FXML
    private Button registerExitButton;

    @FXML
    private ImageView fingerprintImageView;

    private int empleadoId;

    public void setEmpleadoId(int id) {
        this.empleadoId = id;
        loadUserRecords();
        checkButtonStatus();
    }

    @FXML
    public void initialize() {
        startDateTimeUpdater();  // Actualiza la fecha y hora en tiempo real
        loadFingerprintImage();  // Cargar la imagen de huella
    }

    private void startDateTimeUpdater() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime currentTime = LocalDateTime.now();

            // Formatear la fecha y hora de manera amigable
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

            String formattedDate = currentTime.format(dateFormatter);
            String formattedTime = currentTime.format(timeFormatter);

            dateTimeLabel.setText(formattedDate + " - " + formattedTime);
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void loadUserRecords() {
        LocalDate today = LocalDate.now();
        try {
            Optional<Map<String, LocalTime>> records = BaseDAO.obtenerRegistrosDelDia(empleadoId, today);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

            if (records.isPresent()) {
                Map<String, LocalTime> recordData = records.get();
                LocalTime entrada = recordData.get("hora_entrada");
                LocalTime salida = recordData.get("hora_salida");

                entryTimeLabel.setText("Te registraste a las: " + entrada.format(timeFormatter));

                if (salida != null) {
                    exitTimeLabel.setText("Saliste a las: " + salida.format(timeFormatter));
                } else {
                    exitTimeLabel.setText("Aún no has registrado tu salida.");
                }
            } else {
                entryTimeLabel.setText("Aún no hay registros en el día");
                exitTimeLabel.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadFingerprintImage() {
        Image fingerprintImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/Huella.jpg")));
        fingerprintImageView.setImage(fingerprintImage);
    }

    @FXML
    private void registerEntry() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        try {
            // Insertar entrada en la base de datos usando el DAO
            BaseDAO.insertarEntradaAsistencia(empleadoId, today, now);

            // Formato 24 horas para la hora registrada
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            statusLabel.setText("Entrada registrada: " + now.format(timeFormatter));

            // Recargar los registros y actualizar el estado de los botones
            loadUserRecords();
            checkButtonStatus();
        } catch (SQLException e) {
            statusLabel.setText("Error al registrar entrada: " + e.getMessage());
        }
    }

    @FXML
    private void registerExit() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        try {
            // Actualizar la salida en la base de datos usando el DAO
            BaseDAO.actualizarSalidaAsistencia(empleadoId, today, now);

            // Formato 24 horas para la hora registrada
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            statusLabel.setText("Salida registrada: " + now.format(timeFormatter));

            // Recargar los registros y actualizar el estado de los botones
            loadUserRecords();
            checkButtonStatus();
        } catch (SQLException e) {
            statusLabel.setText("Error al registrar salida: " + e.getMessage());
        }
    }

    private void checkButtonStatus() {
        LocalDate today = LocalDate.now();
        try {
            // Llamar al DAO para verificar el estado de la entrada y salida del día actual
            Optional<Map<String, LocalTime>> horarios = BaseDAO.obtenerHorariosDeAsistencia(empleadoId, today);

            if (horarios.isPresent()) {
                LocalTime entrada = horarios.get().get("entrada");
                LocalTime salida = horarios.get().get("salida");

                registerEntryButton.setDisable(entrada != null);  // Desactivar botón si ya se registró entrada
                registerExitButton.setDisable(salida != null || entrada == null);  // Desactivar botón si ya se registró salida o no hay entrada
            } else {
                registerEntryButton.setDisable(false);
                registerExitButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}