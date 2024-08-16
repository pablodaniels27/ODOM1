package controllers;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.*;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.image.BufferedImage;

public class IdentificarseController {

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    private Label statusLabel;

    private DPFPCapture capturer;
    private BufferedImage bufferedImage;
    private DPFPVerification verifier;

    public void initialize() {
        capturer = DPFPGlobal.getCaptureFactory().createCapture();
        verifier = DPFPGlobal.getVerificationFactory().createVerification(DPFPVerification.MEDIUM_SECURITY_FAR);
        initCaptureEvents();
        startCapture();
        statusLabel.setText("Identifíquese con su huella por favor");

        // Usar Platform.runLater para asegurarse de que la escena esté cargada
        Platform.runLater(() -> {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent event) -> {
                stopCapture();
            });
        });
    }

    private void initCaptureEvents() {
        capturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(DPFPDataEvent e) {
                showFingerprintImage(e.getSample());
                verifyFingerprint(e.getSample());
            }
        });
    }

    public void startCapture() {
        if (capturer != null) {
            capturer.startCapture();
        }
    }

    @FXML
    private void stopCapture() {
        if (capturer != null) {
            capturer.stopCapture();
        }
    }

    @FXML
    private void closeWindow() {
        stopCapture();
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private void showFingerprintImage(DPFPSample sample) {
        // Muestra la imagen de la huella digital en la interfaz
    }

    private void verifyFingerprint(DPFPSample sample) {
        // Verifica la huella digital
    }
}
