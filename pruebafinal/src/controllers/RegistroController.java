package controllers;

import Lector.EnrollmentFormController;
import com.digitalpersona.onetouch.DPFPTemplate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private ImageView fingerprintImageView;


    @FXML
    private ChoiceBox<String> departamentoChoiceBox;
    @FXML
    private ChoiceBox<String> puestoChoiceBox;

    private DPFPTemplate template; // Variable para almacenar el template de la huella digital
    private byte[] fingerprintImageBytes; // Variable para almacenar la imagen de la huella en bytes

    @FXML
    private void initialize() {
        cargarDepartamentos();
        cargarPuestos();
    }

    public void setTemplate(DPFPTemplate template) {
        this.template = template;
    }

    public void setFingerprintImageBytes(byte[] fingerprintImageBytes) {
        this.fingerprintImageBytes = fingerprintImageBytes;
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
    private void enviarDatos() {
        // Validar si la huella digital ha sido ingresada
        if (template == null) {
            mostrarError("No se ha ingresado la huella digital. Por favor, regístrela antes de continuar.");
            return;
        }

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

        if (!validarCampos()) {
            return; // No se enviarán datos si la validación falla
        }

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

            // Insertar los datos del empleado sin la huella aún
            String empleadoSql = "INSERT INTO empleados (nombres, apellido_materno, apellido_paterno, fecha_nacimiento, pais, ciudad, correo_electronico, lada, telefono, rfc, curp, profesion, departamento_id, jerarquia_id, estatus_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
            PreparedStatement empleadoStatement = connection.prepareStatement(empleadoSql, Statement.RETURN_GENERATED_KEYS);
            empleadoStatement.setString(1, nombre);
            empleadoStatement.setString(2, apellidoMaterno);
            empleadoStatement.setString(3, apellidoPaterno);
            empleadoStatement.setDate(4, fechaNacimiento);
            empleadoStatement.setString(5, pais);
            empleadoStatement.setString(6, ciudad);
            empleadoStatement.setString(7, email);
            empleadoStatement.setString(8, lada);
            empleadoStatement.setString(9, telefono);
            empleadoStatement.setString(10, rfc);
            empleadoStatement.setString(11, curp);
            empleadoStatement.setString(12, profesion);
            empleadoStatement.setInt(13, departamentoId);
            empleadoStatement.setInt(14, jerarquiaId);

            int rowsAffected = empleadoStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se pudo insertar el empleado, no se generaron filas.");
            }

            // Obtener el ID del empleado recién insertado
            ResultSet generatedKeys = empleadoStatement.getGeneratedKeys();
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

                    String huellaSql = "INSERT INTO huellas (empleado_id, huella, huella_imagen) VALUES (?, ?, ?)";
                    PreparedStatement huellaStatement = connection.prepareStatement(huellaSql, Statement.RETURN_GENERATED_KEYS);
                    huellaStatement.setInt(1, empleadoId);
                    huellaStatement.setBytes(2, serializedTemplate);
                    huellaStatement.setBytes(3, fingerprintImageBytes);
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
                String updateEmpleadoHuellaSQL = "UPDATE empleados SET huella = ? WHERE id = ?";
                PreparedStatement updateEmpleadoHuellaStmt = connection.prepareStatement(updateEmpleadoHuellaSQL);
                updateEmpleadoHuellaStmt.setInt(1, huellaId);
                updateEmpleadoHuellaStmt.setInt(2, empleadoId);
                updateEmpleadoHuellaStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean validarCampos() {
        // Validación de Lada
        String lada = ladaField.getText();
        if (!lada.matches("\\d{2,3}")) {
            mostrarError("Lada inválida. Debe tener entre 2 a 3 dígitos.");
            return false;
        }

        // Validación de Teléfono
        String telefono = telefonoField.getText();
        if (!telefono.matches("\\d{7,8}")) {
            mostrarError("Teléfono inválido. Debe tener 7 u 8 dígitos.");
            return false;
        }

        // Validación de RFC
        String rfc = rfcField.getText();
        if (!rfc.matches("[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}")) {
            mostrarError("RFC inválido. Debe cumplir con el formato estándar de 4 letras y 9 dígitos.");
            return false;
        }

        // Validación de CURP
        String curp = curpField.getText();
        if (!curp.matches("[A-Z][AEIOU][A-Z]{2}\\d{6}[HM][A-Z]{5}[A-Z0-9]{2}")) {
            mostrarError("CURP inválido. Debe cumplir con el formato estándar de 18 caracteres alfanuméricos.");
            return false;
        }

        // Validación de correo electrónico
        String email = emailField.getText();
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        Matcher matcher = pattern.matcher(email);
        if (!matcher.find()) {
            mostrarError("Correo electrónico inválido. Ingrese un correo válido.");
            return false;
        }

        return true; // Validaciones exitosas
    }



    @FXML
    private void rellenarCampos() {
        nombreField.setText("Juan");
        apellidoMaternoField.setText("Pérez");
        apellidoPaternoField.setText("García");
        fechaNacimientoPicker.setValue(LocalDate.of(1990, 1, 1));
        emailField.setText("juan.perez@example.com");
        ladaField.setText("55");
        telefonoField.setText("1234567");
        rfcField.setText("PEGA900101ABC");
        curpField.setText("PEGA901101HMCRRL00");
        profesionField.setText("Ingeniero");
        departamentoChoiceBox.getSelectionModel().selectFirst();
        puestoChoiceBox.getSelectionModel().select(2);
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
            controller.setRegistroController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Enrollment Form");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void updateFingerprintImage(javafx.scene.image.Image image) {
        fingerprintImageView.setImage(image);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}