package controllers;

import Lector.EnrollmentFormController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegistroController {

    @FXML
    private TextField nombreField;
    @FXML
    private TextField apellidoMaternoField;
    @FXML
    private TextField apellidoPaternoField;
    @FXML
    private DatePicker fechaNacimientoPicker;
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
    private ChoiceBox<String> departamentoChoiceBox;
    @FXML
    private ChoiceBox<String> puestoChoiceBox;

    @FXML
    private ImageView imageView;

    @FXML
    private void initialize() {
        System.out.println("Inicializando...");

        // Cargar datos en los ChoiceBox
        cargarDepartamentos();
        cargarPuestos();

        // Manejar la selección en el ChoiceBox de departamentos
        departamentoChoiceBox.setOnAction(event -> {
            String selectedDepartamento = departamentoChoiceBox.getSelectionModel().getSelectedItem();
            System.out.println("Departamento seleccionado: " + selectedDepartamento);
        });

        // Manejar la selección en el ChoiceBox de puestos
        puestoChoiceBox.setOnAction(event -> {
            String selectedPuesto = puestoChoiceBox.getSelectionModel().getSelectedItem();
            System.out.println("Puesto seleccionado: " + selectedPuesto);
        });
    }

    private void cargarDepartamentos() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM departamentos";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                departamentoChoiceBox.getItems().add(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarPuestos() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM jerarquias";  // La tabla `jerarquias` representa los puestos
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                puestoChoiceBox.getItems().add(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void enviarDatos() {
        // Obtener los datos de los campos de texto
        String nombre = nombreField.getText();
        String apellidoMaterno = apellidoMaternoField.getText();
        String apellidoPaterno = apellidoPaternoField.getText();
        LocalDate fechaNacimiento = fechaNacimientoPicker.getValue();  // Cambiado para obtener la fecha del DatePicker
        String fechaNacimientoFormatted = fechaNacimiento.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String pais = paisField.getText();
        String ciudad = ciudadField.getText();
        String email = emailField.getText();
        String lada = ladaField.getText();
        String telefono = telefonoField.getText();
        String rfc = rfcField.getText();
        String curp = curpField.getText();
        String profesion = profesionField.getText();

        // Obtener los valores seleccionados en los ChoiceBox
        String departamentoSeleccionado = departamentoChoiceBox.getSelectionModel().getSelectedItem();
        String puestoSeleccionado = puestoChoiceBox.getSelectionModel().getSelectedItem();

        // Conexión a la base de datos e inserción de datos
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Encontrar el ID del departamento seleccionado
            String departamentoQuery = "SELECT id FROM departamentos WHERE nombre = ?";
            PreparedStatement departamentoStatement = connection.prepareStatement(departamentoQuery);
            departamentoStatement.setString(1, departamentoSeleccionado);
            ResultSet departamentoResult = departamentoStatement.executeQuery();
            int departamentoId = 0;
            if (departamentoResult.next()) {
                departamentoId = departamentoResult.getInt("id");
            }

            // Encontrar el ID del puesto (jerarquía) seleccionado
            String puestoQuery = "SELECT id FROM jerarquias WHERE nombre = ?";
            PreparedStatement puestoStatement = connection.prepareStatement(puestoQuery);
            puestoStatement.setString(1, puestoSeleccionado);
            ResultSet puestoResult = puestoStatement.executeQuery();
            int jerarquiaId = 0;
            if (puestoResult.next()) {
                jerarquiaId = puestoResult.getInt("id");
            }

            // Insertar los datos del empleado
            String sql = "INSERT INTO empleados (nombres, apellido_materno, apellido_paterno, fecha_nacimiento, pais, ciudad, correo_electronico, lada, telefono, rfc, curp, profesion, departamento_id, jerarquia_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, apellidoMaterno);
            statement.setString(3, apellidoPaterno);
            statement.setString(4, fechaNacimientoFormatted);  // Usar la fecha formateada
            statement.setString(5, pais);
            statement.setString(6, ciudad);
            statement.setString(7, email);
            statement.setString(8, lada);
            statement.setString(9, telefono);
            statement.setString(10, rfc);
            statement.setString(11, curp);
            statement.setString(12, profesion);
            statement.setInt(13, departamentoId);  // Usar el ID del departamento
            statement.setInt(14, jerarquiaId);     // Usar el ID de la jerarquía

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
