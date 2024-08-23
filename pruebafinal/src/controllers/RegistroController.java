package controllers;

import Lector.EnrollmentFormController;
import com.digitalpersona.onetouch.DPFPTemplate;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

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
    private ImageView fingerprintImageView;

    private DPFPTemplate template; // Variable para almacenar el template de la huella digital

    @FXML
    private void initialize() {
        cargarDepartamentos();
        cargarPuestos();
    }

    public void setTemplate(DPFPTemplate template) {
        this.template = template;
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
            String query = "SELECT nombre FROM jerarquias";
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
    public void updateFingerprintImage(javafx.scene.image.Image image) {
        fingerprintImageView.setImage(image);
    }

    @FXML
    private void enviarDatos() {
        String nombre = nombreField.getText();
        String apellidoMaterno = apellidoMaternoField.getText();
        String apellidoPaterno = apellidoPaternoField.getText();
        Date fechaNacimiento = Date.valueOf(fechaNacimientoPicker.getValue());
        String pais = paisField.getText();
        String ciudad = ciudadField.getText();
        String email = emailField.getText();
        String lada = ladaField.getText();
        String telefono = telefonoField.getText();
        String rfc = rfcField.getText();
        String curp = curpField.getText();
        String profesion = profesionField.getText();

        String departamentoSeleccionado = departamentoChoiceBox.getSelectionModel().getSelectedItem();
        String puestoSeleccionado = puestoChoiceBox.getSelectionModel().getSelectedItem();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Obtener el ID del departamento
            String departamentoQuery = "SELECT id FROM departamentos WHERE nombre = ?";
            PreparedStatement departamentoStatement = connection.prepareStatement(departamentoQuery);
            departamentoStatement.setString(1, departamentoSeleccionado);
            ResultSet departamentoResult = departamentoStatement.executeQuery();
            int departamentoId = 0;
            if (departamentoResult.next()) {
                departamentoId = departamentoResult.getInt("id");
            }

            // Obtener el ID del puesto
            String puestoQuery = "SELECT id FROM jerarquias WHERE nombre = ?";
            PreparedStatement puestoStatement = connection.prepareStatement(puestoQuery);
            puestoStatement.setString(1, puestoSeleccionado);
            ResultSet puestoResult = puestoStatement.executeQuery();
            int jerarquiaId = 0;
            if (puestoResult.next()) {
                jerarquiaId = puestoResult.getInt("id");
            }

            // Insertar los datos del empleado sin huella aún
            String empleadoSql = "INSERT INTO empleados (nombres, apellido_materno, apellido_paterno, fecha_nacimiento, pais, ciudad, correo_electronico, lada, telefono, rfc, curp, profesion, departamento_id, jerarquia_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(empleadoSql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, nombre);
            statement.setString(2, apellidoMaterno);
            statement.setString(3, apellidoPaterno);
            statement.setDate(4, fechaNacimiento);
            statement.setString(5, pais);
            statement.setString(6, ciudad);
            statement.setString(7, email);
            statement.setString(8, lada);
            statement.setString(9, telefono);
            statement.setString(10, rfc);
            statement.setString(11, curp);
            statement.setString(12, profesion);
            statement.setInt(13, departamentoId);
            statement.setInt(14, jerarquiaId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se pudo insertar el empleado, no se generaron filas.");
            }

            // Obtener el ID del empleado recién insertado
            ResultSet generatedKeys = statement.getGeneratedKeys();
            int empleadoId = 0;
            if (generatedKeys.next()) {
                empleadoId = generatedKeys.getInt(1);
            }

            // Insertar la huella en la tabla 'huellas'
            int huellaId = 0;
            if (template != null) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(bos)) {

                    oos.writeObject(template.serialize()); // Serializar el template
                    oos.flush();
                    byte[] serializedTemplate = bos.toByteArray();

                    String huellaSql = "INSERT INTO huellas (empleado_id, huella) VALUES (?, ?)";
                    PreparedStatement huellaStatement = connection.prepareStatement(huellaSql, Statement.RETURN_GENERATED_KEYS);
                    huellaStatement.setInt(1, empleadoId);
                    huellaStatement.setBytes(2, serializedTemplate);
                    huellaStatement.executeUpdate();

                    ResultSet huellaGeneratedKeys = huellaStatement.getGeneratedKeys();
                    if (huellaGeneratedKeys.next()) {
                        huellaId = huellaGeneratedKeys.getInt(1); // Obtener el ID generado de la huella
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;  // Early return on serialization failure
                }

                // Actualizar el campo huella_id en la tabla empleados
                String updateEmpleadoHuellaSQL = "UPDATE empleados SET huella_id = ? WHERE id = ?";
                PreparedStatement updateEmpleadoHuellaStmt = connection.prepareStatement(updateEmpleadoHuellaSQL);
                updateEmpleadoHuellaStmt.setInt(1, huellaId);
                updateEmpleadoHuellaStmt.setInt(2, empleadoId);
                updateEmpleadoHuellaStmt.executeUpdate();
            }

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

            EnrollmentFormController controller = loader.getController();
            controller.setRegistroController(this); // Pasar el controlador de Registro al de Enrollment

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Enrollment Form");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
