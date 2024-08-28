package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleStringProperty;

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


    private ObservableList<Map<String, Object>> employees = FXCollections.observableArrayList();

    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

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

        // Cargar entradas más recientes por defecto
        try {
            loadRecentEntries();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Configurar el botón de búsqueda
        searchButton.setOnAction(event -> {
            System.out.println("Botón de búsqueda presionado");
            try {
                searchByDateAndDepartment(); // Buscar por fecha y departamento
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Calcular el total de páginas
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

        // Mostrar la primera página
        showPage(currentPage);
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


    private void searchByDateAndDepartment() throws SQLException {
        String departamentoSeleccionado = departamentoChoiceBox.getSelectionModel().getSelectedItem();
        String searchQuery = searchField.getText().trim();  // Obtener el valor del campo de búsqueda

        employees.clear();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Base de la consulta
        String query = "SELECT e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
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

        if (filtroDepartamento || filtroSupervisores || filtroEmpleados || !searchQuery.isEmpty() || filtroFechas) {
            query += "WHERE ";

            if (filtroDepartamento) {
                query += "e.departamento_id IN (SELECT id FROM departamentos WHERE nombre = '" + departamentoSeleccionado + "') ";
            }

            if (filtroSupervisores) {
                if (filtroDepartamento) query += "AND ";
                query += "e.jerarquia_id = 2 ";  // ID de Supervisores
            }

            if (filtroEmpleados) {
                if (filtroDepartamento || filtroSupervisores) query += "AND ";
                query += "e.jerarquia_id = 3 ";  // ID de Empleados
            }

            if (!searchQuery.isEmpty()) {
                if (filtroDepartamento || filtroSupervisores || filtroEmpleados) query += "AND ";
                query += "(e.nombres LIKE '%" + searchQuery + "%' OR e.apellido_paterno LIKE '%" + searchQuery + "%' OR e.apellido_materno LIKE '%" + searchQuery + "%') ";
            }

            if (filtroFechas) {
                if (filtroDepartamento || filtroSupervisores || filtroEmpleados || !searchQuery.isEmpty()) query += "AND ";
                String fechaInicioSQL = fechaInicioPicker.getValue().toString();
                String fechaFinSQL = fechaFinPicker.getValue().toString();
                query += "dias.fecha BETWEEN '" + fechaInicioSQL + "' AND '" + fechaFinSQL + "'";
            }
        }

        System.out.println("Ejecutando consulta SQL: " + query);

        try {
            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("id", resultSet.getInt("estado_id") != 0 ? String.valueOf(resultSet.getInt("estado_id")) : "");
                employeeData.put("fechaEntrada", resultSet.getString("fecha") != null ? resultSet.getString("fecha") : "");
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada") != null ? resultSet.getString("hora_entrada") : "");
                employeeData.put("horaSalida", resultSet.getString("hora_salida") != null ? resultSet.getString("hora_salida") : "");
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




    private Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>> createCellValueFactory(String key) {
        return cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get(key);
            return new SimpleStringProperty(value != null ? value.toString() : "");
        };
    }

    private void showPage(int pageNumber) {
        int fromIndex = (pageNumber - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, employees.size());
        employeeTableView.setItems(FXCollections.observableArrayList(employees.subList(fromIndex, toIndex)));
    }

    private void loadRecentEntries() throws SQLException {
        employees.clear();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Consulta SQL para obtener las entradas más recientes sin importar el día
        String query = "SELECT e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "ORDER BY dias.fecha DESC, en.hora_entrada DESC " +
                "LIMIT 10"; // Puedes ajustar el límite al número de entradas recientes que deseas mostrar

        System.out.println("Ejecutando consulta SQL: " + query);

        try {
            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("id", resultSet.getInt("estado_id") != 0 ? String.valueOf(resultSet.getInt("estado_id")) : "");
                employeeData.put("fechaEntrada", resultSet.getString("fecha") != null ? resultSet.getString("fecha") : "");
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada") != null ? resultSet.getString("hora_entrada") : "");
                employeeData.put("horaSalida", resultSet.getString("hora_salida") != null ? resultSet.getString("hora_salida") : "");
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
}
