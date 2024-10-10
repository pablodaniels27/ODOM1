package controllers;

import DAO.BaseDAO;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPTemplate;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
        try {
            // Obtener la lista de departamentos desde el DAO
            Map<String, Integer> departamentos = BaseDAO.obtenerDepartamentosid();

            // Agregar los departamentos al ChoiceBox y al mapa
            for (Map.Entry<String, Integer> entry : departamentos.entrySet()) {
                String nombre = entry.getKey();
                int id = entry.getValue();
                departamentoChoiceBox.getItems().add(nombre);
                departamentoMap.put(nombre, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void cargarPuestos() {
        try {
            // Obtener la lista de puestos desde el DAO
            Map<String, Integer> puestos = BaseDAO.obtenerPuestosid();

            // Agregar los puestos al ChoiceBox y al mapa
            for (Map.Entry<String, Integer> entry : puestos.entrySet()) {
                String nombre = entry.getKey();
                int id = entry.getValue();
                puestoChoiceBox.getItems().add(nombre);
                puestoMap.put(nombre, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void cargarEstatus() {
        try {
            // Obtener la lista de estatus desde el DAO
            Map<String, Integer> estatus = BaseDAO.obtenerEstatusEmpleados();

            // Agregar los estatus al ChoiceBox y al mapa
            for (Map.Entry<String, Integer> entry : estatus.entrySet()) {
                String nombre = entry.getKey();
                int id = entry.getValue();
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

        try {
            // Obtener los datos del empleado desde el DAO
            Map<String, Object> empleadoData = BaseDAO.obtenerDatosEmpleado(empleadoId);

            if (empleadoData != null) {
                nombreField.setText((String) empleadoData.get("nombres"));
                apellidoPaternoField.setText((String) empleadoData.get("apellido_paterno"));
                apellidoMaternoField.setText((String) empleadoData.get("apellido_materno"));
                paisField.setText((String) empleadoData.get("pais"));
                ciudadField.setText((String) empleadoData.get("ciudad"));
                ladaField.setText((String) empleadoData.get("lada"));
                telefonoField.setText((String) empleadoData.get("telefono"));
                emailField.setText((String) empleadoData.get("correo_electronico"));
                rfcField.setText((String) empleadoData.get("rfc"));
                curpField.setText((String) empleadoData.get("curp"));
                profesionField.setText((String) empleadoData.get("profesion"));
                fechaNacimientoPicker.setValue(((java.sql.Date) empleadoData.get("fecha_nacimiento")).toLocalDate());

                int departamentoId = (int) empleadoData.get("departamento_id");
                departamentoChoiceBox.setValue(getKeyByValue(departamentoMap, departamentoId));

                int puestoId = (int) empleadoData.get("jerarquia_id");
                puestoChoiceBox.setValue(getKeyByValue(puestoMap, puestoId));

                int estatusId = (int) empleadoData.get("estatus_id");
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
        try {
            byte[] imageBytes = BaseDAO.obtenerHuellaImagen(empleadoId);
            if (imageBytes != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage bufferedImage = ImageIO.read(bis);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                fingerprintImageView.setImage(image);
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
        // Validar si hay cambios antes de guardar
        if (!hayCambios()) {
            Alert sinCambiosAlert = new Alert(Alert.AlertType.INFORMATION);
            sinCambiosAlert.setTitle("Sin cambios detectados");
            sinCambiosAlert.setHeaderText(null);
            sinCambiosAlert.setContentText("No se han detectado cambios en el formulario.");
            sinCambiosAlert.showAndWait();
            return;
        }

        // Mostrar confirmación antes de guardar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cambios");
        confirmacion.setHeaderText("¿Desea guardar los cambios?");
        confirmacion.setContentText("Haga clic en 'Confirmar cambios' para guardar o 'Cancelar' para descartar.");

        // Botones de confirmación
        ButtonType botonConfirmar = new ButtonType("Confirmar cambios");
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(botonConfirmar, botonCancelar);

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == botonConfirmar) {
                // Validar los campos antes de guardar
                if (!validarCampos()) {
                    return; // No se guardarán los cambios si la validación falla
                }

                try {
                    // Crear un mapa con los datos actualizados del empleado
                    Map<String, Object> empleadoData = new HashMap<>();
                    empleadoData.put("nombres", nombreField.getText());
                    empleadoData.put("apellido_paterno", apellidoPaternoField.getText());
                    empleadoData.put("apellido_materno", apellidoMaternoField.getText());
                    empleadoData.put("pais", paisField.getText());
                    empleadoData.put("ciudad", ciudadField.getText());
                    empleadoData.put("lada", ladaField.getText());
                    empleadoData.put("telefono", telefonoField.getText());
                    empleadoData.put("correo_electronico", emailField.getText());
                    empleadoData.put("rfc", rfcField.getText());
                    empleadoData.put("curp", curpField.getText());
                    empleadoData.put("profesion", profesionField.getText());
                    empleadoData.put("fecha_nacimiento", java.sql.Date.valueOf(fechaNacimientoPicker.getValue()));
                    empleadoData.put("departamento_id", departamentoMap.get(departamentoChoiceBox.getValue()));
                    empleadoData.put("jerarquia_id", puestoMap.get(puestoChoiceBox.getValue()));
                    empleadoData.put("estatus_id", estatusMap.get(estatusChoiceBox.getValue()));
                    empleadoData.put("id", empleadoId);

                    // Llamar al DAO para guardar los cambios
                    boolean success = BaseDAO.actualizarEmpleado(empleadoData);
                    if (success) {
                        System.out.println("Los cambios se han guardado correctamente.");

                        // Mostrar alerta de éxito
                        Alert exitoAlert = new Alert(Alert.AlertType.INFORMATION);
                        exitoAlert.setTitle("Éxito");
                        exitoAlert.setHeaderText(null);
                        exitoAlert.setContentText("Los cambios fueron guardados exitosamente.");
                        exitoAlert.showAndWait();

                    } else {
                        System.out.println("No se pudo actualizar el registro.");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Método para verificar si hay cambios entre los valores actuales y los originales
    private boolean hayCambios() {
        return !nombreField.getText().equals(datosOriginales.get("nombre")) ||
                !apellidoPaternoField.getText().equals(datosOriginales.get("apellidoPaterno")) ||
                !apellidoMaternoField.getText().equals(datosOriginales.get("apellidoMaterno")) ||
                !paisField.getText().equals(datosOriginales.get("pais")) ||
                !ciudadField.getText().equals(datosOriginales.get("ciudad")) ||
                !ladaField.getText().equals(datosOriginales.get("lada")) ||
                !telefonoField.getText().equals(datosOriginales.get("telefono")) ||
                !emailField.getText().equals(datosOriginales.get("email")) ||
                !rfcField.getText().equals(datosOriginales.get("rfc")) ||
                !curpField.getText().equals(datosOriginales.get("curp")) ||
                !profesionField.getText().equals(datosOriginales.get("profesion")) ||
                !fechaNacimientoPicker.getValue().equals(datosOriginales.get("fechaNacimiento")) ||
                !departamentoChoiceBox.getValue().equals(datosOriginales.get("departamento")) ||
                !puestoChoiceBox.getValue().equals(datosOriginales.get("puesto")) ||
                !estatusChoiceBox.getValue().equals(datosOriginales.get("estatus"));
    }
}