package Lector;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.EnumMap;

public class SimpleVerificationFormController {

    @FXML
    private Label statusLabel;

    private EnumMap<DPFPFingerIndex, DPFPTemplate> templates;
    private int farRequested;
    private int farAchieved;
    private boolean matched;

    public SimpleVerificationFormController() {
        this.templates = new EnumMap<>(DPFPFingerIndex.class);
        this.farRequested = DPFPVerification.MEDIUM_SECURITY_FAR;
    }

    @FXML
    public void handleLoadTemplateButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a fingerprint template to compare");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try (FileInputStream fileIn = new FileInputStream(selectedFile);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {
                byte[] templateData = (byte[]) in.readObject();
                DPFPTemplate loadedTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateData);

                DPFPSample currentSample = null; // Debes obtener la muestra actual de alguna manera
                DPFPFeatureSet features = extractFeatures(currentSample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

                DPFPVerification verifier = DPFPGlobal.getVerificationFactory().createVerification(farRequested);
                DPFPVerificationResult result = verifier.verify(features, loadedTemplate);

                if (result.isVerified()) {
                    matched = true;
                    farAchieved = result.getFalseAcceptRate();
                    statusLabel.setText("Fingerprint match successful.");
                } else {
                    matched = false;
                    statusLabel.setText("Fingerprint did not match.");
                }
            } catch (Exception e) {
                statusLabel.setText("Error loading or verifying template: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleCloseButtonAction() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            statusLabel.setText("Error extracting features: " + e.getMessage());
            return null;
        }
    }
}
