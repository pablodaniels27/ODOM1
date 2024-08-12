package Lector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;

public class CaptureForm extends JFrame {
    private DPFPCapture capturer;
    private JLabel statusLabel;

    public CaptureForm() {
        super("Capture Form");

        capturer = DPFPGlobal.getCaptureFactory().createCapture();

        capturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(DPFPDataEvent e) {
                statusLabel.setText("Fingerprint data acquired.");
                process(e.getSample());
            }
        });

        capturer.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            @Override
            public void readerConnected(DPFPReaderStatusEvent e) {
                statusLabel.setText("The fingerprint reader was connected.");
            }

            @Override
            public void readerDisconnected(DPFPReaderStatusEvent e) {
                statusLabel.setText("The fingerprint reader was disconnected.");
            }
        });

        capturer.addErrorListener(new DPFPErrorAdapter() {
            
            public void errorOccurred(DPFPErrorEvent e) {
                statusLabel.setText("Error: " + e.getError());
            }
        });

        statusLabel = new JLabel("Status: ");
        JButton startButton = new JButton("Start Capture");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startCapture();
            }
        });

        JButton stopButton = new JButton("Stop Capture");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopCapture();
            }
        });

        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.NORTH);
        add(startButton, BorderLayout.CENTER);
        add(stopButton, BorderLayout.SOUTH);
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    void startCapture() {
        capturer.startCapture();
        statusLabel.setText("Using the fingerprint reader, scan your fingerprint.");
    }

    void stopCapture() {
        capturer.stopCapture();
        statusLabel.setText("Capture stopped.");
    }

    private void process(DPFPSample sample) {
        // Este método debe ser sobrescrito por las clases que extiendan CaptureForm
    }

    public static void main(String[] args) {
        CaptureForm captureForm = new CaptureForm();
        captureForm.setVisible(true);
    }
}

