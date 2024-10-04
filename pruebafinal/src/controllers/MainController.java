package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
    private StackPane mainContent;

    @FXML
    private Button editButton; // Botón que será usado para cargar la vista de edición

    @FXML
    public void initialize() {
        // Inicialmente, el botón de edición está oculto
        editButton.setVisible(false);

        // Cargar la vista de inicio al cargar el MainView
        showInicio();
    }

    @FXML
    private void handleLoginAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

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

    @FXML
    public void showAuditoria() { loadContent("/views/Auditoria.fxml");}




    @FXML
    public void showEdicion(int empleadoId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EdicionView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador de la vista de edición
            EdicionController edicionController = loader.getController();
            edicionController.cargarDatosEmpleado(empleadoId); // Método para cargar los datos del empleado en la vista de edición

            // Cargar la vista en el mainContent manteniendo la barra de navegación
            mainContent.getChildren().clear();
            mainContent.getChildren().add(root);

            // Mostrar el botón de edición en la barra azul
            editButton.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            // Ocultar el botón de edición siempre que se cargue otra vista
            editButton.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            POSApplication mainApp = new POSApplication();
            mainApp.start(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para ser llamado desde el botón de la barra azul para cargar la vista de edición
    @FXML
    private void handleEditAction() {
        showEdicion(-1); // Utiliza un id específico si tienes que cargar algo en particular
    }
}