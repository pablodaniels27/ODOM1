package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditoriaController {
    @FXML
    public TableColumn nombreCompletoEmpleado;

    @FXML
    public TableColumn nombreCompletoSupervisor;

    @FXML
    public TableColumn departamentoColumn;

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
    private TableColumn<Auditoria, String> accionColumn;

    @FXML
    private TableColumn<Auditoria, Integer> targetEmployeeIdColumn;

    @FXML
    private TableColumn<Auditoria, String> timestampColumn;

    @FXML
    private TableColumn<Auditoria, String> detallesColumn;

    // Lista de datos de auditoría (simulación de una base de datos)
    private ObservableList<Auditoria> auditoriaData = FXCollections.observableArrayList();

    public void initialize() throws SQLException {
        // Configurar las columnas con las propiedades correctas
        nombreCompletoSupervisor.setCellValueFactory(new PropertyValueFactory<>("nombreSupervisor"));
        nombreCompletoEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreCompletoEmpleado"));
        departamentoColumn.setCellValueFactory(new PropertyValueFactory<>("departamentoNombre"));
        accionColumn.setCellValueFactory(new PropertyValueFactory<>("accion"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        detallesColumn.setCellValueFactory(new PropertyValueFactory<>("detalles"));

        // Configurar la columna detalles con estilo de enlace y mostrar un popup
        detallesColumn.setCellFactory(tc -> new TableCell<Auditoria, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null); // Sin detalles, dejar vacío
                    setOnMouseClicked(null);
                    setStyle(""); // Resetear el estilo cuando esté vacío
                } else {
                    setText("ver detalles");
                    setStyle("-fx-text-fill: blue; -fx-underline: true;"); // Estilo de enlace
                    setOnMouseClicked(event -> {
                        if (!isEmpty()) {
                            showDetailsPopup(item); // Mostrar el popup con los detalles
                        }
                    });
                }
            }
        });

        // Inicializar combo box
        actionFilterComboBox.getItems().addAll("Todos", "Agregar", "Modificar", "Eliminar", "Consultar");

        // Cargar datos iniciales (puedes reemplazarlo con una consulta a la base de datos)
        cargarDatos();

        // Establecer los datos en la tabla
        auditoriaTable.setItems(auditoriaData);
    }

    public static class Auditoria {
        private String nombreCompletoEmpleado; // Nombre del empleado afectado
        private String departamentoNombre; // Nombre del departamento
        private String accion;
        private String nombreSupervisor; // Nombre del supervisor
        private String timestamp;
        private String detalles;

        // Constructor
        public Auditoria(String nombreCompletoEmpleado, String departamentoNombre, String accion, String nombreSupervisor, String timestamp, String detalles) {
            this.nombreCompletoEmpleado = nombreCompletoEmpleado;
            this.departamentoNombre = departamentoNombre;
            this.accion = accion;
            this.nombreSupervisor = nombreSupervisor;
            this.timestamp = timestamp;
            this.detalles = detalles;
        }

        // Getters y Setters
        public String getNombreCompletoEmpleado() {
            return nombreCompletoEmpleado;
        }

        public void setNombreCompletoEmpleado(String nombreCompletoEmpleado) {
            this.nombreCompletoEmpleado = nombreCompletoEmpleado;
        }

        public String getDepartamentoNombre() {
            return departamentoNombre;
        }

        public void setDepartamentoNombre(String departamentoNombre) {
            this.departamentoNombre = departamentoNombre;
        }

        public String getAccion() {
            return accion;
        }

        public void setAccion(String accion) {
            this.accion = accion;
        }

        public String getNombreSupervisor() {
            return nombreSupervisor;
        }

        public void setNombreSupervisor(String nombreSupervisor) {
            this.nombreSupervisor = nombreSupervisor;
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

    private void showDetailsPopup(String detalle) {
        // Crear un nuevo dialog para mostrar los detalles
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles");
        alert.setHeaderText("Detalles de la Acción:");
        alert.setContentText(detalle);

        // Hacer que el diálogo sea modal
        alert.initModality(Modality.APPLICATION_MODAL);

        // Mostrar el diálogo
        alert.showAndWait();
    }





    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        // Implementa la lógica para filtrar los datos según el término de búsqueda
        System.out.println("Buscar: " + searchTerm);
        // Aquí puedes implementar un filtrado real
    }

    private void cargarDatos() throws SQLException {
        auditoriaData.clear();  // Limpiar los datos anteriores

        // Conectar a la base de datos
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Consulta SQL que une los logs con la tabla de empleados y departamentos
        String query = "SELECT l.supervisor_id, l.action, l.target_employee_id, l.timestamp, l.details, " +
                "esuper.nombres AS supervisor_nombres, esuper.apellido_paterno AS supervisor_apellido_paterno, esuper.apellido_materno AS supervisor_apellido_materno, " +
                "etarget.nombres AS empleado_nombres, etarget.apellido_paterno AS empleado_apellido_paterno, etarget.apellido_materno AS empleado_apellido_materno, " +
                "d.nombre AS departamento_nombre " + // Obtener el nombre del departamento
                "FROM logs l " +
                "JOIN empleados esuper ON l.supervisor_id = esuper.id " + // Obtener el nombre completo del supervisor
                "JOIN empleados etarget ON l.target_employee_id = etarget.id " + // Obtener el nombre completo del empleado
                "JOIN departamentos d ON etarget.departamento_id = d.id " + // Obtener el nombre del departamento
                "ORDER BY l.timestamp DESC";

        // Ejecutar la consulta
        PreparedStatement preparedStatement = connectDB.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();

        // Procesar los resultados y añadir a la lista observable
        while (resultSet.next()) {
            // Obtener y concatenar el nombre completo del supervisor
            String nombreCompletoSupervisor = resultSet.getString("supervisor_nombres") + " " +
                    resultSet.getString("supervisor_apellido_paterno") + " " +
                    resultSet.getString("supervisor_apellido_materno");

            // Obtener y concatenar el nombre completo del empleado
            String nombreCompletoEmpleado = resultSet.getString("empleado_nombres") + " " +
                    resultSet.getString("empleado_apellido_paterno") + " " +
                    resultSet.getString("empleado_apellido_materno");

            // Obtener el nombre del departamento
            String departamentoNombre = resultSet.getString("departamento_nombre");

            String accion = resultSet.getString("action");
            String timestamp = resultSet.getString("timestamp");
            String detalles = resultSet.getString("details");

            // Añadir los datos a la lista observable
            auditoriaData.add(new Auditoria(nombreCompletoEmpleado, departamentoNombre, accion, nombreCompletoSupervisor, timestamp, detalles));
        }

        // Cerrar las conexiones
        resultSet.close();
        preparedStatement.close();
        connectDB.close();
    }


    // Clase Auditoria como parte del mismo archivo

}
