package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class EdicionController {

    @FXML
    private TextField nombreField, apellidoPaternoField, apellidoMaternoField, paisField, ciudadField, emailField, ladaField, telefonoField, rfcField, curpField, profesionField;
    @FXML
    private DatePicker fechaNacimientoPicker;
    @FXML
    private ChoiceBox<String> departamentoChoiceBox, puestoChoiceBox;

    @FXML
    private Button ConfirmarButton, CancelarButton;

    private int empleadoId; // ID del empleado seleccionado

    // Método para inicializar la vista con datos del empleado seleccionado
    public void cargarDatosEmpleado(int id) {
        this.empleadoId = id;

        // Conexión a la base de datos para cargar la información del empleado
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM empleados WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, empleadoId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                nombreField.setText(resultSet.getString("nombres"));
                apellidoPaternoField.setText(resultSet.getString("apellido_paterno"));
                apellidoMaternoField.setText(resultSet.getString("apellido_materno"));
                fechaNacimientoPicker.setValue(resultSet.getDate("fecha_nacimiento").toLocalDate());
                paisField.setText(resultSet.getString("pais"));
                ciudadField.setText(resultSet.getString("ciudad"));
                emailField.setText(resultSet.getString("correo_electronico"));
                ladaField.setText(resultSet.getString("lada"));
                telefonoField.setText(resultSet.getString("telefono"));
                rfcField.setText(resultSet.getString("rfc"));
                curpField.setText(resultSet.getString("curp"));
                profesionField.setText(resultSet.getString("profesion"));

                // Cargar departamento y puesto (esto depende de cómo estén implementadas tus tablas)
                departamentoChoiceBox.getSelectionModel().select(resultSet.getString("departamento_id")); // Cambia según el campo
                puestoChoiceBox.getSelectionModel().select(resultSet.getString("jerarquia_id")); // Cambia según el campo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar los datos del empleado
    @FXML
    private void enviarDatos() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Actualizar la información del empleado en la base de datos
            String updateSQL = "UPDATE empleados SET nombres = ?, apellido_paterno = ?, apellido_materno = ?, fecha_nacimiento = ?, pais = ?, ciudad = ?, correo_electronico = ?, lada = ?, telefono = ?, rfc = ?, curp = ?, profesion = ?, departamento_id = ?, jerarquia_id = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(updateSQL);
            statement.setString(1, nombreField.getText());
            statement.setString(2, apellidoPaternoField.getText());
            statement.setString(3, apellidoMaternoField.getText());
            statement.setDate(4, java.sql.Date.valueOf(fechaNacimientoPicker.getValue()));
            statement.setString(5, paisField.getText());
            statement.setString(6, ciudadField.getText());
            statement.setString(7, emailField.getText());
            statement.setString(8, ladaField.getText());
            statement.setString(9, telefonoField.getText());
            statement.setString(10, rfcField.getText());
            statement.setString(11, curpField.getText());
            statement.setString(12, profesionField.getText());
            statement.setString(13, departamentoChoiceBox.getValue()); // Actualiza departamento según el valor
            statement.setString(14, puestoChoiceBox.getValue()); // Actualiza jerarquía según el valor
            statement.setInt(15, empleadoId);
            statement.executeUpdate();

            // Cerrar la ventana después de confirmar
            cerrarVentana();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para cancelar la edición y cerrar la ventana
    @FXML
    private void Cancelar() {
        cerrarVentana();
    }

    // Método para cerrar la ventana
    private void cerrarVentana() {
        Stage stage = (Stage) CancelarButton.getScene().getWindow();
        stage.close();
    }
}
