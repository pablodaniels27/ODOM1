package controllers;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.*;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.*;
import com.digitalpersona.onetouch.verification.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class asistenciasController {

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button registerEntryButton;

    @FXML
    private Button registerExitButton;

    private DPFPCapture capturer;
    private DPFPVerification verifier;
    private DPFPFeatureSet features;
    private String currentEmployeeId;

    public void initialize() {
        capturer = DPFPGlobal.getCaptureFactory().createCapture();
        verifier = DPFPGlobal.getVerificationFactory().createVerification(DPFPVerification.MEDIUM_SECURITY_FAR);
        initCaptureEvents();
        startCapture();
        statusLabel.setText("Identifíquese con su huella por favor");

        registerEntryButton.setOnAction(event -> registerAttendance("entrada"));
        registerExitButton.setOnAction(event -> registerAttendance("salida"));
    }

    private void initCaptureEvents() {
        capturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(DPFPDataEvent e) {
                Platform.runLater(() -> showFingerprintImage(e.getSample()));
                verifyFingerprint(e.getSample());
            }
        });
    }

    public void startCapture() {
        capturer.startCapture();
    }

    private void showFingerprintImage(DPFPSample sample) {
        BufferedImage bufferedImage = (BufferedImage) DPFPGlobal.getSampleConversionFactory().createImage(sample);
        javafx.scene.image.Image fingerprintImage = javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null);
        fingerprintImageView.setImage(fingerprintImage);
    }

    private void verifyFingerprint(DPFPSample sample) {
        features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (features != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "SELECT id, huella FROM empleados";
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();

                boolean found = false;
                while (resultSet.next()) {
                    byte[] templateBytes = resultSet.getBytes("huella");

                    if (templateBytes != null) {
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(templateBytes);
                             ObjectInputStream ois = new ObjectInputStream(bais)) {

                            byte[] serializedTemplate = (byte[]) ois.readObject();
                            DPFPTemplate template = DPFPGlobal.getTemplateFactory().createTemplate(serializedTemplate);

                            DPFPVerificationResult result = verifier.verify(features, template);

                            if (result.isVerified()) {
                                currentEmployeeId = resultSet.getString("id");
                                Platform.runLater(() -> statusLabel.setText("Verificación exitosa."));
                                found = true;
                                stopCapture();
                                break;
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> statusLabel.setText("Error al deserializar la huella: " + e.getMessage()));
                        }
                    }
                }

                if (!found) {
                    Platform.runLater(() -> statusLabel.setText("Huella no reconocida."));
                }
            } catch (SQLException e) {
                Platform.runLater(() -> statusLabel.setText("Error de base de datos: " + e.getMessage()));
            }
        } else {
            Platform.runLater(() -> statusLabel.setText("No se pudieron extraer características de la huella."));
        }
    }

    private DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            Platform.runLater(() -> statusLabel.setText("Error al extraer características: " + e.getMessage()));
            return null;
        }
    }

    private void registerAttendance(String tipo) {
        if (currentEmployeeId != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                // Obtener el dia_id de la fecha actual
                String dayQuery = "SELECT id FROM dias WHERE fecha = ?";
                PreparedStatement dayStatement = connection.prepareStatement(dayQuery);
                dayStatement.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                ResultSet dayResult = dayStatement.executeQuery();

                int diaId;
                if (dayResult.next()) {
                    diaId = dayResult.getInt("id");
                } else {
                    // Si no existe el dia_id para la fecha actual, lo creamos
                    String insertDayQuery = "INSERT INTO dias (fecha) VALUES (?)";
                    PreparedStatement insertDayStatement = connection.prepareStatement(insertDayQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                    insertDayStatement.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                    insertDayStatement.executeUpdate();
                    ResultSet generatedKeys = insertDayStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        diaId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Error al insertar el día actual.");
                    }
                }

                if (tipo.equals("entrada")) {
                    // Insertar registro de entrada
                    String sql = "INSERT INTO entradas_salidas (empleado_id, dia_id, hora_entrada) VALUES (?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, currentEmployeeId);
                    statement.setInt(2, diaId);
                    statement.setTime(3, java.sql.Time.valueOf(LocalTime.now()));
                    statement.executeUpdate();
                    Platform.runLater(() -> statusLabel.setText("Registro de entrada exitoso."));
                } else if (tipo.equals("salida")) {
                    // Actualizar registro de salida
                    String sql = "UPDATE entradas_salidas SET hora_salida = ? WHERE empleado_id = ? AND dia_id = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setTime(1, java.sql.Time.valueOf(LocalTime.now()));
                    statement.setString(2, currentEmployeeId);
                    statement.setInt(3, diaId);
                    statement.executeUpdate();
                    Platform.runLater(() -> statusLabel.setText("Registro de salida exitoso."));
                }
            } catch (SQLException e) {
                Platform.runLater(() -> statusLabel.setText("Error al registrar asistencia: " + e.getMessage()));
            }
        } else {
            Platform.runLater(() -> statusLabel.setText("Primero debes verificar tu huella."));
        }
    }

    @FXML
    private void stopCapture() {
        capturer.stopCapture();
    }
}
