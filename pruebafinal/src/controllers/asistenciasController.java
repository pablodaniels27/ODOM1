package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
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
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT hora_entrada, hora_salida FROM entradas_salidas " +
                    "JOIN dias ON entradas_salidas.dia_id = dias.id " +
                    "WHERE entradas_salidas.empleado_id = ? AND dias.fecha = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, empleadoId);
            statement.setDate(2, java.sql.Date.valueOf(today));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalTime entrada = resultSet.getTime("hora_entrada").toLocalTime();
                LocalTime salida = resultSet.getTime("hora_salida") != null ? resultSet.getTime("hora_salida").toLocalTime() : null;

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

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
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Insertar entrada en la base de datos
            String sql = "INSERT INTO entradas_salidas (empleado_id, dia_id, hora_entrada, tipo_asistencia_id) " +
                    "VALUES (?, (SELECT id FROM dias WHERE fecha = ?), ?, 1)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, empleadoId);
            statement.setDate(2, java.sql.Date.valueOf(today));
            statement.setTime(3, java.sql.Time.valueOf(now));
            statement.executeUpdate();

            statusLabel.setText("Entrada registrada: " + now.toString());
            loadUserRecords();  // Recargar los registros después de insertar
            checkButtonStatus();  // Actualizar el estado de los botones
        } catch (SQLException e) {
            statusLabel.setText("Error al registrar entrada: " + e.getMessage());
        }
    }

    @FXML
    private void registerExit() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Actualizar salida en la base de datos
            String sql = "UPDATE entradas_salidas SET hora_salida = ?, tipo_salida_id = 4 WHERE empleado_id = ? " +
                    "AND dia_id = (SELECT id FROM dias WHERE fecha = ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setTime(1, java.sql.Time.valueOf(now));
            statement.setInt(2, empleadoId);
            statement.setDate(3, java.sql.Date.valueOf(today));
            statement.executeUpdate();

            statusLabel.setText("Salida registrada: " + now.toString());
            loadUserRecords();  // Recargar los registros después de actualizar
            checkButtonStatus();  // Actualizar el estado de los botones
        } catch (SQLException e) {
            statusLabel.setText("Error al registrar salida: " + e.getMessage());
        }
    }

    private void checkButtonStatus() {
        LocalDate today = LocalDate.now();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT hora_entrada, hora_salida FROM entradas_salidas " +
                    "JOIN dias ON entradas_salidas.dia_id = dias.id " +
                    "WHERE entradas_salidas.empleado_id = ? AND dias.fecha = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, empleadoId);
            statement.setDate(2, java.sql.Date.valueOf(today));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalTime entrada = resultSet.getTime("hora_entrada").toLocalTime();
                LocalTime salida = resultSet.getTime("hora_salida") != null ? resultSet.getTime("hora_salida").toLocalTime() : null;

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
