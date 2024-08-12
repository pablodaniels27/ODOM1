package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Lector.EnrollmentFormController;

public class RegistroController {

    @FXML
    private TextField nombreField;
    @FXML
    private TextField apellidoMaternoField;
    @FXML
    private TextField apellidoPaternoField;
    @FXML
    private TextField fechaNacimientoField;
    @FXML
    private TextField paisField;
    @FXML
    private TextField ciudadField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ladaField;
    @FXML
    private TextField telefonoField;
    @FXML
    private TextField rfcField;
    @FXML
    private TextField curpField;
    @FXML
    private TextField profesionField;
    @FXML
    private TextArea notasArea;
    @FXML
    private Button ingresarHuellaButton;
    @FXML
    private Button enviarButton;
    @FXML
    private Button borrarButton;

    @FXML
    public void initialize() {
        // Esta línea es opcional si decides manejar la acción en el método handleIngresarHuellaButton
        //ingresarHuellaButton.setOnAction(event -> openEnrollmentForm());
    }

    @FXML
    public void handleIngresarHuellaButton(ActionEvent event) {
        openEnrollmentForm();
    }

    private void openEnrollmentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EnrollmentForm.fxml"));
            Parent root = loader.load();

            // Si necesitas pasar datos entre controladores, puedes obtener el controlador y configurarlo aquí.
            EnrollmentFormController controller = loader.getController();
            // Pasar datos al controlador si es necesario

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Enrollment Form");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Métodos para manejar otros botones
}
