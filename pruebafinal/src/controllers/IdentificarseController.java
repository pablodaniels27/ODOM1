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
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class IdentificarseController {

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timerLabel;

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
                System.out.println("Huella capturada.");
                Platform.runLater(() -> {
                    showFingerprintImage(e.getSample());
                });
                verifyFingerprint(e.getSample());
            }
        });
    }

    public void startCapture() {
        capturer.startCapture();
        System.out.println("Captura iniciada...");
    }

    private void showFingerprintImage(DPFPSample sample) {
        bufferedImage = (BufferedImage) DPFPGlobal.getSampleConversionFactory().createImage(sample);
        Image fingerprintImage = SwingFXUtils.toFXImage(bufferedImage, null);
        fingerprintImageView.setImage(fingerprintImage);
        System.out.println("Imagen de huella mostrada en la interfaz.");
    }

    private void verifyFingerprint(DPFPSample sample) {
        DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (features != null) {
            System.out.println("Características de la huella extraídas correctamente.");
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/empresaodom", "root", "")) {
                System.out.println("Conexión a la base de datos establecida.");
                String query = "SELECT huella FROM empleados";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet rs = statement.executeQuery();

                boolean matchFound = false;

                while (rs.next()) {
                    byte[] blobData = rs.getBytes("huella");

                    if (blobData != null) {
                        try {
                            // Crear el template a partir del blob
                            DPFPTemplate template = DPFPGlobal.getTemplateFactory().createTemplate();
                            template.deserialize(blobData);

                            // Agregar mensajes de depuración para comparar directamente los datos
                            System.out.println("Comparando huella capturada con huella almacenada...");
                            DPFPVerificationResult result = verifier.verify(features, template);
                            System.out.println("Resultado de la verificación: " + result.isVerified());

                            if (result.isVerified()) {
                                Platform.runLater(() -> {
                                    statusLabel.setText("Verificación exitosa.");
                                    System.out.println("Huella verificada con éxito.");
                                    startRedirectTimer();
                                });
                                stopCapture();
                                matchFound = true;
                                break;
                            } else {
                                System.out.println("No hay coincidencia con esta huella.");
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> statusLabel.setText("Error al deserializar la huella: " + e.getMessage()));
                            System.out.println("Error al deserializar la huella: " + e.getMessage());
                        }
                    }
                }

                if (!matchFound) {
                    Platform.runLater(() -> statusLabel.setText("Huella no reconocida."));
                    System.out.println("No se encontró ninguna coincidencia para la huella capturada.");
                }

            } catch (SQLException e) {
                Platform.runLater(() -> statusLabel.setText("Error en la conexión a la base de datos: " + e.getMessage()));
                System.out.println("Error en la conexión a la base de datos: " + e.getMessage());
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error inesperado: " + e.getMessage()));
                System.out.println("Error inesperado: " + e.getMessage());
            }
        } else {
            Platform.runLater(() -> statusLabel.setText("No se pudieron extraer características de la huella."));
            System.out.println("Error: No se pudieron extraer características de la huella.");
        }
    }

    private DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            DPFPFeatureSet features = extractor.createFeatureSet(sample, purpose);
            System.out.println("Características extraídas: " + (features != null));
            return features;
        } catch (DPFPImageQualityException e) {
            Platform.runLater(() -> statusLabel.setText("Error al extraer características: " + e.getMessage()));
            System.out.println("Error al extraer características de la huella: " + e.getMessage());
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
                        System.out.println("Redireccionando en " + countdown + " segundos.");
                        countdown--;
                    } else {
                        timer.cancel();
                        redirectToAsistencias();
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
            System.out.println("Redireccionado a la vista de asistencias.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al redirigir a la vista de asistencias: " + e.getMessage());
        }
    }

    @FXML
    private void stopCapture() {
        capturer.stopCapture();
        System.out.println("Captura detenida.");
    }

    public void closeWindow() {
        stopCapture();
        System.out.println("Ventana cerrada y captura detenida.");
    }
}
