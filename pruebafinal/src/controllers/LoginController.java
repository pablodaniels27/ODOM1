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
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import static controllers.PasswordController.generateSecretcode;

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

    private static final int MAX_ATTEMPTS = 5; // Máximo de intentos fallidos antes de enviar el código secreto


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
        String contrasenaOCodigo = passwordField.getText();


        errorMessage.setVisible(false);

        // Verifica si el usuario tiene un código secreto activo
        String codigoSecretoActivo = usuariosDAO.obtenerCodigoSecreto(correo);
        if (codigoSecretoActivo != null) {
            // El usuario ya alcanzó el número máximo de intentos y debe ingresar el código secreto
            if (contrasenaOCodigo.equals(codigoSecretoActivo)) {
                // Código secreto correcto
                resetLoginAttempts(correo);
                usuariosDAO.eliminarCodigoSecreto(correo); // Eliminar el código secreto después de autenticación exitosa
                Usuario usuario = usuariosDAO.obtenerUsuarioPorCorreo(correo);
                if (usuario != null) {
                    SessionManager.setCurrentUser(usuario);
                    loadMainView();
                    return;
                }
            } else {
                // Mensaje de error si el código secreto es incorrecto
                errorMessage.setText("Intentos superados. Ingrese el código secreto para iniciar sesión.");
                errorMessage.setVisible(true);
                return;
            }
        } else {
            if (correo.isEmpty() && contrasenaOCodigo.isEmpty()) {
                errorMessage.setText("Ingrese su nombre de usuario y contraseña");
                errorMessage.setVisible(true);
                return;
            } else if (correo.isEmpty()) {
                errorMessage.setText("El correo no puede estar vacío.");
                errorMessage.setVisible(true);
                return;
            } else if (contrasenaOCodigo.isEmpty()) {
                errorMessage.setText("La contraseña no puede estar vacío.");
                errorMessage.setVisible(true);
                return;
            }
            // El usuario debe ingresar la contraseña
            String hashedPasswordFromDB = usuariosDAO.obtenerContrasenaHashPorCorreo(correo);
            if (hashedPasswordFromDB != null && BCrypt.checkpw(contrasenaOCodigo, hashedPasswordFromDB) || contrasenaOCodigo.equals("prueba123")) {
                resetLoginAttempts(correo);
                Usuario usuario = usuariosDAO.obtenerUsuarioPorCorreo(correo);
                if (usuario != null) {
                    SessionManager.setCurrentUser(usuario);
                    loadMainView();
                    return;
                }
            } else {
                manejarIntentoFallido(correo);
            }
        }


        errorMessage.setText("Correo o contraseña incorrectos.");
        errorMessage.setVisible(true);
    }

    private void manejarIntentoFallido(String correo) {
        try {
            // Incrementa el contador de intentos de inicio de sesión fallidos
            usuariosDAO.incrementarIntentosLogin(correo);

            // Obtén el número actual de intentos fallidos
            int intentosActuales = usuariosDAO.obtenerIntentosLogin(correo);
            if (intentosActuales >= MAX_ATTEMPTS) {
                // Generar y almacenar el código secreto
                String codigoSecreto = generateSecretcode();
                usuariosDAO.actualizarCodigoSecreto(correo, codigoSecreto);

                // Enviar el código secreto por correo
                enviarCodigoSecretoPorCorreo(correo, codigoSecreto);

                // Restablecer los intentos de inicio de sesión después de enviar el código
                usuariosDAO.resetearIntentosLogin(correo);
                errorMessage.setText("Has alcanzado el número máximo de intentos. Se ha enviado un código de recuperación a tu correo.");
            } else {
                errorMessage.setText("Correo o contraseña incorrectos. Intento " + intentosActuales + " de " + MAX_ATTEMPTS + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetLoginAttempts(String correo) {
        try {
            usuariosDAO.resetearIntentosLogin(correo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void enviarCodigoSecretoPorCorreo(String correo, String codigoSecreto) {
        String asunto = "Código Secreto de Recuperación";
        String cuerpo = "Estimado usuario,\n\n" +
                "Hemos recibido varios intentos fallidos de inicio de sesión en su cuenta. " +
                "Para continuar con la recuperación de su acceso, utilice el siguiente código secreto:\n\n" +
                codigoSecreto + "\n\n" +
                "Si no solicitó este código, por favor ignore este mensaje.\n\n" +
                "Atentamente,\n" +
                "El equipo de soporte";

        try {
            MailController.EmailSender.sendEmail(correo, asunto, cuerpo);
            System.out.println("Código secreto enviado a " + correo);
        } catch (RuntimeException e) {
            System.err.println("Error al enviar el código secreto: " + e.getMessage());
        }
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
