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
import Services.CacheService;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
    private SplitPane splitPane;

    private Stage primaryStage;

    @FXML
    public void initialize() {
        splitPane.setDividerPositions(0.3);
        splitPane.lookupAll(".split-pane-divider").forEach(div -> div.setMouseTransparent(true));
        backgroundImage.fitWidthProperty().bind(splitPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(splitPane.heightProperty());
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

        if (username.equals("") && password.equals("")) {
            // Después de la validación del login, cargamos los datos al caché
            loadEmployeeDataToCache();
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

    // Método que carga los datos de los empleados en el caché
    private void loadEmployeeDataToCache() {
        if (CacheService.isCacheEmpty()) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT id, nombres, apellido_paterno, apellido_materno, profesion FROM empleados";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                List<Map<String, Object>> employees = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("id", resultSet.getInt("id"));
                    employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                    employeeData.put("profesion", resultSet.getString("profesion"));
                    employees.add(employeeData);
                }

                // Guardar los datos en el caché
                CacheService.loadEmployeesIntoCache(employees);

                System.out.println("Datos cargados en el caché");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Datos obtenidos del caché");
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