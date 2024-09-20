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

import java.util.Objects;

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

            popupStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/ODOM.jpg"))));

            // Crear la escena y establecerla en el stage
            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            // Obtener el controlador del popup
            IdentificarseController identificarseController = loader.getController();

            // Añadir un evento de cierre para asegurarse de que los recursos se liberen
            popupStage.setOnHidden(event -> {
                identificarseController.closeWindow();  // Asegúrate de liberar los recursos en este método
            });

            // Establecer tamaño máximo y mínimo de la ventana emergente
            popupStage.setMinHeight(350);
            popupStage.setMinWidth(350);

            // Desactivar la capacidad de cambiar el tamaño de la ventana
            popupStage.setResizable(false);

            // Mostrar y esperar a que el popup se cierre
            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterEntryAction() {
        // Lógica para manejar la acción de registrar entrada
    }
}
