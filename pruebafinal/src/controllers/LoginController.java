package controllers;

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
import javafx.scene.layout.AnchorPane;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private Button loginButton;

    @FXML
    private SplitPane splitPane; // Referencia al SplitPane en tu FXML

    private Stage primaryStage;

    @FXML
    public void initialize() {
        splitPane.setDividerPositions(0.3);
        splitPane.lookupAll(".split-pane-divider").forEach(div -> div.setMouseTransparent(true));
        // Vincular el tamaño de la imagen al SplitPane para que se ajuste dinámicamente
        backgroundImage.fitWidthProperty().bind(splitPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(splitPane.heightProperty());
    }

    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLoginAction();
        }
    }

    @FXML
    private ImageView eyeIcon;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
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

    @FXML
    private void handleLoginAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.equals("") && password.equals("")) {
            showMainView();
        } else {
            // Manejar error de login
        }
    }

    private void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle("ODOM. SA DE CV");
            primaryStage.setMinWidth(1040);
            primaryStage.setMinHeight(480);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showPassword() {
        passwordVisibleField.setText(passwordField.getText());
        passwordVisibleField.setVisible(true);
        passwordField.setVisible(false);
    }

    @FXML
    private void hidePassword() {
        passwordField.setText(passwordVisibleField.getText());
        passwordField.setVisible(true);
        passwordVisibleField.setVisible(false);
    }
}