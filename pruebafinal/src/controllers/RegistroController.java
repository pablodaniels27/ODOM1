package controllers;

import DAO.BaseDAO;
import Lector.EnrollmentFormController;
import Usuarios.Supervisor;
import Usuarios.Usuario;
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
import java.util.List;
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

    private Usuario usuarioAutenticado;



    @FXML
    private void initialize() {
        cargarDepartamentos();
        cargarPuestos();
    }

    // Este método se encargará de bloquear o permitir acceso a los campos según el tipo de usuario
    public void setUsuarioAutenticado(Usuario usuario) {
        this.usuarioAutenticado = usuario;

        if (usuarioAutenticado instanceof Supervisor) {
            Supervisor supervisor = (Supervisor) usuarioAutenticado;

            // Cargar solo el departamento del supervisor y bloquear el campo
            departamentoChoiceBox.getItems().clear();
            departamentoChoiceBox.getItems().add(supervisor.getDepartamentoNombre()); // Suponiendo que tienes un método getDepartamentoNombre()
            departamentoChoiceBox.setValue(supervisor.getDepartamentoNombre());
            departamentoChoiceBox.setDisable(true); // Bloquear para que no se pueda modificar

            // Cargar solo la opción "Empleado" en el puesto y bloquear el campo
            puestoChoiceBox.getItems().clear();
            puestoChoiceBox.getItems().add("Empleado");
            puestoChoiceBox.setValue("Empleado");
            puestoChoiceBox.setDisable(true); // Bloquear para que no se pueda modificar
        } else {
            // Si es un líder, cargar todos los departamentos y permitir la selección
            cargarDepartamentos();

            // Para puestos, solo cargar "Empleado" y "Supervisor"
            puestoChoiceBox.getItems().clear();
            puestoChoiceBox.getItems().addAll("Empleado", "Supervisor");
        }
    }

    public void setTemplate(DPFPTemplate template) {
        this.template = template;
    }

    public void setFingerprintImageBytes(byte[] fingerprintImageBytes) {
        this.fingerprintImageBytes = fingerprintImageBytes;
    }

    private void cargarDepartamentos() {
        departamentoChoiceBox.getItems().clear(); // Limpiar los ítems previamente cargados
        try {
            List<String> departamentos = BaseDAO.obtenerDepartamentos();
            departamentoChoiceBox.getItems().addAll(departamentos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void cargarPuestos() {
        puestoChoiceBox.getItems().clear(); // Limpiar los ítems previamente cargados
        try {
            List<String> puestos = BaseDAO.obtenerPuestos();
            puestoChoiceBox.getItems().addAll(puestos);
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

        try {
            // Obtener el ID del departamento y el puesto
            int departamentoId = BaseDAO.obtenerIdDepartamento(departamentoSeleccionado);
            int jerarquiaId = BaseDAO.obtenerIdPuesto(puestoSeleccionado);

            // Insertar empleado en la base de datos
            int empleadoId = BaseDAO.insertarEmpleado(nombre, apellidoMaterno, apellidoPaterno, fechaNacimiento, pais, ciudad, email, lada, telefono, rfc, curp, profesion, departamentoId, jerarquiaId);
            if (empleadoId == -1) {
                throw new SQLException("No se pudo insertar el empleado, no se generaron filas.");
            }

            // Insertar la huella en la base de datos
            if (template != null) {
                byte[] serializedTemplate = BaseDAO.serializarTemplate(template);
                if (serializedTemplate != null) {
                    int huellaId = BaseDAO.insertarHuella(empleadoId, serializedTemplate, fingerprintImageBytes);
                    if (huellaId != -1) {
                        BaseDAO.actualizarHuellaEmpleado(empleadoId, huellaId);
                    }
                }
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