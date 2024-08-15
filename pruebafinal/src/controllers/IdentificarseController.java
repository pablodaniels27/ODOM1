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
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Objects;
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
            URL resourceUrl = getClass().getResource("/resources");
            if (resourceUrl != null) {
                File dir = new File(Objects.requireNonNull(resourceUrl).getPath());
                File[] files = dir.listFiles((d, name) -> name.endsWith(".ser"));

                if (files != null) {
                    for (File file : files) {
                        try (FileInputStream fileIn = new FileInputStream(file);
                             ObjectInputStream in = new ObjectInputStream(fileIn)) {

                            byte[] templateData = (byte[]) in.readObject();
                            DPFPTemplate loadedTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateData);
                            DPFPVerificationResult result = verifier.verify(features, loadedTemplate);

                            if (result.isVerified()) {
                                Platform.runLater(() -> {
                                    statusLabel.setText("Verificación exitosa.");
                                    startRedirectTimer();  // Iniciar el temporizador después de la verificación exitosa
                                });
                                stopCapture();
                                return;
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> statusLabel.setText("Error al cargar o verificar la huella: " + e.getMessage()));
                        }
                    }
                    Platform.runLater(() -> statusLabel.setText("Huella no reconocida."));
                } else {
                    Platform.runLater(() -> statusLabel.setText("No se encontraron huellas guardadas."));
                }
            } else {
                Platform.runLater(() -> statusLabel.setText("Directorio de recursos no encontrado."));
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
            int countdown = 5;

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
