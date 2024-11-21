package controllers;

import DAO.BaseDAO;
import DAO.UsuariosDAO;
import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.*;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.*;
import com.digitalpersona.onetouch.verification.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ForgotPasswordPopupController {

    @FXML
    private ImageView fingerprintImageView;

    @FXML
    private Button cambiarButton;

    @FXML
    private Label messageLabel;

    private DPFPCapture capturer;
    private DPFPVerification verifier;
    private String userEmail; // Correo del usuario identificado
    private UsuariosDAO usuariosDAO;

    public ForgotPasswordPopupController() throws SQLException {
        this.usuariosDAO = new UsuariosDAO(DatabaseConnection.getConnection());
    }

    @FXML
    public void initialize() {
        capturer = DPFPGlobal.getCaptureFactory().createCapture();
        verifier = DPFPGlobal.getVerificationFactory().createVerification(DPFPVerification.MEDIUM_SECURITY_FAR);
        cambiarButton.setDisable(true); // El botón comienza deshabilitado
        initCaptureEvents();
        startCapture();

        // Asegurar que el sensor se detenga al cerrar la ventana
        Platform.runLater(() -> {
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                stopCapture();
                System.out.println("Captura detenida al cerrar la ventana.");
            });
        });
    }

    private void initCaptureEvents() {
        capturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(DPFPDataEvent e) {
                Platform.runLater(() -> {
                    showFingerprintImage(e.getSample());
                    verifyFingerprint(e.getSample());
                });
            }
        });
    }

    private void startCapture() {
        capturer.startCapture();
        messageLabel.setText("Coloque su dedo en el lector.");
    }

    private void stopCapture() {
        capturer.stopCapture();
    }

    private void showFingerprintImage(DPFPSample sample) {
        BufferedImage bufferedImage = (BufferedImage) DPFPGlobal.getSampleConversionFactory().createImage(sample);
        Image fingerprintImage = SwingFXUtils.toFXImage(bufferedImage, null);
        fingerprintImageView.setImage(fingerprintImage);
    }

    private void verifyFingerprint(DPFPSample sample) {
        DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (features != null) {
            try {
                List<Map<String, Object>> huellas = BaseDAO.obtenerHuellasEmpleadosActivos();

                boolean found = false;

                for (Map<String, Object> huellaData : huellas) {
                    byte[] templateBytes = (byte[]) huellaData.get("huella");

                    if (templateBytes != null) {
                        DPFPTemplate template = DPFPGlobal.getTemplateFactory().createTemplate(templateBytes);

                        DPFPVerificationResult result = verifier.verify(features, template);

                        if (result.isVerified()) {
                            String nombreEmpleado = (String) huellaData.get("nombres");
                            String apellidoPaterno = (String) huellaData.get("apellido_paterno");
                            userEmail = (String) huellaData.get("correo_electronico"); // Obtenemos el correo del usuario

                            Platform.runLater(() -> {
                                messageLabel.setText("Huella verificada correctamente.");
                                cambiarButton.setDisable(false); // Habilitamos el botón
                            });
                            stopCapture();
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    Platform.runLater(() -> messageLabel.setText("Huella no reconocida. Intente nuevamente."));
                }
            } catch (SQLException e) {
                Platform.runLater(() -> messageLabel.setText("Error al verificar huella: " + e.getMessage()));
            }
        } else {
            Platform.runLater(() -> messageLabel.setText("No se pudieron extraer características de la huella."));
        }
    }

    private DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            Platform.runLater(() -> messageLabel.setText("Error al extraer características de la huella: " + e.getMessage()));
            return null;
        }
    }

    @FXML
    private void handleSendPasswordAction() {
        if (userEmail != null) {
            try {
                // Generar una contraseña temporal
                String temporalPassword = generarContraseñaTemporal();

                // Hashear la contraseña temporal
                String hashedTemporalPassword = BCrypt.hashpw(temporalPassword, BCrypt.gensalt());

                // Actualizar la contraseña temporal en la base de datos
                usuariosDAO.actualizarContrasenaTemporal(userEmail, hashedTemporalPassword);

                // Enviar la contraseña temporal por correo
                String mensajeCorreo = "Tu nueva contraseña es: " + temporalPassword +
                        "\n\nEsta será tu nueva contraseña para iniciar sesión.";
                MailController.EmailSender.enviarCorreoConContrasena(userEmail, mensajeCorreo);

                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Correo enviado exitosamente.");
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Error al procesar la solicitud.");
            }
        } else {
            messageLabel.setText("No se pudo identificar al usuario.");
        }
    }

    private String generarContraseñaTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder contraseña = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int indice = (int) (Math.random() * caracteres.length());
            contraseña.append(caracteres.charAt(indice));
        }
        return contraseña.toString();
    }


}
