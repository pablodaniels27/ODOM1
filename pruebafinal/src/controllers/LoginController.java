package controllers;

import DAO.UsuariosDAO;
import Usuarios.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.Connection;

import org.mindrot.jbcrypt.BCrypt;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView backgroundImage;
    @FXML
    private Button loginButton;

    @FXML
    private SplitPane splitPane;

    private Stage primaryStage;
    private UsuariosDAO usuariosDAO;
    private Connection conexion;

    // Constructor sin parámetros
    public LoginController() {
    }

    @FXML
    public void initialize() {
        PasswordHashing();
        splitPane.setDividerPositions(0.3);
        splitPane.lookupAll(".split-pane-divider").forEach(div -> div.setMouseTransparent(true));
        backgroundImage.fitWidthProperty().bind(splitPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(splitPane.heightProperty());
        setConexion();
    }


    public  void  PasswordHashing() {
        String password = "prueba123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));  // Generar un hash válido
        System.out.println(hashedPassword);
    }


    public void setConexion() {
        try {
            // Usa DatabaseConnection para obtener la conexión
            this.conexion = DatabaseConnection.getConnection();
            this.usuariosDAO = new UsuariosDAO(conexion);  // Inicializa UsuariosDAO con la conexión obtenida
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLoginAction();
        }
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void handleLoginAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Llamada al método autenticar del UsuariosDAO
        Usuario usuario = usuariosDAO.autenticar(username, password);

        if (usuario != null) {
            System.out.println("Inicio de sesión exitoso para: " + usuario.getNombre());

            // Guardar el usuario autenticado en el SessionManager
            SessionManager.setCurrentUser(usuario);

            // Cargar la vista principal y pasar el usuario autenticado al MainController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            try {
                Parent root = loader.load();

                // Obtener el controlador de la vista principal
                MainController mainController = loader.getController();
                mainController.setUsuarioAutenticado(usuario);  // Pasar el usuario autenticado

                // Mostrar la vista principal
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("ODOM. SA DE CV");
                stage.setMinWidth(1280);
                stage.setMinHeight(780);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // Mostrar mensaje de error si las credenciales son incorrectas
            System.out.println("Credenciales incorrectas.");
        }
    }

    private void showMainView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle("ODOM. SA DE CV");
            primaryStage.setMinWidth(1280);
            primaryStage.setMinHeight(780);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToPreLoginAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PreLoginView.fxml"));
            Parent root = loader.load();

            PreLoginView preLoginController = loader.getController();
            preLoginController.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("ODOM. SA DE CV - PreLogin");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}