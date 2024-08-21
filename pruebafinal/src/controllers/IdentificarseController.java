package controllers;

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
import java.util.Timer;
import java.util.TimerTask;

public class IdentificarseController {

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timerLabel;  // Nuevo Label para el temporizador

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
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "SELECT huella FROM empleados";
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();

                boolean found = false;
                while (resultSet.next()) {
                    byte[] templateBytes = resultSet.getBytes("huella");

                    if (templateBytes != null) {
                        // Deserializa el template desde los bytes recuperados de la base de datos
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(templateBytes);
                             ObjectInputStream ois = new ObjectInputStream(bais)) {

                            byte[] serializedTemplate = (byte[]) ois.readObject();
                            DPFPTemplate template = DPFPGlobal.getTemplateFactory().createTemplate(serializedTemplate);

                            DPFPVerificationResult result = verifier.verify(features, template);

                            if (result.isVerified()) {
                                Platform.runLater(() -> {
                                    statusLabel.setText("Verificación exitosa.");
                                    startRedirectTimer();  // Iniciar el temporizador después de la verificación exitosa
                                });
                                stopCapture();
                                found = true;
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

    private void startRedirectTimer() {
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
                        redirectToAsistencias();  // Redirigir después de que el temporizador llegue a 0
                    }
                });
            }
        }, 0, 1000);
    }

    private void redirectToAsistencias() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/asistencias.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
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
        // Código para cerrar la ventana si es necesario
    }
}
