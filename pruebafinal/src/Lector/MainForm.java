package Lector;

import com.digitalpersona.onetouch.DPFPFingerIndex;
import com.digitalpersona.onetouch.DPFPTemplate;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

public class MainForm extends JFrame {
    private final EnumMap<DPFPFingerIndex, DPFPTemplate> templates;

    public MainForm() {
        setTitle("Fingerprint Enrollment and Verification");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center the window

        templates = new EnumMap<>(DPFPFingerIndex.class);

        JButton enrollButton = new JButton("Enroll Fingerprint");
        JButton verifyButton = new JButton("Verify Fingerprint");

        enrollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EnrollmentForm enrollmentForm = new EnrollmentForm(templates);
                enrollmentForm.setVisible(true);
                enrollmentForm.startCapture();
            }
        });

        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleVerificationForm verificationForm = new SimpleVerificationForm(templates);
                verificationForm.setVisible(true);
            }
        });

        setLayout(new FlowLayout());
        add(enrollButton);
        add(verifyButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainForm mainForm = new MainForm();
                mainForm.setVisible(true);
            }
        });
    }
}
