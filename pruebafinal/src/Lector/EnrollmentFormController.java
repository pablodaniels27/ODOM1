package Lector;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.*;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.*;
import controllers.EdicionController;
import controllers.RegistroController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import javafx.scene.image.Image;
import javafx.stage.Window;

import javax.imageio.ImageIO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class EnrollmentFormController {

    @FXML
    private Label statusLabel;

    @FXML
    private Label idLabel;

    @FXML
    private Button continuarButton;

    @FXML
    private Button stopButton;

    @FXML
    private ImageView fingerprintImageView;

    private DPFPCapture capturer;
    private DPFPEnrollment enrollment;
    private DPFPTemplate template;
    private BufferedImage bufferedImage;
    private EdicionController edicionController;
    private boolean bandera = false;



    private RegistroController registroController;

    public void initialize() {
        capturer = DPFPGlobal.getCaptureFactory().createCapture();
        enrollment = DPFPGlobal.getEnrollmentFactory().createEnrollment();
        initCaptureEvents();
        continuarButton.setDisable(true);
        stopButton.setDisable(true);




        startCapture();

        // Agregar manejador de cierre
        Platform.runLater(() -> {
            Stage stage = (Stage) stopButton.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                // Llama al método closeWindow() cuando la ventana se cierre
                closeWindow();
            });
        });


    }

    public void setEdicionController(EdicionController controller) {
        this.edicionController = controller;
    }

    public void setRegistroController(RegistroController registroController) {
        this.registroController = registroController;
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
                    statusLabel.setText("Lector conectado");
                });
            }

            @Override
            public void readerDisconnected(DPFPReaderStatusEvent e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Lector no conectado");
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
            statusLabel.setText("Capturando huella");
            stopButton.setDisable(false);
        });
    }

    @FXML
    private void stopCapture() {
        capturer.stopCapture();
        Platform.runLater(() -> {
            statusLabel.setText("Captura finalizada.");
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
                            continuarButton.setDisable(false);

                            // Enviar el template y la imagen de la huella al RegistroController
                            if (registroController != null || edicionController != null) {
                                if (registroController != null){
                                    registroController.setTemplate(template);
                                } else if (edicionController != null) {
                                    edicionController.setTemplate(template);
                                    bandera = true;
                                }

                                if (bufferedImage != null && bandera ==false) {
                                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                        ImageIO.write(bufferedImage, "png", baos);
                                        byte[] imageBytes = baos.toByteArray();
                                        registroController.setFingerprintImageBytes(imageBytes);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else if (bufferedImage != null && bandera) {
                                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                        ImageIO.write(bufferedImage, "png", baos);
                                        byte[] imageBytes = baos.toByteArray();
                                        edicionController.setFingerprintImageBytes(imageBytes);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            statusLabel.setText("Template is ready. Click 'Save Template' to save it.");
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
    private void handleSaveTemplateButtonAction(ActionEvent event) {
        Stage stage = (Stage) continuarButton.getScene().getWindow();
        stage.close();
    }

    private void showFingerprintImage(DPFPSample sample) {
        java.awt.Image awtImage = DPFPGlobal.getSampleConversionFactory().createImage(sample);

        if (awtImage instanceof BufferedImage) {
            bufferedImage = (BufferedImage) awtImage;
        } else {
            bufferedImage = new BufferedImage(awtImage.getWidth(null), awtImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D bGr = bufferedImage.createGraphics();
            bGr.drawImage(awtImage, 0, 0, null);
            bGr.dispose();
        }

        Image fingerprintImage = SwingFXUtils.toFXImage(bufferedImage, null);
        fingerprintImageView.setImage(fingerprintImage);

        if (registroController != null || edicionController != null) {
            if (registroController != null){
                registroController.updateFingerprintImage(fingerprintImage);
            } else if (edicionController != null) {
                edicionController.updateFingerprintImage(fingerprintImage);
            }


        }
    }

    private void saveTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar plantilla de huella digital");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fingerprint Template", "*.ser"));
        Stage stage = (Stage) continuarButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fileOut = new FileOutputStream(file);
                 ObjectOutputStream out = new ObjectOutputStream(fileOut)) {

                out.writeObject(template.serialize());

                statusLabel.setText("Template guardado exitosamente en " + file.getAbsolutePath());
            } catch (Exception e) {
                statusLabel.setText("Error al guardar el template: " + e.getMessage());
            }
        }
    }

    @FXML
    private void closeWindow() {
        stopCapture();
        Stage stage = (Stage) stopButton.getScene().getWindow();
        stage.close();
    }
}