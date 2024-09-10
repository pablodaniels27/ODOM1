package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;

public class MonitoreoController {

    @FXML
    private TableView<Map<String, Object>> employeeTableView;

    @FXML
    private TableColumn<Map<String, Object>, String> nombreColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> idColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> fechaEntradaColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> horaEntradaColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> horaSalidaColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> tiempoLaboradoColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> tipoAsistenciaColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> tipoSalidaColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> estadoColumn;

    @FXML
    private DatePicker fechaInicioPicker;

    @FXML
    private DatePicker fechaFinPicker;

    @FXML
    private Button searchButton;

    @FXML
    private ChoiceBox<String> departamentoChoiceBox;

    @FXML
    private CheckBox supervisoresCheckBox;

    @FXML
    private CheckBox empleadosCheckBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button graphViewButton;

    @FXML
    private Pane chartPane;

    @FXML
    private ChoiceBox<Integer> itemsPerPageChoiceBox;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button page1Button;

    @FXML
    private Button page2Button;

    @FXML
    private Button page3Button;

    @FXML
    private VBox detailsPane;

    @FXML
    private ListView<String> detailsListView;

    @FXML
    private HBox chartContainer;

    private ObservableList<Map<String, Object>> employees = FXCollections.observableArrayList();

    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    private GraficosController graficosController = new GraficosController();

    @FXML
    public void initialize() {
        // Configurar las columnas con los Callbacks
        nombreColumn.setCellValueFactory(createCellValueFactory("nombreCompleto"));
        idColumn.setCellValueFactory(createCellValueFactory("id"));
        fechaEntradaColumn.setCellValueFactory(createCellValueFactory("fechaEntrada"));
        horaEntradaColumn.setCellValueFactory(createCellValueFactory("horaEntrada"));
        horaSalidaColumn.setCellValueFactory(createCellValueFactory("horaSalida"));
        tiempoLaboradoColumn.setCellValueFactory(createCellValueFactory("tiempoLaborado"));
        tipoAsistenciaColumn.setCellValueFactory(createCellValueFactory("tipoAsistencia"));
        tipoSalidaColumn.setCellValueFactory(createCellValueFactory("tipoSalida"));
        estadoColumn.setCellValueFactory(createCellValueFactory("estado"));

        employeeTableView.setItems(employees);

        cargarDepartamentos(); // Cargar los departamentos en el ChoiceBox
        departamentoChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> handleDepartmentSelection());

