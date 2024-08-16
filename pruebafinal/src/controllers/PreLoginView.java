package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PreLoginView {

    @FXML
    private Button loginButton;

    @FXML
    private ImageView gifImageView;

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    public void initialize() {
        // Cargar el GIF desde el directorio de recursos
        Image gifImage = new Image(getClass().getResourceAsStream("/resources/Gif.GIF"));
        gifImageView.setImage(gifImage);
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
    private void popup() {
        try {
            // Cargar el archivo FXML del popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Identificarse.fxml"));
            Parent root = loader.load();

            // Configurar la ventana emergente (popup)
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);  // Bloquea la ventana principal hasta que se cierre el popup
            popupStage.setTitle("Identificación de Usuario");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();  // Muestra y espera a que el popup se cierre
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterEntryAction() {
        // Lógica para manejar la acción de registrar entrada
    }
}