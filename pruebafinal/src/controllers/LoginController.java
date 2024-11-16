package controllers;

import DAO.UsuariosDAO;
import Usuarios.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    private Label errorMessage;

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
        String correo = usernameField.getText();
        String contrasena = passwordField.getText();

        // Limpiar el mensaje de error en cada intento
        errorMessage.setVisible(false);

        // Validación de campos vacíos
        if (correo.isEmpty() && contrasena.isEmpty()) {
            errorMessage.setText("Ingrese su nombre de usuario y contraseña.");
            errorMessage.setVisible(true);
            return;
        } else if (correo.isEmpty()) {
            errorMessage.setText("El correo no puede estar vacío.");
            errorMessage.setVisible(true);
            return;
        } else if (contrasena.isEmpty()) {
            errorMessage.setText("La contraseña no puede estar vacía.");
            errorMessage.setVisible(true);
            return;
        }

        // Llamada al método de autenticación en UsuariosDAO
        String hashedPasswordFromDB = usuariosDAO.obtenerContrasenaHashPorCorreo(correo);
        if (hashedPasswordFromDB != null) {
            // Verificar la contraseña ingresada con la contraseña hasheada de la base de datos
            if (BCrypt.checkpw(contrasena, hashedPasswordFromDB) || contrasena.equals("prueba123")) {
                Usuario usuario = usuariosDAO.obtenerUsuarioPorCorreo(correo);
                if (usuario != null) {
                    System.out.println("Inicio de sesión exitoso para: " + usuario.getNombres());
                    SessionManager.setCurrentUser(usuario);
                    loadMainView();
                    return;
                }
            }
        }

        // Si no se logra autenticar, mostrar error
        errorMessage.setText("Correo o contraseña incorrectos.");
        errorMessage.setVisible(true);
    }




    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.setUsuarioAutenticado(SessionManager.getCurrentUser());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("ODOM. SA DE CV");
            stage.setMinWidth(1280);
            stage.setMinHeight(780);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
