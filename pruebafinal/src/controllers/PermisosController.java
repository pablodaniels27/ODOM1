package controllers;

import DAO.BaseDAO;
import DAO.UsuariosDAO;
import Usuarios.Permisos;
import Usuarios.Supervisor;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermisosController {

    // Bandera para cambios sin guardar
    private boolean cambiosSinGuardar = false;

    // Método para verificar si hay cambios sin guardar
    public boolean tieneCambiosSinGuardar() {
        return cambiosSinGuardar;
    }

    private UsuariosDAO usuariosDAO;

    @FXML
    private VBox permisosContainer; // Contenedor dinámico para CheckBoxes

    @FXML
    private TextField searchField;

    @FXML
    private Label supervisorLabel; // Label para mostrar el nombre del supervisor

    @FXML
    private Button applyChangesButton; // Botón para aplicar cambios

    @FXML
    private Button undoChangesButton; // Botón para deshacer cambios

    private ContextMenu suggestionsMenu = new ContextMenu();

    // Mapa para asociar permisos con sus CheckBoxes
    private Map<String, CheckBox> permisosCheckBoxMap = new HashMap<>();
    private Map<String, Boolean> estadoInicialPermisos = new HashMap<>(); // Estado inicial de permisos


    Map<String, String> permisosDependencias = new HashMap<>();

    @FXML
    public void initialize() {
        // Inicializa UsuariosDAO usando la conexión de DatabaseConnection
        Connection conexion;
        try {
            conexion = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        usuariosDAO = new UsuariosDAO(conexion);

        // Llenar el mapa de dependencias de permisos
        inicializarDependenciasDePermisos();

        // Cargar todos los permisos dinámicamente al inicializar la vista
        cargarPermisosDisponibles();

        // Deshabilita el botón de aplicar cambios y de deshacer cambios al inicio
        applyChangesButton.setDisable(true);
        undoChangesButton.setDisable(true);
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

    @FXML
    private void searchForSupervisors(KeyEvent event) {
        String searchQuery = searchField.getText().trim();

        if (searchQuery.length() < 3) {
            suggestionsMenu.hide();
            return;
        }

        try {
            ObservableList<String> results = BaseDAO.buscarSupervisores(searchQuery);

            if (!results.isEmpty()) {
                populateSuggestions(results);
            } else {
                suggestionsMenu.hide();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateSuggestions(ObservableList<String> suggestions) {
        suggestionsMenu.getItems().clear();

        VBox suggestionBox = new VBox();
        suggestionBox.setFillWidth(true);
        suggestionBox.setStyle("-fx-background-color: white;");

        for (String suggestion : suggestions) {
            Label item = new Label(suggestion);
            item.setStyle("-fx-padding: 5px; -fx-background-color: white;");

            // Actualiza solo el campo de búsqueda, sin actualizar el label del supervisor
            item.setOnMouseClicked(event -> {
                searchField.setText(suggestion);  // Actualiza el campo de búsqueda con la sugerencia
                suggestionsMenu.hide();  // Cierra el menú de sugerencias
            });

            suggestionBox.getChildren().add(item);
        }

        ScrollPane scrollPane = new ScrollPane(suggestionBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        CustomMenuItem scrollableMenuItem = new CustomMenuItem(scrollPane, false);
        suggestionsMenu.getItems().add(scrollableMenuItem);

        if (!suggestionsMenu.isShowing()) {
            Bounds boundsInScreen = searchField.localToScreen(searchField.getBoundsInLocal());
            suggestionsMenu.show(searchField, boundsInScreen.getMinX(), boundsInScreen.getMaxY());
        }
    }

    @FXML
    private void handleSearchButton() {
        System.out.println("Botón de búsqueda presionado");

        String nombreSupervisor = searchField.getText().trim();

        if (!nombreSupervisor.isEmpty()) {
            supervisorLabel.setText("Supervisor: " + nombreSupervisor); // Actualizar el label del supervisor
            cargarPermisosSupervisor(nombreSupervisor);
            applyChangesButton.setDisable(false); // Habilitar el botón de aplicar cambios solo después de una búsqueda exitosa
            undoChangesButton.setDisable(false); // Habilitar el botón de deshacer cambios solo después de una búsqueda exitosa
            cambiosSinGuardar = false; // Restablece la bandera al hacer una búsqueda
        } else {
            System.out.println("Ingrese el nombre del supervisor a buscar.");
            applyChangesButton.setDisable(true); // Deshabilitar el botón si no hay texto en el campo
            undoChangesButton.setDisable(true); // Deshabilitar el botón si no hay texto en el campo
        }
    }

    private void cargarPermisosSupervisor(String nombreSupervisor) {
        try {
            Supervisor supervisor = usuariosDAO.obtenerSupervisorConPermisos(nombreSupervisor);

            if (supervisor != null) {
                Set<Permisos> permisosDelSupervisor = supervisor.getPermisos();

                // Guarda el estado inicial de los permisos
                estadoInicialPermisos.clear();
                for (Map.Entry<String, CheckBox> entry : permisosCheckBoxMap.entrySet()) {
                    String permisoNombre = entry.getKey();
                    CheckBox checkBox = entry.getValue();

                    boolean tienePermiso = permisosDelSupervisor.stream()
                            .anyMatch(p -> p.getNombre().trim().equalsIgnoreCase(permisoNombre.trim()));

                    checkBox.setSelected(tienePermiso);
                    estadoInicialPermisos.put(permisoNombre, tienePermiso); // Guardar el estado inicial

                    // Agregar listener para detectar cambios
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> cambiosSinGuardar = true);
                }
            } else {
                System.out.println("Supervisor no encontrado en la base de datos.");
                applyChangesButton.setDisable(true); // Deshabilitar el botón si el supervisor no es encontrado
                undoChangesButton.setDisable(true); // Deshabilitar el botón si el supervisor no es encontrado
            }
        } catch (SQLException e) {
            e.printStackTrace();
            applyChangesButton.setDisable(true); // Deshabilitar el botón en caso de error de búsqueda
            undoChangesButton.setDisable(true); // Deshabilitar el botón en caso de error de búsqueda
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
                    checkBoxAccion.setSelected(false); // Deselecciona el permiso dependiente
                }
            }
        }
    }

    @FXML
    private void undoChanges() {
        // Restaura el estado inicial de los permisos
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

    @FXML
    public void onWindowCloseRequest() {
        if (cambiosSinGuardar) {
            // Mostrar popup de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cambios sin guardar");
            alert.setHeaderText("Tiene cambios sin guardar");
            alert.setContentText("¿Desea guardar los cambios antes de salir?");

            ButtonType buttonTypeGuardar = new ButtonType("Sí");
            ButtonType buttonTypeNoGuardar = new ButtonType("No");
            ButtonType buttonTypeCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeGuardar, buttonTypeNoGuardar, buttonTypeCancelar);

            alert.showAndWait().ifPresent(response -> {
                if (response == buttonTypeGuardar) {
                    applyChanges();
                    cerrarVentana();
                } else if (response == buttonTypeNoGuardar) {
                    cerrarVentana();
                }
            });
        } else {
            cerrarVentana();
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) permisosContainer.getScene().getWindow();
        stage.close();
    }

    private void guardarPermisosSupervisor(int supervisorId) throws SQLException {
        for (Map.Entry<String, CheckBox> entry : permisosCheckBoxMap.entrySet()) {
            String permisoNombre = entry.getKey();
            CheckBox checkBox = entry.getValue();

            // Solo guarda si el CheckBox está habilitado y seleccionado
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
    public void applyChanges() {
        try {
            Supervisor supervisor = usuariosDAO.obtenerSupervisorConPermisos(searchField.getText().trim());
            if (supervisor != null) {
                guardarPermisosSupervisor(supervisor.getId());
                System.out.println("Cambios aplicados correctamente.");
                cambiosSinGuardar = false;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar los permisos: " + e.getMessage());
        }
    }
}
//asd