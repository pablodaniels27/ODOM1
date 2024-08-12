package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class LoginController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private ImageView eyeIcon;

    @FXML
    private StackPane passwordPane;

    @FXML
    private TextField usernameField;

    // Método para manejar el inicio de sesión
    @FXML
    private void handleLoginAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Lógica para verificar las credenciales
        if (username.equals("admin") && password.equals("1234")) {
            // Aquí puedes agregar la lógica para redirigir a la siguiente vista o mostrar un mensaje de éxito.
            System.out.println("Login successful");
        } else {
            // Muestra un mensaje de error en caso de credenciales incorrectas.
            System.out.println("Login failed");
        }
    }

    // Método para mostrar la contraseña en texto claro
    @FXML
    private void showPassword() {
        passwordVisibleField.setText(passwordField.getText());
        passwordVisibleField.setVisible(true);
        passwordField.setVisible(false);
    }

    // Método para ocultar la contraseña
    @FXML
    private void hidePassword() {
        passwordField.setText(passwordVisibleField.getText());
        passwordField.setVisible(true);
        passwordVisibleField.setVisible(false);
    }
}
