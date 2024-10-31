package controllers;

import DAO.BaseDAO;
import Usuarios.Lider;
import Usuarios.Supervisor;
import Usuarios.Usuario;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private Button deshacerButton, guardarButton, regresarButton;

    @FXML
    private ImageView fingerprintImageView;

    private final Map<String, Integer> departamentoMap = new HashMap<>();
    private final Map<String, Integer> puestoMap = new HashMap<>();
    private final Map<String, Integer> estatusMap = new HashMap<>();

    // Mapa para almacenar los valores originales de los campos
    private final Map<String, Object> datosOriginales = new HashMap<>();
    private int empleadoId;

    private Usuario usuarioAutenticado;

    private int visitas = 0;

    public void setUsuarioAutenticado(Usuario usuario) {
        this.usuarioAutenticado = usuario;

        if (usuarioAutenticado instanceof Supervisor) {
            Supervisor supervisor = (Supervisor) usuarioAutenticado;

            // Cargar solo el departamento del supervisor y bloquear el campo
            departamentoChoiceBox.getItems().clear();
            departamentoChoiceBox.getItems().add(supervisor.getDepartamentoNombre());
            departamentoChoiceBox.setValue(supervisor.getDepartamentoNombre());
            departamentoChoiceBox.setDisable(true); // Bloquear para que no se pueda modificar

            // Cargar solo la opción "Empleado" en el puesto y bloquear el campo
            puestoChoiceBox.getItems().clear();
            puestoChoiceBox.getItems().add("Empleado");
            puestoChoiceBox.setValue("Empleado");
            puestoChoiceBox.setDisable(true); // Bloquear para que no se pueda modificar

            // Verificar si el supervisor tiene permiso para editar el estatus
            if (!supervisor.tienePermiso("Cambiar estatus del empleado")) {
                estatusChoiceBox.setDisable(true); // Bloquear el campo si no tiene permiso
            }

        } else if (usuarioAutenticado instanceof Lider) {
            // Si es un líder, cargar todos los departamentos y permitir la selección
            cargarDepartamentos();

            // Para puestos, solo cargar "Empleado" y "Supervisor"
            puestoChoiceBox.getItems().clear();
            puestoChoiceBox.getItems().addAll("Empleado", "Supervisor");

            // Un líder siempre puede modificar el estatus
            estatusChoiceBox.setDisable(false); // Permitir cambios en el estatus
        }
    }



    @FXML
    public void initialize() {
        cargarDepartamentos();
        cargarPuestos();
        cargarEstatus();

        // Vincular los botones a sus métodos
        deshacerButton.setOnAction(event -> deshacerCambios());
        guardarButton.setOnAction(event -> guardarCambios());
        regresarButton.setOnAction(event -> regresarARegistroSucursal() );
    }

    private void cargarDepartamentos() {
        try {
            // Limpiar el ChoiceBox antes de agregar nuevos elementos
            departamentoChoiceBox.getItems().clear();  // Limpia el ChoiceBox para evitar duplicados

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
            // Limpiar el ChoiceBox y el mapa antes de agregar nuevos elementos
            puestoChoiceBox.getItems().clear();
            puestoMap.clear();  // Limpiar el mapa para evitar residuos de datos anteriores

            // Obtener la lista de puestos desde el DAO
            Map<String, Integer> puestos = BaseDAO.obtenerPuestosid();

            // Verificar si los puestos están siendo obtenidos correctamente
            if (puestos.isEmpty()) {
                System.out.println("No se encontraron puestos en la base de datos.");
            } else {
                // Agregar los puestos al ChoiceBox y al mapa
                for (Map.Entry<String, Integer> entry : puestos.entrySet()) {
                    String nombre = entry.getKey();
                    int id = entry.getValue();
                    puestoChoiceBox.getItems().add(nombre);  // Agregar el nombre del puesto al ChoiceBox
                    puestoMap.put(nombre, id);  // Guardar el nombre y el ID en el mapa
                }
                System.out.println("Puestos cargados correctamente: " + puestoChoiceBox.getItems());
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
    // Método para establecer los valores actuales en los campos
    // Método para cargar datos del empleado
    public void cargarDatosEmpleado(int empleadoId) {

        this.empleadoId = empleadoId;

        try {
            // Obtener los datos del empleado desde el DAO
            Map<String, Object> empleadoData = BaseDAO.obtenerDatosEmpleado(empleadoId);

            if (empleadoData != null) {
                // Establecer valores en los campos
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


                if (visitas == 0 || usuarioAutenticado instanceof Lider ){

                    // Cargar los departamentos y seleccionarlos
                    cargarDepartamentos();
                    Platform.runLater(() -> {
                        int departamentoId = (int) empleadoData.get("departamento_id");
                        String departamentoNombre = getKeyByValue(departamentoMap, departamentoId);
                        if (departamentoNombre != null) {
                            departamentoChoiceBox.setValue(departamentoNombre);
                            System.out.println("Departamento cargado: " + departamentoNombre);
                        } else {
                            System.out.println("Error al cargar el departamento: ID no encontrado.");
                        }
                    });

                    // Cargar los puestos y seleccionarlos
                    cargarPuestos();
                    Platform.runLater(() -> {
                        int puestoId = (int) empleadoData.get("jerarquia_id");
                        String puestoNombre = getKeyByValue(puestoMap, puestoId);
                        if (puestoNombre != null) {
                            puestoChoiceBox.setValue(puestoNombre);
                            System.out.println("Puesto cargado: " + puestoNombre);
                        } else {
                            System.out.println("Error al cargar el puesto: ID no encontrado.");
                        }


                    });

                }


                // Cargar el estatus seleccionado
                int estatusId = (int) empleadoData.get("estatus_id");
                estatusChoiceBox.setValue(getKeyByValue(estatusMap, estatusId));

                // Cargar la huella y mostrarla
                cargarHuella(empleadoId);

                visitas++;
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









    // Método para deshacer cambios y restaurar los valores originales
// Método para deshacer cambios y restaurar los valores originales
    @FXML
    private void deshacerCambios() {
        cargarDatosEmpleado(empleadoId);
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

        ButtonType botonConfirmar = new ButtonType("Confirmar cambios");
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(botonConfirmar, botonCancelar);

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == botonConfirmar) {
                if (!validarCampos()) {
                    return;
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
                    int nuevoDepartamentoId = departamentoMap.get(departamentoChoiceBox.getValue());
                    int nuevoEstatusId = estatusMap.get(estatusChoiceBox.getValue());
                    empleadoData.put("departamento_id", nuevoDepartamentoId);
                    empleadoData.put("jerarquia_id", puestoMap.get(puestoChoiceBox.getValue()));
                    empleadoData.put("estatus_id", nuevoEstatusId);
                    empleadoData.put("id", empleadoId);

                    Map<String, Object> datosAntiguos = BaseDAO.obtenerDatosEmpleado(empleadoId);

                    boolean success = BaseDAO.actualizarEmpleado(empleadoData);
                    if (success) {
                        System.out.println("Los cambios se han guardado correctamente.");

                        if (usuarioAutenticado instanceof Supervisor || usuarioAutenticado instanceof Lider) {
                            int userId = usuarioAutenticado.getId();

                            StringBuilder cambios = new StringBuilder();

                            // Registrar cambios en cada campo
                            registrarCambioCampo(datosAntiguos, "nombres", empleadoData, "Nombre", cambios);
                            registrarCambioCampo(datosAntiguos, "apellido_paterno", empleadoData, "Apellido Paterno", cambios);
                            registrarCambioCampo(datosAntiguos, "apellido_materno", empleadoData, "Apellido Materno", cambios);
                            registrarCambioCampo(datosAntiguos, "pais", empleadoData, "País", cambios);
                            registrarCambioCampo(datosAntiguos, "ciudad", empleadoData, "Ciudad", cambios);
                            registrarCambioCampo(datosAntiguos, "lada", empleadoData, "Lada", cambios);
                            registrarCambioCampo(datosAntiguos, "telefono", empleadoData, "Teléfono", cambios);
                            registrarCambioCampo(datosAntiguos, "correo_electronico", empleadoData, "Correo Electrónico", cambios);
                            registrarCambioCampo(datosAntiguos, "rfc", empleadoData, "RFC", cambios);
                            registrarCambioCampo(datosAntiguos, "curp", empleadoData, "CURP", cambios);
                            registrarCambioCampo(datosAntiguos, "profesion", empleadoData, "Profesión", cambios);

                            // Registrar cambios en fecha de nacimiento
                            if (!datosAntiguos.get("fecha_nacimiento").equals(empleadoData.get("fecha_nacimiento"))) {
                                cambios.append("Fecha de nacimiento cambió de ")
                                        .append(datosAntiguos.get("fecha_nacimiento"))
                                        .append(" a ")
                                        .append(empleadoData.get("fecha_nacimiento"))
                                        .append(".\n");
                            }

                            // Registrar cambios en el departamento
                            int antiguoDepartamentoId = (int) datosAntiguos.get("departamento_id");
                            if (antiguoDepartamentoId != nuevoDepartamentoId) {
                                String nombreAntiguoDepartamento = BaseDAO.obtenerNombreDepartamento(antiguoDepartamentoId);
                                String nombreNuevoDepartamento = BaseDAO.obtenerNombreDepartamento(nuevoDepartamentoId);
                                cambios.append("Departamento cambió de ")
                                        .append(nombreAntiguoDepartamento).append(" a ")
                                        .append(nombreNuevoDepartamento).append(".\n");
                            }

                            // Registrar cambios en el estatus
                            int antiguoEstatusId = (int) datosAntiguos.get("estatus_id");
                            if (antiguoEstatusId != nuevoEstatusId) {
                                String nombreAntiguoEstatus = BaseDAO.obtenerNombreEstatus(antiguoEstatusId);
                                String nombreNuevoEstatus = BaseDAO.obtenerNombreEstatus(nuevoEstatusId);
                                cambios.append("Estatus cambió de ")
                                        .append(nombreAntiguoEstatus).append(" a ")
                                        .append(nombreNuevoEstatus).append(".\n");
                            }

                            // Registrar en el log
                            BaseDAO.registrarCambioLogCambios(userId, "Actualización de datos de empleado", empleadoId, cambios.toString());
                        }

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

    // Método auxiliar para registrar cambios en campos específicos
    private void registrarCambioCampo(Map<String, Object> datosAntiguos, String campo, Map<String, Object> datosNuevos, String nombreCampo, StringBuilder cambios) {
        Object valorAntiguo = datosAntiguos.get(campo);
        Object valorNuevo = datosNuevos.get(campo);
        if (valorAntiguo != null && valorNuevo != null && !valorAntiguo.equals(valorNuevo)) {
            cambios.append(nombreCampo).append(" cambió de ").append(valorAntiguo).append(" a ").append(valorNuevo).append(".\n");
        }
    }

    private boolean compararConDatosOriginales(Map<String, Object> datosOriginalesBD) {
        return !nombreField.getText().equals(datosOriginalesBD.get("nombres")) ||
                !apellidoPaternoField.getText().equals(datosOriginalesBD.get("apellido_paterno")) ||
                !apellidoMaternoField.getText().equals(datosOriginalesBD.get("apellido_materno")) ||
                !paisField.getText().equals(datosOriginalesBD.get("pais")) ||
                !ciudadField.getText().equals(datosOriginalesBD.get("ciudad")) ||
                !ladaField.getText().equals(datosOriginalesBD.get("lada")) ||
                !telefonoField.getText().equals(datosOriginalesBD.get("telefono")) ||
                !emailField.getText().equals(datosOriginalesBD.get("correo_electronico")) ||
                !rfcField.getText().equals(datosOriginalesBD.get("rfc")) ||
                !curpField.getText().equals(datosOriginalesBD.get("curp")) ||
                !profesionField.getText().equals(datosOriginalesBD.get("profesion")) ||
                !fechaNacimientoPicker.getValue().equals(((java.sql.Date) datosOriginalesBD.get("fecha_nacimiento")).toLocalDate()) ||
                !departamentoChoiceBox.getValue().equals(getKeyByValue(departamentoMap, (int) datosOriginalesBD.get("departamento_id"))) ||
                !puestoChoiceBox.getValue().equals(getKeyByValue(puestoMap, (int) datosOriginalesBD.get("jerarquia_id"))) ||
                !estatusChoiceBox.getValue().equals(getKeyByValue(estatusMap, (int) datosOriginalesBD.get("estatus_id")));
    }

    // Método para regresar a la vista de registro de sucursal
    @FXML
    private void regresarARegistroSucursal() {

        try {
            // Obtener los datos originales desde la base de datos
            Map<String, Object> datosOriginalesBD = BaseDAO.obtenerDatosEmpleado(empleadoId);

            // Comparar con los valores actuales en la vista y obtener los campos que han cambiado
            List<String> camposModificados = obtenerCamposModificados(datosOriginalesBD);

            if (!camposModificados.isEmpty()) {
                // Mostrar alerta de confirmación con los campos modificados
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Cambios sin guardar");
                confirmacion.setHeaderText("Hay cambios sin guardar.");
                confirmacion.setContentText("Los siguientes campos han sido modificados: " + String.join(", ", camposModificados) +
                        "\n\n¿Desea salir de todas formas?");

                ButtonType botonSalir = new ButtonType("Salir");
                ButtonType botonGuardar = new ButtonType("Guardar cambios");
                ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmacion.getButtonTypes().setAll(botonSalir, botonGuardar, botonCancelar);

                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == botonGuardar) {
                        guardarCambios();
                    } else if (response == botonSalir) {
                        navegarARegistroSucursal();
                    }
                    // Si se selecciona cancelar, no hacer nada
                });
            } else {
                // Si no hay cambios, proceder con la navegación
                navegarARegistroSucursal();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private List<String> obtenerCamposModificados(Map<String, Object> datosOriginalesBD) {
        List<String> camposModificados = new ArrayList<>();

        if (!nombreField.getText().equals(datosOriginalesBD.get("nombres"))) {
            camposModificados.add("Nombre");
        }
        if (!apellidoPaternoField.getText().equals(datosOriginalesBD.get("apellido_paterno"))) {
            camposModificados.add("Apellido Paterno");
        }
        if (!apellidoMaternoField.getText().equals(datosOriginalesBD.get("apellido_materno"))) {
            camposModificados.add("Apellido Materno");
        }
        if (!paisField.getText().equals(datosOriginalesBD.get("pais"))) {
            camposModificados.add("País");
        }
        if (!ciudadField.getText().equals(datosOriginalesBD.get("ciudad"))) {
            camposModificados.add("Ciudad");
        }
        if (!ladaField.getText().equals(datosOriginalesBD.get("lada"))) {
            camposModificados.add("Lada");
        }
        if (!telefonoField.getText().equals(datosOriginalesBD.get("telefono"))) {
            camposModificados.add("Teléfono");
        }
        if (!emailField.getText().equals(datosOriginalesBD.get("correo_electronico"))) {
            camposModificados.add("Correo Electrónico");
        }
        if (!rfcField.getText().equals(datosOriginalesBD.get("rfc"))) {
            camposModificados.add("RFC");
        }
        if (!curpField.getText().equals(datosOriginalesBD.get("curp"))) {
            camposModificados.add("CURP");
        }
        if (!profesionField.getText().equals(datosOriginalesBD.get("profesion"))) {
            camposModificados.add("Profesión");
        }
        if (!fechaNacimientoPicker.getValue().equals(((java.sql.Date) datosOriginalesBD.get("fecha_nacimiento")).toLocalDate())) {
            camposModificados.add("Fecha de Nacimiento");
        }
        if (!departamentoChoiceBox.getValue().equals(getKeyByValue(departamentoMap, (int) datosOriginalesBD.get("departamento_id")))) {
            camposModificados.add("Departamento");
        }
        if (!puestoChoiceBox.getValue().equals(getKeyByValue(puestoMap, (int) datosOriginalesBD.get("jerarquia_id")))) {
            camposModificados.add("Puesto");
        }
        if (!estatusChoiceBox.getValue().equals(getKeyByValue(estatusMap, (int) datosOriginalesBD.get("estatus_id")))) {
            camposModificados.add("Estatus");
        }

        return camposModificados;
    }



    private void navegarARegistroSucursal() {
        if (usuarioAutenticado instanceof Lider){
            try {
                // Obtén el StackPane principal desde la escena actual
                StackPane mainContent = (StackPane) regresarButton.getScene().lookup("#mainContent");

                // Carga la vista de RegistroSucursal
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegistroSucursalView.fxml"));
                Parent registroView = loader.load();

                // Obtén el controlador de RegistroSucursal
                RegistroSucursalController registroController = loader.getController();

                // Configura el usuario autenticado en el controlador de RegistroSucursal
                registroController.setUsuarioAutenticado(usuarioAutenticado);

                // Limpia el contenido actual y añade la nueva vista
                mainContent.getChildren().clear();
                mainContent.getChildren().add(registroView);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            try {
                // Obtén el StackPane principal desde la escena actual
                StackPane mainContent = (StackPane) regresarButton.getScene().lookup("#mainContent");

                // Carga la vista de RegistroSucursal
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegistroSucursalView2.fxml"));
                Parent registroView = loader.load();

                // Obtén el controlador de RegistroSucursal
                RegistroSucursalController2 registroController = loader.getController();

                // Configura el usuario autenticado en el controlador de RegistroSucursal
                registroController.setUsuarioAutenticado(usuarioAutenticado);

                // Limpia el contenido actual y añade la nueva vista
                mainContent.getChildren().clear();
                mainContent.getChildren().add(registroView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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