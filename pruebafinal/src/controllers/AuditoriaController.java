package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditoriaController {

    @FXML
    private TableView<Auditoria> auditoriaTable;

    @FXML
    private TableColumn<Auditoria, String> nombreCompletoEmpleado;

    @FXML
    private TableColumn<Auditoria, String> nombreCompletoSupervisor;

    @FXML
    private TableColumn<Auditoria, String> departamentoColumn;

    @FXML
    private TableColumn<Auditoria, String> accionColumn;

    @FXML
    private TableColumn<Auditoria, String> timestampColumn;

    @FXML
    private TableColumn<Auditoria, String> detallesColumn;

    @FXML
    private DatePicker fechaInicioPicker;

    @FXML
    private DatePicker fechaFinPicker;

    @FXML
    private TextField userFilterField;

    @FXML
    private ComboBox<String> actionFilterComboBox;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label currentPageLabel;

    @FXML
    private HBox paginationBox;

    private ObservableList<Auditoria> auditoriaData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages;

    public void initialize() throws SQLException {
        nombreCompletoSupervisor.setCellValueFactory(new PropertyValueFactory<>("nombreSupervisor"));
        nombreCompletoEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreCompletoEmpleado"));
        departamentoColumn.setCellValueFactory(new PropertyValueFactory<>("departamentoNombre"));
        accionColumn.setCellValueFactory(new PropertyValueFactory<>("accion"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        detallesColumn.setCellValueFactory(new PropertyValueFactory<>("detalles"));

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

        cargarDatos();

        auditoriaTable.setItems(auditoriaData);

        previousButton.setOnAction(event -> handlePreviousPage());
        nextButton.setOnAction(event -> handleNextPage());

        totalPages = (int) Math.ceil((double) auditoriaData.size() / itemsPerPage);
        showPage(currentPage);
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

        totalPages = (int) Math.ceil((double) auditoriaData.size() / itemsPerPage);
    }

    private void showPage(int pageNumber) {
        currentPage = pageNumber;
        int fromIndex = (pageNumber - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, auditoriaData.size());

        if (fromIndex <= auditoriaData.size()) {
            auditoriaTable.setItems(FXCollections.observableArrayList(auditoriaData.subList(fromIndex, toIndex)));
        }

        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        previousButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);
        currentPageLabel.setText("Página " + currentPage + " de " + totalPages);
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            showPage(currentPage - 1);
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            showPage(currentPage + 1);
        }
    }

    @FXML
    private void handleSearch() {
        System.out.println("Buscar: Supervisor=" + userFilterField.getText() +
                ", Fecha Inicio=" + (fechaInicioPicker.getValue() != null ? fechaInicioPicker.getValue().toString() : "") +
                ", Fecha Fin=" + (fechaFinPicker.getValue() != null ? fechaFinPicker.getValue().toString() : "") +
                ", Acción=" + actionFilterComboBox.getValue());

        // Aquí puedes implementar un filtrado real y luego llamar a `showPage(1);` para mostrar los resultados filtrados
    }
}
