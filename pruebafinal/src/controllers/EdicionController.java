package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EdicionController {

    @FXML
    private TextField nombreField, apellidoPaternoField, apellidoMaternoField, paisField, ciudadField,
            ladaField, telefonoField, emailField, rfcField, curpField, profesionField;

    @FXML
    private DatePicker fechaNacimientoPicker;

    @FXML
    private ChoiceBox<String> departamentoChoiceBox, puestoChoiceBox, estatusChoiceBox;

    private final Map<String, Integer> departamentoMap = new HashMap<>();
    private final Map<String, Integer> puestoMap = new HashMap<>();
    private final Map<String, Integer> estatusMap = new HashMap<>();

    @FXML
    public void initialize() {
        cargarDepartamentos();
        cargarPuestos();
        cargarEstatus();
    }

    private void cargarDepartamentos() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombre FROM departamentos";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                departamentoChoiceBox.getItems().add(nombre);
                departamentoMap.put(nombre, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarPuestos() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombre FROM jerarquias";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                puestoChoiceBox.getItems().add(nombre);
                puestoMap.put(nombre, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarEstatus() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombre FROM estatus_empleado";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                estatusChoiceBox.getItems().add(nombre);
                estatusMap.put(nombre, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para establecer los valores actuales en los campos
    public void cargarDatosEmpleado(int empleadoId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM empleados WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, empleadoId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                nombreField.setText(resultSet.getString("nombres"));
                apellidoPaternoField.setText(resultSet.getString("apellido_paterno"));
                apellidoMaternoField.setText(resultSet.getString("apellido_materno"));
                paisField.setText(resultSet.getString("pais"));
                ciudadField.setText(resultSet.getString("ciudad"));
                ladaField.setText(resultSet.getString("lada"));
                telefonoField.setText(resultSet.getString("telefono"));
                emailField.setText(resultSet.getString("correo_electronico"));
                rfcField.setText(resultSet.getString("rfc"));
                curpField.setText(resultSet.getString("curp"));
                profesionField.setText(resultSet.getString("profesion"));
                fechaNacimientoPicker.setValue(resultSet.getDate("fecha_nacimiento").toLocalDate());

                int departamentoId = resultSet.getInt("departamento_id");
                departamentoChoiceBox.setValue(getKeyByValue(departamentoMap, departamentoId));

                int puestoId = resultSet.getInt("jerarquia_id");
                puestoChoiceBox.setValue(getKeyByValue(puestoMap, puestoId));

                int estatusId = resultSet.getInt("estatus_id");
                estatusChoiceBox.setValue(getKeyByValue(estatusMap, estatusId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getKeyByValue(Map<String, Integer> map, int value) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
        }
        return null;
    }
}
