package Lector;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.EnumMap;

public class SimpleVerificationForm extends JFrame {
    private EnumMap<DPFPFingerIndex, DPFPTemplate> templates;
    private int farRequested;
    private int farAchieved;
    private boolean matched;
    private JLabel statusLabel;

    public SimpleVerificationForm(EnumMap<DPFPFingerIndex, DPFPTemplate> templates) {
        super("Fingerprint Verification");
        this.templates = templates;
        this.farRequested = DPFPVerification.MEDIUM_SECURITY_FAR;

        setupUI();
    }

    private void setupUI() {
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        statusLabel = new JLabel("To verify your identity, load a fingerprint template.", JLabel.CENTER);

        JButton loadTemplateButton = new JButton("Load Template for Verification");
        loadTemplateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verifyFingerprint();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false); // End Dialog
                dispose();
            }
        });

        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        center.add(statusLabel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(loadTemplateButton);
        bottom.add(closeButton);

        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.PAGE_END);
    }

    public int getFAR() {
        return farAchieved;
    }

    protected void setFAR(int far) {
        final int old = getFAR();
        farAchieved = far;
        firePropertyChange("FAR", old, getFAR());
    }

    public boolean getMatched() {
        return matched;
    }

    protected void setMatched(boolean matched) {
        final boolean old = getMatched();
        this.matched = matched;
        firePropertyChange("Matched", old, getMatched());
    }

    public void verifyFingerprint() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a fingerprint template to compare");
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileInputStream fileIn = new FileInputStream(file);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {
                byte[] templateData = (byte[]) in.readObject();
                DPFPTemplate loadedTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateData);

                // Obtener la última muestra capturada desde EnrollmentForm
                EnrollmentForm enrollmentForm = (EnrollmentForm) getOwner();
                DPFPSample currentSample = enrollmentForm.getLastSample();
                DPFPFeatureSet features = extractFeatures(currentSample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

                DPFPVerification verifier = DPFPGlobal.getVerificationFactory().createVerification(farRequested);
                DPFPVerificationResult result = verifier.verify(features, loadedTemplate);

                if (result.isVerified()) {
                    setMatched(true);
                    setFAR(result.getFalseAcceptRate());
                    statusLabel.setText("Fingerprint match successful.");
                } else {
                    setMatched(false);
                    statusLabel.setText("Fingerprint did not match.");
                }
            } catch (Exception e) {
                statusLabel.setText("Error loading or verifying template: " + e.getMessage());
            }
        }
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

    @Override
    public void setVisible(boolean b) {
        if (b) {
            matched = false;
        }
        super.setVisible(b);
    }
}
