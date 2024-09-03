package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import views.POSApplication;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainController {

    @FXML
    private VBox loginContainer;

    @FXML
    private VBox mainContainer;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLoginAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Aquí puedes añadir la lógica para verificar las credenciales
        if (username.equals("admin") && password.equals("1234")) {
            // Ocultar el login y mostrar el contenido principal
            loginContainer.setVisible(false);
            mainContainer.setVisible(true);
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Incorrect username or password.");
            alert.showAndWait();
        }
    }

    @FXML
    private StackPane mainContent;
    @FXML
    public void initialize() {
        // Cargar la vista de inicio al cargar el MainView
        showInicio();
    }

    @FXML
    public void showInicio() {
        loadContent("/views/InicioView.fxml");
    }

    @FXML
    public void showRegistro() {
        loadContent("/views/RegistroView.fxml");
    }

    @FXML
    public void showRegistroSucursal() {
        loadContent("/views/RegistroSucursalView.fxml");
    }

    @FXML
    public void showMonitoreo() {
        loadContent("/views/MonitoreoView.fxml");
    }

    @FXML
    public void showTerminacion() {
        loadContent("/views/TerminacionView.fxml");
    }

    @FXML
    public void showAyuda() {
        loadContent("/views/AyudaView.fxml");
    }
    @FXML
    public void showConfiguración() {
        loadContent("/views/ConfiguracionView.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            Parent content = FXMLLoader.load(fxmlUrl);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void cerrarSesion(ActionEvent event) {
        // Obtener la ventana actual
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

        try {
            // Llamar al método start de la clase principal
            POSApplication mainApp = new POSApplication();
            mainApp.start(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

