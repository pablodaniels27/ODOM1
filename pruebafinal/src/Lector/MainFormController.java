package Lector;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class MainFormController {

    @FXML
    private Button enrollButton;

    @FXML
    private Button verifyButton;

    @FXML
    public void handleEnrollButtonAction() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("EnrollmentForm.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Enrollment Form");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleVerifyButtonAction() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("SimpleVerificationForm.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Verification Form");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
