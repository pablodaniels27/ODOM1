package Lector;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.*;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.*;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;

public class EnrollmentForm extends JFrame {
    private final DPFPEnrollment enrollment;
    private DPFPTemplate template;
    private final DPFPCapture capturer;
    private JLabel statusLabel;
    private JLabel idLabel;
    private JLabel imageLabel;
    private File saveLocation;
    private final EnumMap<DPFPFingerIndex, DPFPTemplate> templates;
    private String fingerprintID;
    private JButton saveTemplateButton;
    private JButton verifyButton;
    private DPFPSample lastSample;  // Guardar la última muestra capturada

    public EnrollmentForm(EnumMap<DPFPFingerIndex, DPFPTemplate> templates) {
        super("Fingerprint Enrollment");
        this.templates = templates;
        this.enrollment = DPFPGlobal.getEnrollmentFactory().createEnrollment();
        this.capturer = DPFPGlobal.getCaptureFactory().createCapture();

        setupUI();
        initCaptureEvents();
    }

    private void setupUI() {
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        statusLabel = new JLabel("Status: ");
        idLabel = new JLabel("Fingerprint ID: Not generated yet");
        imageLabel = new JLabel();

        saveTemplateButton = new JButton("Save Template");
        saveTemplateButton.setEnabled(false);
        saveTemplateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (template != null && saveLocation != null) {
                    saveTemplateAsJPG(lastSample, fingerprintID + "_fingerprint.jpg");
                    saveSerializedTemplate(template, fingerprintID + "_fingerprint_template.ser");
                } else {
                    setStatus("Template not ready or save location not chosen.");
                }
            }
        });

        verifyButton = new JButton("Verify Fingerprint");
        verifyButton.setEnabled(false); // Inicialmente deshabilitado
        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verifyFingerprint();
            }
        });

        JButton chooseLocationButton = new JButton("Choose Save Location");
        chooseLocationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseSaveLocation();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(idLabel, BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(chooseLocationButton);
        buttonPanel.add(saveTemplateButton);
        buttonPanel.add(verifyButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
    }

    private void initCaptureEvents() {
        capturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(DPFPDataEvent e) {
                lastSample = e.getSample();  // Guardar la última muestra capturada
                setStatus("Fingerprint captured.");
                processSample(lastSample);
            }
        });

        capturer.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            @Override
            public void readerConnected(DPFPReaderStatusEvent e) {
                setStatus("The fingerprint reader was connected.");
            }

            @Override
            public void readerDisconnected(DPFPReaderStatusEvent e) {
                setStatus("The fingerprint reader was disconnected.");
            }
        });

        capturer.addErrorListener(new DPFPErrorAdapter() {

            public void errorReader(DPFPErrorEvent e) {
                setStatus("Error: " + e.getError());
            }
        });
    }

    void startCapture() {
        try {
            capturer.startCapture();
            setStatus("Using the fingerprint reader, scan your fingerprint.");
        } catch (RuntimeException e) {
            setStatus("Failed to start capture: " + e.getMessage());
        }
    }

    private void stopCapture() {
        capturer.stopCapture();
    }

    private void processSample(DPFPSample sample) {
        showFingerprintImage(sample);  // Mostrar la imagen de la huella
        DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

        if (features != null) {
            try {
                enrollment.addFeatures(features);  // Agregar el conjunto de características a la plantilla.
                switch (enrollment.getTemplateStatus()) {
                    case TEMPLATE_STATUS_READY:
                        stopCapture();
                        template = enrollment.getTemplate();
                        fingerprintID = generateFingerprintID(features);  // Generar un ID único
                        idLabel.setText("Fingerprint ID: " + fingerprintID);
                        setStatus("Fingerprint template is ready. Choose save location and save.");
                        saveTemplateButton.setEnabled(true);  // Habilitar el botón para guardar
                        verifyButton.setEnabled(true);  // Habilitar el botón de verificación
                        break;

                    case TEMPLATE_STATUS_FAILED:
                        enrollment.clear();
                        stopCapture();
                        startCapture();
                        setStatus("Fingerprint template is not valid. Scan again.");
                        break;
                }
            } catch (DPFPImageQualityException ex) {
                setStatus("Error: " + ex.getMessage());
            }
        } else {
            setStatus("No features extracted from fingerprint sample.");
        }
    }

    private void showFingerprintImage(DPFPSample sample) {
        Image image = DPFPGlobal.getSampleConversionFactory().createImage(sample);
        BufferedImage bufferedImage = toBufferedImage(image);
        if (bufferedImage != null) {
            ImageIcon icon = new ImageIcon(bufferedImage);
            imageLabel.setIcon(icon);
        } else {
            setStatus("Error displaying fingerprint image.");
        }
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    private String generateFingerprintID(DPFPFeatureSet features) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(features.serialize());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Error generating ID";
        }
    }

    private DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose) {
        try {
            DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            setStatus("Error extracting features: " + e.getMessage());
            return null;
        }
    }

    private void chooseSaveLocation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            saveLocation = fileChooser.getSelectedFile();
            setStatus("Save location set to: " + saveLocation.getAbsolutePath());
        } else {
            saveLocation = null;
            setStatus("Save location not chosen.");
        }
    }

    private void saveSerializedTemplate(DPFPTemplate template, String filename) {
        if (saveLocation != null) {
            try {
                File file = new File(saveLocation, filename);
                try (FileOutputStream fileOut = new FileOutputStream(file);
                     ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                    out.writeObject(template.serialize());
                    setStatus("Fingerprint template saved to " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                setStatus("Error saving fingerprint template: " + e.getMessage());
            }
        } else {
            setStatus("Save location not chosen.");
        }
    }

    private void saveTemplateAsJPG(DPFPSample sample, String filename) {
        if (saveLocation != null) {
            BufferedImage bufferedImage = toBufferedImage(DPFPGlobal.getSampleConversionFactory().createImage(sample));
            if (bufferedImage != null) {
                try {
                    File file = new File(saveLocation, filename);
                    ImageIO.write(bufferedImage, "jpg", file);
                    setStatus("Fingerprint image saved as JPG to " + file.getAbsolutePath());
                } catch (IOException e) {
                    setStatus("Error saving fingerprint image: " + e.getMessage());
                }
            } else {
                setStatus("Error converting fingerprint sample to image.");
            }
        } else {
            setStatus("Save location not chosen.");
        }
    }

    private void verifyFingerprint() {
        if (lastSample == null) {
            setStatus("No fingerprint captured to verify.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a fingerprint template to compare");
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileInputStream fileIn = new FileInputStream(file);
                 ObjectInputStream in = new ObjectInputStream(fileIn)) {
                byte[] templateData = (byte[]) in.readObject();
                DPFPTemplate loadedTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateData);
                DPFPFeatureSet features = extractFeatures(lastSample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
                DPFPVerification verifier = DPFPGlobal.getVerificationFactory().createVerification();
                DPFPVerificationResult result = verifier.verify(features, loadedTemplate);

                if (result.isVerified()) {
                    setStatus("Fingerprint match successful.");
                } else {
                    setStatus("Fingerprint did not match.");
                }
            } catch (IOException | ClassNotFoundException e) {
                setStatus("Error loading or verifying template: " + e.getMessage());
            }
        }
    }

    // Nuevo método para obtener la última muestra capturada
    public DPFPSample getLastSample() {
        return lastSample;
    }

    private void setStatus(String status) {
        statusLabel.setText(status);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EnumMap<DPFPFingerIndex, DPFPTemplate> templates = new EnumMap<>(DPFPFingerIndex.class);
            EnrollmentForm enrollmentForm = new EnrollmentForm(templates);
            enrollmentForm.setVisible(true);
            enrollmentForm.startCapture();
        });
    }
}
