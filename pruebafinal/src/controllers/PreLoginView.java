package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class PreLoginView {

    @FXML
    private Button loginButton;

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void handleLoginAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Login - ODOM. SA DE CV");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterEntryAction() {
        // Lógica para manejar la acción de registrar entrada
    }

    @FXML
    private void handleRegisterExitAction() {
        // Lógica para manejar la acción de registrar salida
    }
}
