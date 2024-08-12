package Lector;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.*;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

public class EnrollmentFormController {

    @FXML
    private Label statusLabel;

    @FXML
    private Label idLabel;  // Nueva etiqueta para mostrar el ID

    @FXML
    private Button saveTemplateButton;

    @FXML
    private Button stopButton;

    @FXML
    private ImageView fingerprintImageView;

    private DPFPCapture capturer;
    private DPFPEnrollment enrollment;
    private DPFPTemplate template;
    private String uniqueID;
    private BufferedImage bufferedImage;

    public void initialize() {
        capturer = DPFPGlobal.getCaptureFactory().createCapture();
        enrollment = DPFPGlobal.getEnrollmentFactory().createEnrollment();
        initCaptureEvents();
        saveTemplateButton.setDisable(true);
        stopButton.setDisable(true);

        // Comienza la captura automáticamente al inicializar la vista.
        startCapture();
    }

    private void initCaptureEvents() {
        capturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(DPFPDataEvent e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Fingerprint data acquired.");
                    showFingerprintImage(e.getSample());
                });
                process(e.getSample());
            }
        });

        capturer.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            @Override
            public void readerConnected(DPFPReaderStatusEvent e) {
                Platform.runLater(() -> {
                    statusLabel.setText("The fingerprint reader was connected.");
                });
            }

            @Override
            public void readerDisconnected(DPFPReaderStatusEvent e) {
                Platform.runLater(() -> {
                    statusLabel.setText("The fingerprint reader was disconnected.");
                });
            }
        });

        capturer.addErrorListener(new DPFPErrorAdapter() {

            public void errorOccurred(DPFPErrorEvent e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getError());
                });
            }
        });
    }

    public void startCapture() {
        capturer.startCapture();
        Platform.runLater(() -> {
            statusLabel.setText("Using the fingerprint reader, scan your fingerprint.");
            stopButton.setDisable(false); // Habilita el botón de detener captura
        });
    }

    @FXML
    private void stopCapture() {
        capturer.stopCapture();
        Platform.runLater(() -> {
            statusLabel.setText("Capture stopped.");
            stopButton.setDisable(true);
        });
    }

    private void process(DPFPSample sample) {
        DPFPFeatureSet featureSet = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

        if (featureSet != null) {
            try {
                enrollment.addFeatures(featureSet);

                Platform.runLater(() -> {
                    switch (enrollment.getTemplateStatus()) {
                        case TEMPLATE_STATUS_READY:
                            template = enrollment.getTemplate();
                            saveTemplateButton.setDisable(false);

                            // Generar un ID único para la huella
                            uniqueID = UUID.randomUUID().toString();

                            // Mostrar el ID en la misma ventanita
                            idLabel.setText("ID: " + uniqueID);

                            statusLabel.setText("Template is ready. ID: " + uniqueID + ". Click 'Save Template' to save it.");
                            stopCapture();
                            break;

                        case TEMPLATE_STATUS_FAILED:
                            enrollment.clear();
                            stopCapture();
                            startCapture();
                            statusLabel.setText("Template creation failed. Please scan again.");
                            break;

                        default:
                            statusLabel.setText("Template creation in progress...");
                            break;
                    }
                });

            } catch (DPFPImageQualityException ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + ex.getMessage());
                    enrollment.clear();
                    stopCapture();
                    startCapture();
                });
            }
        } else {
            Platform.runLater(() -> {
                statusLabel.setText("No features extracted from fingerprint sample.");
            });
        }
    }

    private DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            Platform.runLater(() -> {
                statusLabel.setText("Error extracting features: " + e.getMessage());
            });
            return null;
        }
    }

    @FXML
    private void saveTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Fingerprint Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fingerprint Files", "*.ser"));
        Stage stage = (Stage) saveTemplateButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fileOut = new FileOutputStream(file);
                 ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

                // Serializar el template y el ID único
                out.writeObject(template.serialize());
                out.writeObject(uniqueID);

                statusLabel.setText("Fingerprint data saved successfully at " + file.getAbsolutePath());

                // Guardar la imagen de la huella como JPG
                saveFingerprintImageAsJPG(file.getParent(), uniqueID);

            } catch (IOException e) {
                statusLabel.setText("Error saving fingerprint data: " + e.getMessage());
            }
        }
    }

    private void showFingerprintImage(DPFPSample sample) {
        bufferedImage = (BufferedImage) DPFPGlobal.getSampleConversionFactory().createImage(sample);
        Image fingerprintImage = SwingFXUtils.toFXImage(bufferedImage, null);
        fingerprintImageView.setImage(fingerprintImage);
    }

    private void saveFingerprintImageAsJPG(String directory, String uniqueID) {
        File imageFile = new File(directory, uniqueID + ".jpg");
        try {
            ImageIO.write(bufferedImage, "jpg", imageFile);
            Platform.runLater(() -> {
                statusLabel.setText(statusLabel.getText() + " and image saved as " + imageFile.getName());
            });
        } catch (IOException e) {
            Platform.runLater(() -> {
                statusLabel.setText("Error saving fingerprint image: " + e.getMessage());
            });
        }
    }

    @FXML
    private void handleSaveTemplateButtonAction() {
        saveTemplate();
    }

    @FXML
    private void closeWindow() {
        stopCapture();
        Stage stage = (Stage) stopButton.getScene().getWindow();
        stage.close();
    }
}
