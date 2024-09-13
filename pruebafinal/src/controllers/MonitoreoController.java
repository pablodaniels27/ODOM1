package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;


import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import javafx.scene.control.cell.PropertyValueFactory;


import java.util.HashSet;
import java.util.Set;


public class MonitoreoController {

    @FXML
    private TableView<Fecha> dateTableView;

    @FXML
    private TableColumn<Fecha, String> fechaColumn;

    @FXML
    private TableView<Empleado> personTableView;  // Cambiamos ListView a TableView

    @FXML
    private TableColumn<Empleado, String> nombreCompletoColumn;

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
    private VBox detailsPane;

    @FXML
    private HBox paginationBox;
    @FXML
    private HBox chartContainer;

    @FXML
    private Label asistenciaLabel;



    private String tipoAsistenciaSeleccionado = "Asistencia"; // Inicializar con un valor por defecto

    private ObservableList<Map<String, Object>> employees = FXCollections.observableArrayList();

    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    private GraficosController graficosController;


    @FXML
    public void initialize() {
        // Vincular la columna con la propiedad "nombreCompleto"
        nombreCompletoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Listener para cuando se selecciona un empleado en el personTableView
        personTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Obtener el nombre del empleado seleccionado
                String nombreEmpleado = newSelection.getNombreCompleto();
                System.out.println("Empleado seleccionado: " + nombreEmpleado);

                // Obtener el tipo de asistencia seleccionado actualmente
                String tipoAsistencia = obtenerTipoAsistenciaSeleccionado();
                System.out.println("Tipo de asistencia obtenido: " + tipoAsistencia);

                // Obtener el departamento seleccionado
                String departamentoSeleccionado = obtenerDepartamentoSeleccionado();

                // Verificar si los DatePicker no están vacíos
                if (fechaInicioPicker.getValue() != null && fechaFinPicker.getValue() != null) {
                    String fechaInicio = fechaInicioPicker.getValue().toString();
                    String fechaFin = fechaFinPicker.getValue().toString();

                    // Llamar al método para mostrar las fechas filtradas por rango y empleado
                    mostrarFechasPorEmpleado(departamentoSeleccionado, tipoAsistencia, nombreEmpleado, fechaInicio, fechaFin);
                } else {
                    System.out.println("Por favor, selecciona un rango de fechas válido.");
                }
            }
        });

        graficosController = new GraficosController();
        graficosController.setMonitoreoController(this);

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
        itemsPerPageChoiceBox.setItems(FXCollections.observableArrayList(10, 20, 30, 40));
        itemsPerPageChoiceBox.setValue(itemsPerPage);
        itemsPerPageChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            itemsPerPage = newVal;
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
            showPage(1); // Mostrar la primera página con los nuevos ítems por página
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

        // Eliminar las referencias a page1Button, page2Button y page3Button, ya que ahora se generan dinámicamente.

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
        // Formato esperado: "HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        try {
            // Parsear las horas de entrada y salida
            LocalTime entrada = LocalTime.parse(horaEntrada, formatter);
            LocalTime salida = LocalTime.parse(horaSalida, formatter);

            // Calcular la duración entre la hora de entrada y la hora de salida
            Duration duracion = Duration.between(entrada, salida);

            // Convertir la duración a horas y minutos
            long horas = duracion.toHours();
            long minutos = duracion.toMinutes() % 60;

            // Devolver la duración en el formato "X horas Y minutos"
            return String.format("%d horas %d minutos", horas, minutos);

        } catch (DateTimeParseException e) {
            // Manejar el caso en que el formato de la hora no sea válido
            e.printStackTrace();
            return "Formato de hora inválido";
        }
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

    @FXML
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

        // Actualizar botones de paginación dinámicamente
        updatePaginationButtons();

        // Actualizar botones de navegación
        previousButton.setDisable(pageNumber == 1);
        nextButton.setDisable(pageNumber == totalPages);
    }

    private void updatePaginationButtons() {
        paginationBox.getChildren().clear(); // Limpiar los botones anteriores
        int visiblePages = 5; // Número de páginas visibles a la vez

        // Determinar el rango de páginas visibles
        int startPage = Math.max(2, currentPage - visiblePages / 2);
        int endPage = Math.min(startPage + visiblePages - 1, totalPages - 1);

        // Ajustar el rango si estamos cerca del inicio o final
        if (endPage - startPage + 1 < visiblePages) {
            startPage = Math.max(2, endPage - visiblePages + 1);
        }

        // Mostrar siempre el botón de la primera página
        Button firstPageButton = new Button("1");
        firstPageButton.setOnAction(event -> showPage(1));
        if (currentPage == 1) {
            firstPageButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        } else {
            firstPageButton.setStyle("");
        }
        paginationBox.getChildren().add(firstPageButton);

        // Agregar puntos suspensivos si el rango de páginas visibles no incluye la página 2
        if (startPage > 2) {
            paginationBox.getChildren().add(new Label("..."));
        }

        // Crear los botones de las páginas visibles
        for (int i = startPage; i <= endPage; i++) {
            final int pageIndex = i; // Crear una variable final para usarla dentro del lambda
            Button pageButton = new Button(String.valueOf(i));
            pageButton.setOnAction(event -> showPage(pageIndex));

            // Establecer el estilo del botón actual
            if (i == currentPage) {
                pageButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
            } else {
                pageButton.setStyle("");
            }

            paginationBox.getChildren().add(pageButton); // Añadir el botón al HBox
        }

        // Agregar puntos suspensivos si el rango de páginas visibles no incluye la penúltima página
        if (endPage < totalPages - 1) {
            paginationBox.getChildren().add(new Label("..."));
        }

        // Mostrar siempre el botón de la última página
        Button lastPageButton = new Button(String.valueOf(totalPages));
        lastPageButton.setOnAction(event -> showPage(totalPages));
        if (currentPage == totalPages) {
            lastPageButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        } else {
            lastPageButton.setStyle("");
        }
        paginationBox.getChildren().add(lastPageButton);
    }

    @FXML
    private void toggleGraphView() {
        // Alternar la visibilidad de la tabla y el gráfico
        boolean isTableVisible = employeeTableView.isVisible();
        employeeTableView.setVisible(!isTableVisible);
        employeeTableView.setManaged(!isTableVisible); // Esto asegura que el espacio se gestiona correctamente
        chartContainer.setVisible(isTableVisible);
        chartContainer.setManaged(isTableVisible);

        if (isTableVisible) {
            // Obtener los valores seleccionados de los filtros
            String departamentoSeleccionado = departamentoChoiceBox.getValue();
            String fechaInicio = fechaInicioPicker.getValue() != null ? fechaInicioPicker.getValue().toString() : "";
            String fechaFin = fechaFinPicker.getValue() != null ? fechaFinPicker.getValue().toString() : "";
            boolean incluirSupervisores = supervisoresCheckBox.isSelected();
            boolean incluirEmpleados = empleadosCheckBox.isSelected();

            // Obtener el valor de búsqueda por nombre
            String searchQuery = searchField.getText();

            // Generar la gráfica en el chartPane
            graficosController.createBarChart(chartPane, fechaInicio, fechaFin, departamentoSeleccionado, searchQuery, incluirSupervisores, incluirEmpleados);

            highlightButton(graphViewButton); // Mantener resaltado el botón de gráfica
        } else {
            unhighlightButton(graphViewButton); // Remover resaltado cuando no está en vista gráfica
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
        boolean soloSupervisores = supervisoresCheckBox.isSelected();
        boolean soloEmpleados = empleadosCheckBox.isSelected();

        // Si ambos checkboxes están seleccionados, no aplicar filtro
        if (soloSupervisores && soloEmpleados) {
            supervisoresCheckBox.setSelected(false);
            empleadosCheckBox.setSelected(false);
        }

        try {
            // Actualizar la tabla de empleados
            searchByDateAndDepartment(); // Filtra los empleados por el departamento, tipo de asistencia y rango de fechas

            // Actualizar la gráfica
            if (chartContainer.isVisible()) {
                String departamentoSeleccionado = departamentoChoiceBox.getValue();
                String fechaInicio = fechaInicioPicker.getValue() != null ? fechaInicioPicker.getValue().toString() : "";
                String fechaFin = fechaFinPicker.getValue() != null ? fechaFinPicker.getValue().toString() : "";
                String searchQuery = searchField.getText();

                // Generar la gráfica actualizada
                graficosController.createBarChart(chartPane, fechaInicio, fechaFin, departamentoSeleccionado, searchQuery, soloSupervisores, soloEmpleados);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void resetFilters() {
        // Resetear todos los filtros visuales
        fechaInicioPicker.setValue(null);
        fechaFinPicker.setValue(null);
        searchField.clear();
        supervisoresCheckBox.setSelected(false);
        empleadosCheckBox.setSelected(false);
        departamentoChoiceBox.getSelectionModel().selectFirst(); // Resetear el ChoiceBox al valor por defecto

        // Si estamos en la vista de gráficos, cambiar de nuevo a la vista de tabla
        if (!employeeTableView.isVisible()) {
            toggleGraphView(); // Volver a la vista de tabla si estamos en gráficos
        }

        // Cargar los registros más recientes (esto ya está en el método loadAllEntries)
        try {
            loadAllEntries(); // Recargar las entradas más recientes sin filtros
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TABLAS DE NOMBRES Y FECHAS

    public void mostrarNombresPorAsistencia(String departamento, String tipoAsistencia, String searchQuery) {
        System.out.println("Método mostrarNombresPorAsistencia llamado con Departamento: " + departamento + ", Tipo de Asistencia: " + tipoAsistencia + ", Filtro de búsqueda: " + searchQuery);

        // Usar un Set para evitar duplicados
        Set<String> empleadosUnicos = new HashSet<>();

        // Consulta SQL base para obtener los nombres de las personas, incluyendo filtro por fechas
        String query = "SELECT e.nombres, e.apellido_paterno, e.apellido_materno " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "JOIN dias ON en.dia_id = dias.id " + // Relacionar con la tabla de días para el rango de fechas
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "WHERE d.nombre = ? AND t.nombre = ? " +  // Filtrar por departamento y tipo de asistencia
                "AND dias.fecha BETWEEN ? AND ? ";  // Filtro por rango de fechas

        // Añadir condiciones adicionales para supervisores o empleados
        if (supervisoresCheckBox.isSelected() || empleadosCheckBox.isSelected()) {
            query += " AND (";
            if (supervisoresCheckBox.isSelected()) {
                query += "e.jerarquia_id = 2";  // Filtrar supervisores (jerarquia_id = 2)
            }
            if (supervisoresCheckBox.isSelected() && empleadosCheckBox.isSelected()) {
                query += " OR ";  // Si ambos están seleccionados, permitir OR
            }
            if (empleadosCheckBox.isSelected()) {
                query += "e.jerarquia_id = 3";  // Filtrar empleados (jerarquia_id = 3)
            }
            query += ")";
        }

        // Si hay una búsqueda, agregar el filtro al query
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query += " AND (e.nombres LIKE ? OR e.apellido_paterno LIKE ? OR e.apellido_materno LIKE ?)";
        }

        try (Connection connectDB = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {

            // Asignar los parámetros de la consulta
            preparedStatement.setString(1, departamento);
            preparedStatement.setString(2, tipoAsistencia);

            // Obtener los valores del rango de fechas
            String fechaInicio = (fechaInicioPicker.getValue() != null) ? fechaInicioPicker.getValue().toString() : "1900-01-01"; // Valor por defecto si no hay fecha seleccionada
            String fechaFin = (fechaFinPicker.getValue() != null) ? fechaFinPicker.getValue().toString() : "2100-12-31"; // Valor por defecto si no hay fecha seleccionada

            // Asignar las fechas a los parámetros de la consulta
            preparedStatement.setString(3, fechaInicio);
            preparedStatement.setString(4, fechaFin);

            // Si hay un valor de búsqueda, agregarlo como parámetro
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.trim() + "%";
                preparedStatement.setString(5, searchPattern);
                preparedStatement.setString(6, searchPattern);
                preparedStatement.setString(7, searchPattern);
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String nombreCompleto = resultSet.getString("nombres") + " " +
                        resultSet.getString("apellido_paterno") + " " +
                        resultSet.getString("apellido_materno");

                // Agregar al Set, que no permitirá duplicados
                empleadosUnicos.add(nombreCompleto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convertir el Set en una lista observable para el TableView
        ObservableList<Empleado> empleados = FXCollections.observableArrayList();
        for (String nombre : empleadosUnicos) {
            empleados.add(new Empleado(nombre));  // Agregar el nombre único al TableView
        }

        // Asignar los datos al TableView
        personTableView.setItems(empleados);

        System.out.println("Nombres únicos añadidos al TableView: " + empleados.size());
    }

    // Clase auxiliar para representar empleados
    public static class Empleado {
        private final String nombreCompleto;

        public Empleado(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }
    }

    public String getSearchFieldText() {
        return (searchField != null) ? searchField.getText().trim() : "";  // Si searchField no está enlazado correctamente, este valor será null
    }

    // Clase auxiliar para representar las fechas
    public static class Fecha {
        private final String fecha;

        public Fecha(String fecha) {
            this.fecha = fecha;
        }

        public String getFecha() {
            return fecha;
        }
    }

    // Método para mostrar las fechas en las que un empleado tuvo una asistencia específica
    public void mostrarFechasPorEmpleado(String departamento, String tipoAsistencia, String nombreEmpleado, String fechaInicio, String fechaFin) {
        asistenciaLabel.setText("Tipo de Asistencia: " + tipoAsistencia);
        System.out.println("Mostrando fechas para el empleado: " + nombreEmpleado + ", Departamento: " + departamento + ", Tipo de Asistencia: " + tipoAsistencia + ", Fecha Inicio: " + fechaInicio + ", Fecha Fin: " + fechaFin);

        // Crear un Set para evitar duplicados
        Set<String> fechasUnicas = new HashSet<>();

        // Base de la consulta SQL para obtener las fechas del empleado
        String query = "SELECT dias.fecha " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "WHERE t.nombre = ? " + // Filtro por tipo de asistencia
                "AND (e.nombres = ? AND e.apellido_paterno = ? AND e.apellido_materno = ?) " + // Filtrar por nombre completo
                "AND dias.fecha BETWEEN ? AND ?";  // Filtrar por rango de fechas

        // Si el departamento no es "Todos los departamentos", agregar el filtro de departamento
        if (!departamento.equals("Todos los departamentos")) {
            query += " AND d.nombre = ?";
        }

        try (Connection connectDB = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {

            System.out.println("Ejecutando consulta SQL: " + query);

            // Asignar los parámetros
            preparedStatement.setString(1, tipoAsistencia);

            // Separar el nombre completo en partes: nombres, apellido paterno y materno
            String[] partesNombre = nombreEmpleado.split(" ");
            if (partesNombre.length < 3) {
                System.out.println("Nombre completo no válido: " + nombreEmpleado);
                return; // Detener si el nombre no tiene al menos 3 partes
            }
            preparedStatement.setString(2, partesNombre[0]); // Nombres
            preparedStatement.setString(3, partesNombre[1]); // Apellido Paterno
            preparedStatement.setString(4, partesNombre[2]); // Apellido Materno

            preparedStatement.setString(5, fechaInicio);  // Fecha de inicio
            preparedStatement.setString(6, fechaFin);     // Fecha de fin

            // Si el departamento no es "Todos los departamentos", asignar también el valor del departamento
            if (!departamento.equals("Todos los departamentos")) {
                preparedStatement.setString(7, departamento);
            }

            System.out.println("Parámetros asignados: ");
            System.out.println("Departamento: " + (departamento.equals("Todos los departamentos") ? "Todos" : departamento));
            System.out.println("Tipo de Asistencia: " + tipoAsistencia);
            System.out.println("Nombre del Empleado: " + nombreEmpleado);
            System.out.println("Fecha Inicio: " + fechaInicio);
            System.out.println("Fecha Fin: " + fechaFin);

            ResultSet resultSet = preparedStatement.executeQuery();
            int counter = 0;
            while (resultSet.next()) {
                String fecha = resultSet.getString("fecha");
                fechasUnicas.add(fecha);  // Añadir al Set para evitar duplicados
                counter++;
                System.out.println("Fecha encontrada: " + fecha);
            }

            if (counter == 0) {
                System.out.println("No se encontraron fechas para el empleado: " + nombreEmpleado + " con el tipo de asistencia: " + tipoAsistencia);
            } else {
                System.out.println("Total de fechas encontradas: " + counter);
            }

        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta SQL para obtener fechas: ");
            e.printStackTrace();
        }

        // Convertir el Set en una lista observable para el TableView
        ObservableList<Fecha> fechas = FXCollections.observableArrayList();
        for (String fecha : fechasUnicas) {
            fechas.add(new Fecha(fecha));
        }

        // Asignar los datos al dateTableView
        dateTableView.setItems(fechas);
        System.out.println("Fechas asignadas al TableView");
    }



    public String obtenerDepartamentoSeleccionado() {
        // Aquí puedes retornar el departamento seleccionado
        // Esto depende de cómo el usuario seleccione o filtre por el departamento en tu aplicación
        return departamentoChoiceBox.getSelectionModel().getSelectedItem(); // Ejemplo usando ChoiceBox
    }

    public String obtenerTipoAsistenciaSeleccionado() {
        System.out.println("Tipo de asistencia seleccionado: " + tipoAsistenciaSeleccionado);
        return tipoAsistenciaSeleccionado;
    }

    public void setTipoAsistenciaSeleccionado(String tipoAsistencia) {
        this.tipoAsistenciaSeleccionado = tipoAsistencia;
    }
    public void updateAsistenciaLabel(String tipoAsistencia) {
        asistenciaLabel.setText("Tipo de Asistencia: " + tipoAsistencia);
    }
    public void clearDateTableView() {
        dateTableView.getItems().clear(); // Esto vacía la tabla de fechas
    }



}