        // Configurar el ChoiceBox para la cantidad de ítems por página
        itemsPerPageChoiceBox.setItems(FXCollections.observableArrayList(5, 10, 20, 50));
        itemsPerPageChoiceBox.setValue(itemsPerPage);
        itemsPerPageChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            itemsPerPage = newVal;
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
            showPage(1);
        });

        // Configurar comportamiento del botón de gráficos
        graphViewButton.setOnAction(event -> toggleGraphView());

        // Cargar todos los registros disponibles al inicio
        try {
            loadAllEntries();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Configurar el botón de búsqueda
        searchButton.setOnAction(event -> {
            try {
                searchByDateAndDepartment(); // Buscar por fecha y departamento
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Configurar botones de paginación
        previousButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                showPage(currentPage);
            }
        });

        nextButton.setOnAction(event -> {
            if (currentPage < totalPages) {
                currentPage++;
                showPage(currentPage);
            }
        });

        page1Button.setOnAction(event -> showPage(1));
        page2Button.setOnAction(event -> showPage(2));
        page3Button.setOnAction(event -> showPage(3));

        // Calcular el total de páginas
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

        // Mostrar la primera página
        showPage(1);
    }

    private void cargarDepartamentos() {
        departamentoChoiceBox.getItems().add("Todos los departamentos"); // Agregar opción para todos los departamentos
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM departamentos";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                departamentoChoiceBox.getItems().add(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Seleccionar la opción "Todos los departamentos" por defecto
        departamentoChoiceBox.getSelectionModel().selectFirst();
    }

    private void loadAllEntries() throws SQLException {
        employees.clear();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Consulta SQL para obtener todas las entradas/salidas
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "ORDER BY dias.fecha DESC, en.hora_entrada DESC";

        System.out.println("Ejecutando consulta SQL: " + query);

        try {
            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha") != null ? resultSet.getString("fecha") : "");
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada") != null ? resultSet.getString("hora_entrada") : "");
                employeeData.put("horaSalida", resultSet.getString("hora_salida") != null ? resultSet.getString("hora_salida") : "");

                // Cálculo del tiempo laborado
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                if (horaEntrada != null && horaSalida != null) {
                    employeeData.put("tiempoLaborado", calculateTiempoLaborado(horaEntrada, horaSalida));
                } else {
                    employeeData.put("tiempoLaborado", "N/A");
                }

                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia") != null ? resultSet.getString("tipo_asistencia") : "");
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida") != null ? resultSet.getString("tipo_salida") : "");
                employeeData.put("estado", resultSet.getString("estado") != null ? resultSet.getString("estado") : "");

                employees.add(employeeData);
            }

            connectDB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Mostrar la primera página de resultados
        showPage(1);
    }

    private void searchByDateAndDepartment() throws SQLException {
        String departamentoSeleccionado = departamentoChoiceBox.getSelectionModel().getSelectedItem();
        String searchQuery = searchField.getText().trim();  // Obtener el valor del campo de búsqueda

        employees.clear();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Base de la consulta
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id ";

        // Filtros de departamento, supervisores, empleados y búsqueda por nombre/apellido
        boolean filtroDepartamento = !departamentoSeleccionado.equals("Todos los departamentos");
        boolean filtroSupervisores = supervisoresCheckBox.isSelected();
        boolean filtroEmpleados = empleadosCheckBox.isSelected();
        boolean filtroFechas = fechaInicioPicker.getValue() != null && fechaFinPicker.getValue() != null;

        // Agregar las condiciones en función de los filtros seleccionados
        StringBuilder conditions = new StringBuilder();

        if (filtroDepartamento) {
            conditions.append("e.departamento_id IN (SELECT id FROM departamentos WHERE nombre = '").append(departamentoSeleccionado).append("') ");
        }

        if (filtroSupervisores || filtroEmpleados) {
            if (conditions.length() > 0) conditions.append("AND ");
            conditions.append("(");
            if (filtroSupervisores) {
                conditions.append("e.jerarquia_id = 2 ");  // ID de Supervisores
            }
            if (filtroSupervisores && filtroEmpleados) {
                conditions.append("OR ");
            }
            if (filtroEmpleados) {
                conditions.append("e.jerarquia_id = 3 ");  // ID de Empleados
            }
            conditions.append(") ");
        }

        if (!searchQuery.isEmpty()) {
            if (conditions.length() > 0) conditions.append("AND ");
            conditions.append("(e.nombres LIKE '%").append(searchQuery).append("%' OR e.apellido_paterno LIKE '%").append(searchQuery).append("%' OR e.apellido_materno LIKE '%").append(searchQuery).append("%') ");
        }

        if (filtroFechas) {
            if (conditions.length() > 0) conditions.append("AND ");
            String fechaInicioSQL = fechaInicioPicker.getValue().toString();
            String fechaFinSQL = fechaFinPicker.getValue().toString();
            conditions.append("dias.fecha BETWEEN '").append(fechaInicioSQL).append("' AND '").append(fechaFinSQL).append("' ");
        }

        // Si hay condiciones, agregarlas a la consulta
        if (conditions.length() > 0) {
            query += "WHERE " + conditions.toString();
        }

        System.out.println("Ejecutando consulta SQL: " + query);

        try {
            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha") != null ? resultSet.getString("fecha") : "");
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada") != null ? resultSet.getString("hora_entrada") : "");
                employeeData.put("horaSalida", resultSet.getString("hora_salida") != null ? resultSet.getString("hora_salida") : "");

                // Cálculo del tiempo laborado
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                if (horaEntrada != null && horaSalida != null) {
                    employeeData.put("tiempoLaborado", calculateTiempoLaborado(horaEntrada, horaSalida));
                } else {
                    employeeData.put("tiempoLaborado", "N/A");
                }

                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia") != null ? resultSet.getString("tipo_asistencia") : "");
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida") != null ? resultSet.getString("tipo_salida") : "");
                employeeData.put("estado", resultSet.getString("estado") != null ? resultSet.getString("estado") : "");

                employees.add(employeeData);
            }

            connectDB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Mostrar la primera página de resultados
        showPage(1);
    }

    private String calculateTiempoLaborado(String horaEntrada, String horaSalida) {
        return ""; // Implementación del cálculo de tiempo laborado
    }

    @FXML
    private void handleDepartmentSelection() {
        String selectedDepartment = departamentoChoiceBox.getSelectionModel().getSelectedItem();

        employees.clear();

        try {
            DatabaseConnection connectNow = new DatabaseConnection();
            Connection connectDB = connectNow.getConnection();

            // Base de la consulta
            String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                    "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida " +
                    "FROM entradas_salidas en " +
                    "JOIN empleados e ON en.empleado_id = e.id " +
                    "JOIN dias ON en.dia_id = dias.id " +
                    "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                    "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                    "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id ";

            if (!selectedDepartment.equals("Todos los departamentos")) {
                query += "WHERE e.departamento_id IN (SELECT id FROM departamentos WHERE nombre = '" + selectedDepartment + "') ";
            }

            query += "ORDER BY dias.fecha DESC, en.hora_entrada DESC";

            System.out.println("Ejecutando consulta SQL: " + query);

            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha") != null ? resultSet.getString("fecha") : "");
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada") != null ? resultSet.getString("hora_entrada") : "");
                employeeData.put("horaSalida", resultSet.getString("hora_salida") != null ? resultSet.getString("hora_salida") : "");

                // Cálculo del tiempo laborado
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                if (horaEntrada != null && horaSalida != null) {
                    employeeData.put("tiempoLaborado", calculateTiempoLaborado(horaEntrada, horaSalida));
                } else {
                    employeeData.put("tiempoLaborado", "N/A");
                }

                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia") != null ? resultSet.getString("tipo_asistencia") : "");
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida") != null ? resultSet.getString("tipo_salida") : "");
                employeeData.put("estado", resultSet.getString("estado") != null ? resultSet.getString("estado") : "");

                employees.add(employeeData);
            }

            connectDB.close();

            // Actualizar el total de páginas después de la selección del departamento
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Mostrar la primera página de resultados
        showPage(1);
    }

    private Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>> createCellValueFactory(String key) {
        return cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get(key);
            return new SimpleStringProperty(value != null ? value.toString() : "");
        };
    }

    private void showPage(int pageNumber) {
        currentPage = pageNumber;
        int fromIndex = (pageNumber - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, employees.size());

        // Asegúrate de que fromIndex y toIndex están dentro de los límites
        if (fromIndex > toIndex || fromIndex >= employees.size()) {
            fromIndex = 0; // Reinicia la paginación si los índices son inválidos
            toIndex = Math.min(itemsPerPage, employees.size());
        }

        employeeTableView.setItems(FXCollections.observableArrayList(employees.subList(fromIndex, toIndex)));

        // Actualizar botones de paginación
        previousButton.setDisable(pageNumber == 1);
        nextButton.setDisable(pageNumber == totalPages);

        page1Button.setDisable(pageNumber == 1);
        page2Button.setDisable(pageNumber == 2 || totalPages < 2);
        page3Button.setDisable(pageNumber == 3 || totalPages < 3);
    }

    private void toggleGraphView() {
        boolean isTableVisible = employeeTableView.isVisible();
        employeeTableView.setVisible(!isTableVisible);
        chartContainer.setVisible(isTableVisible);

        if (isTableVisible) {
            // Obtener el departamento seleccionado, fechas, y si se incluyen supervisores o empleados
            String departamentoSeleccionado = departamentoChoiceBox.getValue();
            String fechaInicio = fechaInicioPicker.getValue() != null ? fechaInicioPicker.getValue().toString() : "";
            String fechaFin = fechaFinPicker.getValue() != null ? fechaFinPicker.getValue().toString() : "";
            boolean incluirSupervisores = supervisoresCheckBox.isSelected();
            boolean incluirEmpleados = empleadosCheckBox.isSelected();

            // Obtener el valor de búsqueda por nombre
            String searchQuery = searchField.getText();  // Nuevo parámetro de búsqueda

            // Llamada al nuevo método de GraficosController con searchQuery
            graficosController.createBarChart(chartPane, fechaInicio, fechaFin, departamentoSeleccionado, searchQuery, incluirSupervisores, incluirEmpleados);

            highlightButton(graphViewButton);
        } else {
            unhighlightButton(graphViewButton);
        }
    }

    private void highlightButton(Button button) {
        button.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
    }

    private void unhighlightButton(Button button) {
        button.setStyle(""); // Restaurar el estilo predeterminado
    }

    @FXML
    private void handleFilterChange() {
        if (supervisoresCheckBox.isSelected() && empleadosCheckBox.isSelected()) {
            // Si ambos checkboxes están seleccionados, no aplicar filtro
            supervisoresCheckBox.setSelected(false);
            empleadosCheckBox.setSelected(false);
        }

        try {
            searchByDateAndDepartment(); // Aplicar los filtros
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void resetFilters() {
        fechaInicioPicker.setValue(null);
        fechaFinPicker.setValue(null);
        searchField.clear();
        supervisoresCheckBox.setSelected(false);
        empleadosCheckBox.setSelected(false);
        departamentoChoiceBox.getSelectionModel().selectFirst();

        try {
            loadAllEntries(); // Recargar todas las entradas sin filtros
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}