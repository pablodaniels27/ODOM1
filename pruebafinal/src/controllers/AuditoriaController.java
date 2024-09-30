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
import java.time.LocalDate;
import java.util.List;

public class AuditoriaController {

    @FXML
    private Label currentPageLabel;

    @FXML
    private TableColumn<Auditoria, String> nombreCompletoEmpleado;

    @FXML
    private TableColumn<Auditoria, String> nombreCompletoSupervisor;

    @FXML
    private TableColumn<Auditoria, String> departamentoColumn;

    @FXML
    private DatePicker fechaInicioPicker;

    @FXML
    private DatePicker fechaFinPicker;

    @FXML
    private TextField userFilterField;

    @FXML
    private ComboBox<String> actionFilterComboBox;

    @FXML
    private TableView<Auditoria> auditoriaTable;

    @FXML
    private TableColumn<Auditoria, String> accionColumn;

    @FXML
    private TableColumn<Auditoria, String> timestampColumn;

    @FXML
    private TableColumn<Auditoria, String> detallesColumn;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button searchButton;

    private ObservableList<Auditoria> auditoriaData = FXCollections.observableArrayList();
    private ObservableList<Auditoria> filteredData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    @FXML
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
                    setText(null);
                    setOnMouseClicked(null);
                    setStyle("");
                } else {
                    setText("ver detalles");
                    setStyle("-fx-text-fill: blue; -fx-underline: true;");
                    setOnMouseClicked(event -> {
                        if (!isEmpty()) {
                            showDetailsPopup(item);
                        }
                    });
                }
            }
        });

        actionFilterComboBox.getItems().addAll("Todos", "Agregar", "Modificar", "Eliminar", "Consultar");

        // Cargar datos iniciales
        cargarDatos();

        // Copiar los datos cargados a la lista filtrada
        filteredData.addAll(auditoriaData);

        // Configurar el tamaño fijo de las celdas
        auditoriaTable.setFixedCellSize(25); // Ajusta este valor según el alto de tus celdas

        // Añadir listener para ajustar itemsPerPage según el alto de la tabla
        auditoriaTable.heightProperty().addListener((obs, oldVal, newVal) -> {
            int newItemsPerPage = (int) (newVal.doubleValue() / auditoriaTable.getFixedCellSize());
            if (newItemsPerPage != itemsPerPage && newItemsPerPage > 0) {
                itemsPerPage = newItemsPerPage;
                totalPages = (int) Math.ceil((double) filteredData.size() / itemsPerPage);
                if (currentPage > totalPages) {
                    currentPage = totalPages;
                }
                showPage(currentPage);
            }
        });

        // Calcular itemsPerPage inicial
        itemsPerPage = (int) (auditoriaTable.getHeight() / auditoriaTable.getFixedCellSize());
        if (itemsPerPage <= 0) {
            itemsPerPage = 10; // Valor por defecto si aún no se ha inicializado el alto
        }

        // Calcular total de páginas
        totalPages = (int) Math.ceil((double) filteredData.size() / itemsPerPage);

        // Mostrar la primera página
        showPage(1);
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage);
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            showPage(currentPage);
        }
    }

    @FXML
    private void validateAndSearch() {
        if (fechaInicioPicker.getValue() == null || fechaFinPicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Faltan fechas");
            alert.setContentText("Por favor, selecciona una fecha de inicio y una fecha final.");
            alert.showAndWait();
            return;
        }

        if (fechaInicioPicker.getValue().isAfter(fechaFinPicker.getValue())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Rango de fechas inválido");
            alert.setContentText("La fecha de inicio no puede ser posterior a la fecha final.");
            alert.showAndWait();
            return;
        }

        handleSearch();
    }

    @FXML
    private void handleSearch() {
        String supervisorFilter = userFilterField.getText().trim().toLowerCase();
        String actionFilter = actionFilterComboBox.getValue();
        LocalDate fechaInicio = fechaInicioPicker.getValue();
        LocalDate fechaFin = fechaFinPicker.getValue();

        ObservableList<Auditoria> filteredList = FXCollections.observableArrayList();

        for (Auditoria auditoria : auditoriaData) {
            boolean matchesSupervisor = supervisorFilter.isEmpty() ||
                    auditoria.getNombreSupervisor().toLowerCase().contains(supervisorFilter);

            boolean matchesAction = actionFilter == null || actionFilter.equals("Todos") ||
                    auditoria.getAccion().equalsIgnoreCase(actionFilter);

            boolean matchesDateRange = true;
            if (fechaInicio != null && fechaFin != null) {
                LocalDate auditDate = LocalDate.parse(auditoria.getTimestamp().substring(0, 10));
                matchesDateRange = !auditDate.isBefore(fechaInicio) && !auditDate.isAfter(fechaFin);
            }

            if (matchesSupervisor && matchesAction && matchesDateRange) {
                filteredList.add(auditoria);
            }
        }

        // Actualizar la lista filtrada y recalcular paginación
        filteredData = filteredList;
        totalPages = (int) Math.ceil((double) filteredData.size() / itemsPerPage);
        currentPage = 1;
        showPage(currentPage);
    }

    private void cargarDatos() throws SQLException {
        auditoriaData.clear();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String query = "SELECT l.supervisor_id, l.action, l.target_employee_id, l.timestamp, l.details, " +
                "esuper.nombres AS supervisor_nombres, esuper.apellido_paterno AS supervisor_apellido_paterno, esuper.apellido_materno AS supervisor_apellido_materno, " +
                "etarget.nombres AS empleado_nombres, etarget.apellido_paterno AS empleado_apellido_paterno, etarget.apellido_materno AS empleado_apellido_materno, " +
                "d.nombre AS departamento_nombre " +
                "FROM logs l " +
                "JOIN empleados esuper ON l.supervisor_id = esuper.id " +
                "JOIN empleados etarget ON l.target_employee_id = etarget.id " +
                "JOIN departamentos d ON etarget.departamento_id = d.id " +
                "ORDER BY l.timestamp DESC";

        PreparedStatement preparedStatement = connectDB.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            String nombreCompletoSupervisor = resultSet.getString("supervisor_nombres") + " " +
                    resultSet.getString("supervisor_apellido_paterno") + " " +
                    resultSet.getString("supervisor_apellido_materno");

            String nombreCompletoEmpleado = resultSet.getString("empleado_nombres") + " " +
                    resultSet.getString("empleado_apellido_paterno") + " " +
                    resultSet.getString("empleado_apellido_materno");

            String departamentoNombre = resultSet.getString("departamento_nombre");
            String accion = resultSet.getString("action");
            String timestamp = resultSet.getString("timestamp");
            String detalles = resultSet.getString("details");

            auditoriaData.add(new Auditoria(nombreCompletoEmpleado, departamentoNombre, accion, nombreCompletoSupervisor, timestamp, detalles));
        }

        resultSet.close();
        preparedStatement.close();
        connectDB.close();
    }

    private void showPage(int pageNumber) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageNumber > totalPages) {
            pageNumber = totalPages;
        }
        currentPage = pageNumber;

        int fromIndex = (pageNumber - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredData.size());

        if (fromIndex > filteredData.size()) {
            fromIndex = filteredData.size() - itemsPerPage;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        List<Auditoria> pageData = filteredData.subList(fromIndex, toIndex);
        auditoriaTable.setItems(FXCollections.observableArrayList(pageData));

        // Actualizamos el label de la página actual
        currentPageLabel.setText("Página " + currentPage + " de " + totalPages);

        previousButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages || totalPages == 0);
    }

    private void showDetailsPopup(String detalle) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles");
        alert.setHeaderText("Detalles de la Acción:");
        alert.setContentText(detalle);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    public static class Auditoria {
        private String nombreCompletoEmpleado;
        private String departamentoNombre;
        private String accion;
        private String nombreSupervisor;
        private String timestamp;
        private String detalles;

        public Auditoria(String nombreCompletoEmpleado, String departamentoNombre, String accion, String nombreSupervisor, String timestamp, String detalles) {
            this.nombreCompletoEmpleado = nombreCompletoEmpleado;
            this.departamentoNombre = departamentoNombre;
            this.accion = accion;
            this.nombreSupervisor = nombreSupervisor;
            this.timestamp = timestamp;
            this.detalles = detalles;
        }

        public String getNombreCompletoEmpleado() {
            return nombreCompletoEmpleado;
        }

        public String getDepartamentoNombre() {
            return departamentoNombre;
        }

        public String getAccion() {
            return accion;
        }

        public String getNombreSupervisor() {
            return nombreSupervisor;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getDetalles() {
            return detalles;
        }
    }
}
