package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AuditoriaController {

    @FXML
    private TextField dateFilterField;

    @FXML
    private TextField userFilterField;

    @FXML
    private ComboBox<String> actionFilterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Auditoria> auditoriaTable;

    @FXML
    private TableColumn<Auditoria, String> nombreCompletoColumn;

    @FXML
    private TableColumn<Auditoria, Integer> departamentoIdColumn;

    @FXML
    private TableColumn<Auditoria, String> accionColumn;

    @FXML
    private TableColumn<Auditoria, Integer> targetEmployeeIdColumn;

    @FXML
    private TableColumn<Auditoria, String> timestampColumn;

    @FXML
    private TableColumn<Auditoria, String> detallesColumn;

    // Lista de datos de auditoría (simulación de una base de datos)
    private ObservableList<Auditoria> auditoriaData = FXCollections.observableArrayList();

    public void initialize() {
        // Configurar las columnas
        nombreCompletoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        departamentoIdColumn.setCellValueFactory(new PropertyValueFactory<>("departamentoId"));
        accionColumn.setCellValueFactory(new PropertyValueFactory<>("accion"));
        targetEmployeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("targetEmployeeId"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        detallesColumn.setCellValueFactory(new PropertyValueFactory<>("detalles"));

        // Inicializar combo box
        actionFilterComboBox.getItems().addAll("Todos", "Agregar", "Modificar", "Eliminar", "Consultar");

        // Cargar datos iniciales (puedes reemplazarlo con una consulta a la base de datos)
        cargarDatos();

        // Establecer los datos en la tabla
        auditoriaTable.setItems(auditoriaData);
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        // Implementa la lógica para filtrar los datos según el término de búsqueda
        System.out.println("Buscar: " + searchTerm);
        // Aquí puedes implementar un filtrado real
    }

    private void cargarDatos() {
        // Aquí puedes cargar los datos desde una base de datos. Por ahora, añadimos algunos datos de ejemplo.
        auditoriaData.add(new Auditoria("Juan Pérez", 101, "Agregar", 1201, "2024-09-26 10:15:00", "Se agregó un nuevo empleado"));
        auditoriaData.add(new Auditoria("Ana López", 102, "Modificar", 1202, "2024-09-25 14:30:00", "Se modificó la información del empleado"));
        auditoriaData.add(new Auditoria("Carlos Ruiz", 103, "Eliminar", 1203, "2024-09-24 09:00:00", "Se eliminó un registro de asistencia"));
    }

    // Clase Auditoria como parte del mismo archivo
    public static class Auditoria {
        private String nombreCompleto;
        private int departamentoId;
        private String accion;
        private int targetEmployeeId;
        private String timestamp;
        private String detalles;

        public Auditoria(String nombreCompleto, int departamentoId, String accion, int targetEmployeeId, String timestamp, String detalles) {
            this.nombreCompleto = nombreCompleto;
            this.departamentoId = departamentoId;
            this.accion = accion;
            this.targetEmployeeId = targetEmployeeId;
            this.timestamp = timestamp;
            this.detalles = detalles;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public void setNombreCompleto(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }

        public int getDepartamentoId() {
            return departamentoId;
        }

        public void setDepartamentoId(int departamentoId) {
            this.departamentoId = departamentoId;
        }

        public String getAccion() {
            return accion;
        }

        public void setAccion(String accion) {
            this.accion = accion;
        }

        public int getTargetEmployeeId() {
            return targetEmployeeId;
        }

        public void setTargetEmployeeId(int targetEmployeeId) {
            this.targetEmployeeId = targetEmployeeId;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getDetalles() {
            return detalles;
        }

        public void setDetalles(String detalles) {
            this.detalles = detalles;
        }
    }
}
