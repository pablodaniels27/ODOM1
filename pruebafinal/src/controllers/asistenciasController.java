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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private int empleadoId;

    public void setEmpleadoId(int empleadoId) {
        this.empleadoId = empleadoId;
        loadUserRecords();  // Cargar los registros del usuario al pasar el ID
        checkIfEntryOrExitExists();  // Verificar si ya existe una entrada o salida para bloquear botones
    }

    @FXML
    public void initialize() {
        startDateTimeUpdater();  // Actualiza la fecha y hora en tiempo real
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
        LocalDate today = LocalDate.now();

        String sql = "SELECT es.hora_entrada, es.hora_salida FROM entradas_salidas es " +
                "JOIN dias d ON es.dia_id = d.id " +
                "WHERE es.empleado_id = ? AND d.fecha = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, empleadoId);
            statement.setDate(2, java.sql.Date.valueOf(today));

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalTime entrada = resultSet.getTime("hora_entrada").toLocalTime();
                entryTimeLabel.setText("Te registraste a las: " + entrada);

                if (resultSet.getTime("hora_salida") != null) {
                    LocalTime salida = resultSet.getTime("hora_salida").toLocalTime();
                    exitTimeLabel.setText("Saliste a las: " + salida);
                } else {
                    exitTimeLabel.setText("Aún no has registrado la salida");
                }
            } else {
                entryTimeLabel.setText("Aún no hay registros en el día");
                exitTimeLabel.setText("");
            }

        } catch (SQLException e) {
            statusLabel.setText("Error al cargar registros: " + e.getMessage());
        }
    }

    private void checkIfEntryOrExitExists() {
        LocalDate today = LocalDate.now();

        String sql = "SELECT es.hora_entrada, es.hora_salida FROM entradas_salidas es " +
                "JOIN dias d ON es.dia_id = d.id " +
                "WHERE es.empleado_id = ? AND d.fecha = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, empleadoId);
            statement.setDate(2, java.sql.Date.valueOf(today));

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getTime("hora_entrada") != null) {
                    registerEntryButton.setDisable(true);  // Deshabilitar el botón de entrada si ya hay registro
                    if (resultSet.getTime("hora_salida") != null) {
                        registerExitButton.setDisable(true);  // Deshabilitar el botón de salida si ya hay registro de salida
                    } else {
                        registerExitButton.setDisable(false);  // Habilitar el botón de salida si no hay registro de salida
                    }
                } else {
                    registerEntryButton.setDisable(false);  // Habilitar el botón de entrada si no hay registro
                    registerExitButton.setDisable(true);  // Deshabilitar el botón de salida si no hay registro de entrada
                }
            } else {
                registerEntryButton.setDisable(false);  // Habilitar el botón de entrada si no hay registros en el día
                registerExitButton.setDisable(true);  // Deshabilitar el botón de salida si no hay registros en el día
            }

        } catch (SQLException e) {
            statusLabel.setText("Error al verificar entradas/salidas: " + e.getMessage());
        }
    }

    @FXML
    private void registerEntry() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        String insertDiaSql = "INSERT INTO dias (fecha) VALUES (?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)";
        String insertEntrySql = "INSERT INTO entradas_salidas (empleado_id, dia_id, hora_entrada, tipo_asistencia_id) VALUES (?, ?, ?, 1)";  // 1: Asistencia

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement diaStatement = connection.prepareStatement(insertDiaSql, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement entryStatement = connection.prepareStatement(insertEntrySql)) {

            // Insertar o actualizar el día
            diaStatement.setDate(1, java.sql.Date.valueOf(today));
            diaStatement.executeUpdate();

            ResultSet diaResultSet = diaStatement.getGeneratedKeys();
            int diaId = diaResultSet.next() ? diaResultSet.getInt(1) : 0;

            // Insertar la entrada
            entryStatement.setInt(1, empleadoId);
            entryStatement.setInt(2, diaId);
            entryStatement.setTime(3, java.sql.Time.valueOf(now));
            entryStatement.executeUpdate();

            statusLabel.setText("Entrada registrada: " + today + " " + now);
            registerEntryButton.setDisable(true);
            registerExitButton.setDisable(false);

        } catch (SQLException e) {
            statusLabel.setText("Error al registrar entrada: " + e.getMessage());
        }
    }

    @FXML
    private void registerExit() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        String updateExitSql = "UPDATE entradas_salidas es JOIN dias d ON es.dia_id = d.id " +
                "SET es.hora_salida = ?, es.tipo_salida_id = 4 WHERE es.empleado_id = ? AND d.fecha = ?";  // 4: Salió a tiempo

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement exitStatement = connection.prepareStatement(updateExitSql)) {

            exitStatement.setTime(1, java.sql.Time.valueOf(now));
            exitStatement.setInt(2, empleadoId);
            exitStatement.setDate(3, java.sql.Date.valueOf(today));
            exitStatement.executeUpdate();

            statusLabel.setText("Salida registrada: " + today + " " + now);
            registerExitButton.setDisable(true);

        } catch (SQLException e) {
            statusLabel.setText("Error al registrar salida: " + e.getMessage());
        }
    }

    private void loadFingerprintImage() {
        Image fingerprintImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/Huella.jpg")));
        fingerprintImageView.setImage(fingerprintImage);
    }
}
