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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PermisosController {

    private UsuariosDAO usuariosDAO;

    @FXML
    private VBox permisosContainer; // Contenedor dinámico para CheckBoxes

    @FXML
    private TextField searchField;

    private ContextMenu suggestionsMenu = new ContextMenu();

    // Mapa para asociar permisos con sus CheckBoxes
    private Map<String, CheckBox> permisosCheckBoxMap = new HashMap<>();

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

        // Cargar todos los permisos dinámicamente al inicializar la vista
        cargarPermisosDisponibles();
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

            // Actualiza solo el campo de búsqueda, pero no carga los permisos automáticamente
            item.setOnMouseClicked(event -> {
                searchField.setText(suggestion);  // Actualiza el campo de búsqueda con la sugerencia
                suggestionsMenu.hide();  // Cierra el menú de sugerencias
                // No llamamos a cargarPermisosSupervisor aquí
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


    private void cargarPermisosSupervisor(String nombreSupervisor) {
        try {
            Supervisor supervisor = usuariosDAO.obtenerSupervisorConPermisos(nombreSupervisor);

            if (supervisor != null) {
                Set<Permisos> permisosDelSupervisor = supervisor.getPermisos();

                System.out.println("Permisos del supervisor: ");
                permisosDelSupervisor.forEach(p -> System.out.println(p.getNombre()));

                // Para cada permiso, selecciona o deselecciona el CheckBox según lo que tenga el supervisor
                for (Map.Entry<String, CheckBox> entry : permisosCheckBoxMap.entrySet()) {
                    String permisoNombre = entry.getKey();
                    CheckBox checkBox = entry.getValue();

                    // Verificar si el permiso del supervisor coincide con el permiso actual del Map
                    boolean tienePermiso = permisosDelSupervisor.stream()
                            .anyMatch(p -> p.getNombre().trim().equalsIgnoreCase(permisoNombre.trim()));

                    // Marcar o desmarcar el CheckBox según el permiso del supervisor
                    checkBox.setSelected(tienePermiso);
                }
            } else {
                System.out.println("Supervisor no encontrado en la base de datos.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private void guardarPermisosSupervisor(int supervisorId) throws SQLException {
        // Recorre el mapa de CheckBoxes y actualiza los permisos según su estado
        for (Map.Entry<String, CheckBox> entry : permisosCheckBoxMap.entrySet()) {
            String permisoNombre = entry.getKey();
            CheckBox checkBox = entry.getValue();

            if (checkBox.isSelected()) {
                // Si está seleccionado, asegúrate de que el supervisor tenga este permiso
                usuariosDAO.asignarPermiso(supervisorId, permisoNombre);
            } else {
                // Si no está seleccionado, elimina el permiso si lo tiene
                usuariosDAO.eliminarPermiso(supervisorId, permisoNombre);
            }
        }
    }



    @FXML
    private void handleSearchButton() {
        System.out.println("Botón de búsqueda presionado");

        String nombreSupervisor = searchField.getText().trim();

        if (!nombreSupervisor.isEmpty()) {
            // Llamamos para cargar los permisos del supervisor y marcar los CheckBoxes
            cargarPermisosSupervisor(nombreSupervisor);
        } else {
            System.out.println("Ingrese el nombre del supervisor a buscar.");
        }
    }

    @FXML
    public void applyChanges() {
        try {
            // Llama al método para guardar los permisos del supervisor
            Supervisor supervisor = usuariosDAO.obtenerSupervisorConPermisos(searchField.getText().trim());
            if (supervisor != null) {
                guardarPermisosSupervisor(supervisor.getId());
                System.out.println("Cambios aplicados correctamente.");
            }
        } catch (SQLException e) {
            // Maneja el error
            System.err.println("Error al guardar los permisos: " + e.getMessage());
        }
    }
    private void cargarPermisosDisponibles() {
        try {
            // Obtener todos los permisos del sistema
            Set<Permisos> todosLosPermisos = usuariosDAO.obtenerTodosLosPermisos();

            // Limpiamos el contenedor y el mapa de CheckBoxes antes de agregar nuevos
            permisosContainer.getChildren().clear();
            permisosCheckBoxMap.clear();

            // Crear CheckBoxes dinámicamente sin seleccionarlos
            for (Permisos permiso : todosLosPermisos) {
                CheckBox checkBox = new CheckBox(permiso.getNombre());

                // No seleccionamos ningún CheckBox al inicializar
                checkBox.setSelected(false);

                // Añadir el CheckBox al contenedor y al mapa
                permisosContainer.getChildren().add(checkBox);
                permisosCheckBoxMap.put(permiso.getNombre(), checkBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
