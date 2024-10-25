package controllers;

import DAO.BaseDAO;
import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.*;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.*;
import com.digitalpersona.onetouch.verification.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class IdentificarseController {

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private Label welcomeLabel;

    private DPFPCapture capturer;
    private BufferedImage bufferedImage;
    private DPFPVerification verifier;

    public void initialize() {
        capturer = DPFPGlobal.getCaptureFactory().createCapture();
        verifier = DPFPGlobal.getVerificationFactory().createVerification(DPFPVerification.MEDIUM_SECURITY_FAR);
        initCaptureEvents();
        startCapture();
        statusLabel.setText("Identifíquese con su huella por favor");
    }

    private void initCaptureEvents() {
        capturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(DPFPDataEvent e) {
                Platform.runLater(() -> {
                    showFingerprintImage(e.getSample());
                });
                verifyFingerprint(e.getSample());
            }
        });
    }

    public void startCapture() {
        capturer.startCapture();
    }

    private void showFingerprintImage(DPFPSample sample) {
        bufferedImage = (BufferedImage) DPFPGlobal.getSampleConversionFactory().createImage(sample);
        Image fingerprintImage = SwingFXUtils.toFXImage(bufferedImage, null);
        fingerprintImageView.setImage(fingerprintImage);
    }

    private void verifyFingerprint(DPFPSample sample) {
        DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (features != null) {
            try {
                List<Map<String, Object>> huellas = BaseDAO.obtenerHuellasEmpleadosActivos();

                boolean found = false;
                int empleadoId = -1;

                for (Map<String, Object> huellaData : huellas) {
                    byte[] templateBytes = (byte[]) huellaData.get("huella");

                    if (templateBytes != null) {
                        // Ya no es necesario usar ObjectInputStream, directamente crea el template
                        DPFPTemplate template = DPFPGlobal.getTemplateFactory().createTemplate(templateBytes);

                        DPFPVerificationResult result = verifier.verify(features, template);

                        if (result.isVerified()) {
                            String nombreEmpleado = (String) huellaData.get("nombres");
                            String apellidoPaterno = (String) huellaData.get("apellido_paterno");
                            empleadoId = (int) huellaData.get("id");

                            int finalEmpleadoId = empleadoId;
                            Platform.runLater(() -> {
                                welcomeLabel.setText("Bienvenido, " + nombreEmpleado + " " + apellidoPaterno);
                                welcomeLabel.setVisible(true);
                                statusLabel.setText("Verificación exitosa.");
                                startRedirectTimer(finalEmpleadoId);
                            });
                            stopCapture();
                            found = true;
                            break;
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

    private void startRedirectTimer(int empleadoId) {
        timerLabel.setVisible(true);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int countdown = 3;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (countdown > 0) {
                        timerLabel.setText("Redireccionándote en " + countdown);
                        countdown--;
                    } else {
                        timer.cancel();
                        redirectToAsistencias(empleadoId);
                    }
                });
            }
        }, 0, 1000);
    }

    private void redirectToAsistencias(int empleadoId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/asistencias.fxml"));
            Parent root = loader.load();

            asistenciasController asistenciasController = loader.getController();
            asistenciasController.setEmpleadoId(empleadoId);

            Scene scene = new Scene(root);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);

            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/ODOM.jpg"))));

            stage.setTitle("Asistencias");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void stopCapture() {
        capturer.stopCapture();
    }

    public void closeWindow() {
        stopCapture();
    }
}
