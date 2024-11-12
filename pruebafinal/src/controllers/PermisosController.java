package controllers;

import DAO.BaseDAO;
import DAO.UsuariosDAO;
import Usuarios.Permisos;
import Usuarios.Supervisor;
import Usuarios.Usuario;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class PermisosController {

    @FXML
    private Button regresarButton;
    private boolean cambiosSinGuardar = false;
    private UsuariosDAO usuariosDAO;
    private int supervisorId; // Añadido para almacenar el ID del supervisor


    @FXML
    private VBox permisosContainer;

    private Usuario usuarioAutenticado; // Declara usuarioAutenticado

    @FXML
    private Label supervisorLabel;

    @FXML
    private Button applyChangesButton;

    @FXML
    private Button undoChangesButton;

    private Map<String, CheckBox> permisosCheckBoxMap = new HashMap<>();
    private Map<String, Boolean> estadoInicialPermisos = new HashMap<>();
    private Map<String, String> permisosDependencias = new HashMap<>();

    // Método para inicializar el controlador
    public void initialize(UsuariosDAO usuariosDAO, int supervisorId,Usuario usuarioAutenticado ) {

        this.usuariosDAO = usuariosDAO; // Se asegura de que usuariosDAO esté inicializado
        this.usuarioAutenticado = usuarioAutenticado;
        this.supervisorId = supervisorId;

        inicializarDependenciasDePermisos();
        cargarPermisosDisponibles();
        cargarPermisosSupervisor(supervisorId);

        applyChangesButton.setDisable(false);
        undoChangesButton.setDisable(false);
    }

    private void inicializarDependenciasDePermisos() {
        permisosDependencias.put("Registrar más empleados", "Ver Registro");
        permisosDependencias.put("Editar empleados", "Ver Gestión de empleados");
        permisosDependencias.put("Editar huella", "Ver Gestión de empleados");
        permisosDependencias.put("Cambiar estatus del empleado", "Ver Gestión de empleados");
        permisosDependencias.put("Dar de baja empleados", "Ver Gestión de empleados");
        permisosDependencias.put("Editar tipo de asistencia", "Ver Monitoreo");
        permisosDependencias.put("Editar nota de asistencia", "Ver Monitoreo");
    }

    protected void cargarPermisosSupervisor(int supervisorId) {
        try {
            Supervisor supervisor = usuariosDAO.obtenerSupervisorConPermisosPorId(supervisorId);

            if (supervisor != null) {
                supervisorLabel.setText("Supervisor: " + usuariosDAO.obtenerNombreCompletoSupervisor(supervisorId));
                Set<Permisos> permisosDelSupervisor = supervisor.getPermisos();

                estadoInicialPermisos.clear();
                for (Map.Entry<String, CheckBox> entry : permisosCheckBoxMap.entrySet()) {
                    String permisoNombre = entry.getKey();
                    CheckBox checkBox = entry.getValue();

                    boolean tienePermiso = permisosDelSupervisor.stream()
                            .anyMatch(p -> p.getNombre().trim().equalsIgnoreCase(permisoNombre.trim()));

                    checkBox.setSelected(tienePermiso);
                    estadoInicialPermisos.put(permisoNombre, tienePermiso);

                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> cambiosSinGuardar = true);
                }
            } else {
                System.out.println("Supervisor no encontrado en la base de datos.");
                applyChangesButton.setDisable(true);
                undoChangesButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            applyChangesButton.setDisable(true);
            undoChangesButton.setDisable(true);
        }
    }

    @FXML
    private void regresarARegistroSucursal() {
        try {
            // Similar a EdicionController para navegar de vuelta a RegistroSucursal
            StackPane mainContent = (StackPane) regresarButton.getScene().lookup("#mainContent");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegistroSucursalView.fxml"));
            Parent registroView = loader.load();

            RegistroSucursalController registroController = loader.getController();
            registroController.setUsuarioAutenticado(usuarioAutenticado);  // Asegura que pase el usuario autenticado

            mainContent.getChildren().clear();
            mainContent.getChildren().add(registroView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void cargarPermisosDisponibles() {
        try {
            Set<Permisos> todosLosPermisos = usuariosDAO.obtenerTodosLosPermisos();

            permisosContainer.getChildren().clear();
            permisosCheckBoxMap.clear();

            List<Permisos> permisosOrdenados = todosLosPermisos.stream()
                    .sorted((p1, p2) -> {
                        if (p1.getNombre().startsWith("Ver") && !p2.getNombre().startsWith("Ver")) return -1;
                        if (!p1.getNombre().startsWith("Ver") && p2.getNombre().startsWith("Ver")) return 1;
                        return p1.getNombre().compareToIgnoreCase(p2.getNombre());
                    })
                    .toList();

            for (Permisos permiso : permisosOrdenados) {
                CheckBox checkBox = new CheckBox(permiso.getNombre());
                checkBox.setSelected(false);

                permisosContainer.getChildren().add(checkBox);
                permisosCheckBoxMap.put(permiso.getNombre(), checkBox);

                if (permiso.getNombre().startsWith("Ver")) {
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        habilitarPermisosDependientes(permiso.getNombre(), newValue);
                        if (!newValue) {
                            deseleccionarPermisosDependientes(permiso.getNombre());
                        }
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void habilitarPermisosDependientes(String permisoVisualizacion, boolean habilitar) {
        for (Map.Entry<String, String> entry : permisosDependencias.entrySet()) {
            String permisoAccion = entry.getKey();
            String permisoRequerido = entry.getValue();

            if (permisoRequerido.equals(permisoVisualizacion)) {
                CheckBox checkBoxAccion = permisosCheckBoxMap.get(permisoAccion);
                if (checkBoxAccion != null) {
                    checkBoxAccion.setDisable(!habilitar);
                }
            }
        }
    }

    private void deseleccionarPermisosDependientes(String permisoVisualizacion) {
        for (Map.Entry<String, String> entry : permisosDependencias.entrySet()) {
            String permisoAccion = entry.getKey();
            String permisoRequerido = entry.getValue();

            if (permisoRequerido.equals(permisoVisualizacion)) {
                CheckBox checkBoxAccion = permisosCheckBoxMap.get(permisoAccion);
                if (checkBoxAccion != null) {
                    checkBoxAccion.setSelected(false);
                }
            }
        }
    }

    @FXML
    private void undoChanges() {
        for (Map.Entry<String, Boolean> entry : estadoInicialPermisos.entrySet()) {
            String permisoNombre = entry.getKey();
            boolean estadoInicial = entry.getValue();
            CheckBox checkBox = permisosCheckBoxMap.get(permisoNombre);

            if (checkBox != null) {
                checkBox.setSelected(estadoInicial);
            }
        }
        cambiosSinGuardar = false;
        System.out.println("Cambios deshechos.");
    }

    private void guardarPermisosSupervisor(int supervisorId) throws SQLException {
        for (Map.Entry<String, CheckBox> entry : permisosCheckBoxMap.entrySet()) {
            String permisoNombre = entry.getKey();
            CheckBox checkBox = entry.getValue();

            if (!checkBox.isDisabled()) {
                if (checkBox.isSelected()) {
                    usuariosDAO.asignarPermiso(supervisorId, permisoNombre);
                } else {
                    usuariosDAO.eliminarPermiso(supervisorId, permisoNombre);
                }
            }
        }
    }

    @FXML
    private void applyChanges() {
        List<String> permisosModificados = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : permisosCheckBoxMap.entrySet()) {
            String permiso = entry.getKey();
            boolean estadoInicial = estadoInicialPermisos.get(permiso);
            boolean estadoActual = entry.getValue().isSelected();
            if (estadoInicial != estadoActual) {
                permisosModificados.add(permiso + (estadoActual ? " - Activado" : " - Desactivado"));
            }
        }

        if (!permisosModificados.isEmpty()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmación de Cambios");
            confirmacion.setHeaderText("Se han modificado los siguientes permisos:");
            confirmacion.setContentText(String.join("\n", permisosModificados) + "\n\n¿Desea aplicar estos cambios?");

            ButtonType botonConfirmar = new ButtonType("Confirmar cambios");
            ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmacion.getButtonTypes().setAll(botonConfirmar, botonCancelar);

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == botonConfirmar) {
                    // Llama a guardarPermisosSupervisor o cualquier método para aplicar cambios
                    try {
                        guardarPermisosSupervisor(supervisorId);
                        cambiosSinGuardar = false;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            System.out.println("No hay cambios en los permisos.");
        }
    }
}
