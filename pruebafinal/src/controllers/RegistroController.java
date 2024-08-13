package controllers;

import Lector.EnrollmentFormController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    private ComboBox<String> departamentoComboBox;
    @FXML
    private ComboBox<String> puestoComboBox;

    @FXML
    private ImageView imageView;

    @FXML
    private void initialize() {
        // Inicializar ComboBox con datos de la base de datos
        cargarDepartamentos();
        cargarPuestos();
    }

    private void cargarDepartamentos() {
        ObservableList<String> departamentos = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM departamentos";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                departamentos.add(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        departamentoComboBox.setItems(departamentos);
    }

    private void cargarPuestos() {
        ObservableList<String> puestos = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM jerarquias";  // Asumiendo que la tabla jerarquias ahora representa puestos
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                puestos.add(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        puestoComboBox.setItems(puestos);
    }

    @FXML
    private void enviarDatos() {
        // Obtener los datos de los campos de texto
        String nombre = nombreField.getText();
        String apellidoMaterno = apellidoMaternoField.getText();
        String apellidoPaterno = apellidoPaternoField.getText();
        String fechaNacimiento = fechaNacimientoField.getText();
        String pais = paisField.getText();
        String ciudad = ciudadField.getText();
        String email = emailField.getText();
        String lada = ladaField.getText();
        String telefono = telefonoField.getText();
        String rfc = rfcField.getText();
        String curp = curpField.getText();
        String profesion = profesionField.getText();

        // Obtener los datos de los ComboBox
        String departamento = departamentoComboBox.getValue();
        String puesto = puestoComboBox.getValue();

        // Conexión a la base de datos e inserción de datos
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO empleados (nombres, apellido_materno, apellido_paterno, fecha_nacimiento, pais, ciudad, correo_electronico, lada, telefono, rfc, curp, profesion, departamento_id, puesto_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT id FROM departamentos WHERE nombre = ?), (SELECT id FROM jerarquias WHERE nombre = ?))";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, apellidoMaterno);
            statement.setString(3, apellidoPaterno);
            statement.setString(4, fechaNacimiento);
            statement.setString(5, pais);
            statement.setString(6, ciudad);
            statement.setString(7, email);
            statement.setString(8, lada);
            statement.setString(9, telefono);
            statement.setString(10, rfc);
            statement.setString(11, curp);
            statement.setString(12, profesion);
            statement.setString(13, departamento);  // Buscar ID del departamento
            statement.setString(14, puesto);        // Buscar ID del puesto

            statement.executeUpdate();

            // Aquí podrías añadir un mensaje de confirmación o redirigir a la vista de listado de empleados
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
