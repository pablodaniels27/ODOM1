package controllers;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class EdicionController {

    @FXML
    private TextField nombreField, apellidoPaternoField, apellidoMaternoField, paisField, ciudadField,
            ladaField, telefonoField, emailField, rfcField, curpField, profesionField;

    @FXML
    private DatePicker fechaNacimientoPicker;

    @FXML
    private ChoiceBox<String> departamentoChoiceBox, puestoChoiceBox, estatusChoiceBox;

    @FXML
    private Button deshacerButton, guardarButton;

    @FXML
    private ImageView fingerprintImageView;

    private final Map<String, Integer> departamentoMap = new HashMap<>();
    private final Map<String, Integer> puestoMap = new HashMap<>();
    private final Map<String, Integer> estatusMap = new HashMap<>();

    // Mapa para almacenar los valores originales de los campos
    private final Map<String, Object> datosOriginales = new HashMap<>();
    private int empleadoId;

    @FXML
    public void initialize() {
        cargarDepartamentos();
        cargarPuestos();
        cargarEstatus();

        // Vincular los botones a sus métodos
        deshacerButton.setOnAction(event -> deshacerCambios());
        guardarButton.setOnAction(event -> guardarCambios());
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
        this.empleadoId = empleadoId;

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

                // Cargar la huella y mostrarla
                cargarHuella(empleadoId);

                // Guardar los valores originales
                guardarDatosOriginales();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarHuella(int empleadoId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT huella_imagen FROM huellas WHERE empleado_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, empleadoId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                byte[] imageBytes = resultSet.getBytes("huella_imagen");
                if (imageBytes != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    BufferedImage bufferedImage = ImageIO.read(bis);
                    Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                    fingerprintImageView.setImage(image);
                }
            }
        } catch (SQLException | IOException e) {
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

    // Método para guardar los datos originales
    private void guardarDatosOriginales() {
        datosOriginales.put("nombre", nombreField.getText());
        datosOriginales.put("apellidoPaterno", apellidoPaternoField.getText());
        datosOriginales.put("apellidoMaterno", apellidoMaternoField.getText());
        datosOriginales.put("pais", paisField.getText());
        datosOriginales.put("ciudad", ciudadField.getText());
        datosOriginales.put("lada", ladaField.getText());
        datosOriginales.put("telefono", telefonoField.getText());
        datosOriginales.put("email", emailField.getText());
        datosOriginales.put("rfc", rfcField.getText());
        datosOriginales.put("curp", curpField.getText());
        datosOriginales.put("profesion", profesionField.getText());
        datosOriginales.put("fechaNacimiento", fechaNacimientoPicker.getValue());
        datosOriginales.put("departamento", departamentoChoiceBox.getValue());
        datosOriginales.put("puesto", puestoChoiceBox.getValue());
        datosOriginales.put("estatus", estatusChoiceBox.getValue());
    }

    // Método para deshacer cambios y restaurar los valores originales
    @FXML
    private void deshacerCambios() {
        nombreField.setText((String) datosOriginales.get("nombre"));
        apellidoPaternoField.setText((String) datosOriginales.get("apellidoPaterno"));
        apellidoMaternoField.setText((String) datosOriginales.get("apellidoMaterno"));
        paisField.setText((String) datosOriginales.get("pais"));
        ciudadField.setText((String) datosOriginales.get("ciudad"));
        ladaField.setText((String) datosOriginales.get("lada"));
        telefonoField.setText((String) datosOriginales.get("telefono"));
        emailField.setText((String) datosOriginales.get("email"));
        rfcField.setText((String) datosOriginales.get("rfc"));
        curpField.setText((String) datosOriginales.get("curp"));
        profesionField.setText((String) datosOriginales.get("profesion"));
        fechaNacimientoPicker.setValue((LocalDate) datosOriginales.get("fechaNacimiento"));
        departamentoChoiceBox.setValue((String) datosOriginales.get("departamento"));
        puestoChoiceBox.setValue((String) datosOriginales.get("puesto"));
        estatusChoiceBox.setValue((String) datosOriginales.get("estatus"));
    }

    // Método para validar los campos antes de guardar los cambios
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

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para guardar los cambios en la base de datos
    @FXML
    private void guardarCambios() {
        // Validar los campos antes de guardar
        if (!validarCampos()) {
            return; // No se guardarán los cambios si la validación falla
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE empleados SET nombres = ?, apellido_paterno = ?, apellido_materno = ?, pais = ?, ciudad = ?, " +
                    "lada = ?, telefono = ?, correo_electronico = ?, rfc = ?, curp = ?, profesion = ?, fecha_nacimiento = ?, " +
                    "departamento_id = ?, jerarquia_id = ?, estatus_id = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nombreField.getText());
            statement.setString(2, apellidoPaternoField.getText());
            statement.setString(3, apellidoMaternoField.getText());
            statement.setString(4, paisField.getText());
            statement.setString(5, ciudadField.getText());
            statement.setString(6, ladaField.getText());
            statement.setString(7, telefonoField.getText());
            statement.setString(8, emailField.getText());
            statement.setString(9, rfcField.getText());
            statement.setString(10, curpField.getText());
            statement.setString(11, profesionField.getText());
            statement.setDate(12, java.sql.Date.valueOf(fechaNacimientoPicker.getValue()));
            statement.setInt(13, departamentoMap.get(departamentoChoiceBox.getValue()));
            statement.setInt(14, puestoMap.get(puestoChoiceBox.getValue()));
            statement.setInt(15, estatusMap.get(estatusChoiceBox.getValue()));
            statement.setInt(16, empleadoId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Los cambios se han guardado correctamente.");
            } else {
                System.out.println("No se pudo actualizar el registro.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
