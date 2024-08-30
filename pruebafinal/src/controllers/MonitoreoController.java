package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleStringProperty;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

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

        // Cargar entradas más recientes por defecto
        try {
            loadRecentEntries();
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
                    LocalTime entrada = LocalTime.parse(horaEntrada);
                    LocalTime salida = LocalTime.parse(horaSalida);
                    Duration tiempoLaborado = Duration.between(entrada, salida);
                    employeeData.put("tiempoLaborado", String.format("%d:%02d",
                            tiempoLaborado.toHours(),
                            tiempoLaborado.toMinutes() % 60));
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
        employeeTableView.setItems(FXCollections.observableArrayList(employees.subList(fromIndex, toIndex)));

        // Actualizar botones de paginación
        previousButton.setDisable(pageNumber == 1);
        nextButton.setDisable(pageNumber == totalPages);

        page1Button.setDisable(pageNumber == 1);
        page2Button.setDisable(pageNumber == 2 || totalPages < 2);
        page3Button.setDisable(pageNumber == 3 || totalPages < 3);
    }

    private void loadRecentEntries() throws SQLException {
        employees.clear();

        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Consulta SQL para obtener las entradas más recientes sin importar el día
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
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
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha") != null ? resultSet.getString("fecha") : "");
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada") != null ? resultSet.getString("hora_entrada") : "");
                employeeData.put("horaSalida", resultSet.getString("hora_salida") != null ? resultSet.getString("hora_salida") : "");

                // Cálculo del tiempo laborado
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                if (horaEntrada != null && horaSalida != null) {
                    LocalTime entrada = LocalTime.parse(horaEntrada);
                    LocalTime salida = LocalTime.parse(horaSalida);
                    Duration tiempoLaborado = Duration.between(entrada, salida);
                    employeeData.put("tiempoLaborado", String.format("%d:%02d",
                            tiempoLaborado.toHours(),
                            tiempoLaborado.toMinutes() % 60));
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

    private void toggleGraphView() {
        boolean isTableVisible = employeeTableView.isVisible();

        // Ocultar la tabla y mostrar el gráfico, o viceversa
        employeeTableView.setVisible(!isTableVisible);
        employeeTableView.setManaged(!isTableVisible);

        chartContainer.setVisible(isTableVisible);
        chartContainer.setManaged(isTableVisible);

        // Cambiar el estilo del botón según la vista actual
        if (isTableVisible) {
            loadChart(); // Si estamos cambiando a la vista de gráficos, cargar el gráfico
            highlightButton(graphViewButton); // Resaltar el botón de gráficos
        } else {
            unhighlightButton(graphViewButton); // Desactivar el resaltado del botón
        }
    }


    private void highlightButton(Button button) {
        button.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
    }

    private void unhighlightButton(Button button) {
        button.setStyle(""); // Restaurar el estilo predeterminado
    }

    private void loadChart() {
        // Limpiar el Pane del gráfico
        chartPane.getChildren().clear();

        // Crear los ejes del gráfico
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Tipo de Asistencia");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cantidad");

        // Crear el gráfico de barras
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Conteo de Tipos de Asistencia por Departamento");
        barChart.setPrefWidth(600);  // Ajustar el ancho
        barChart.setPrefHeight(800); // Ajustar la altura

        // Crear un mapa para almacenar las series por departamento
        Map<String, XYChart.Series<String, Number>> departmentSeriesMap = new HashMap<>();

        try {
            DatabaseConnection connectNow = new DatabaseConnection();
            Connection connectDB = connectNow.getConnection();

            // Modificar la consulta para obtener datos agrupados por departamento y tipo de asistencia
            String query = "SELECT d.nombre AS departamento, t.nombre AS tipo_asistencia, COUNT(*) AS cantidad " +
                    "FROM entradas_salidas en " +
                    "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                    "JOIN empleados e ON en.empleado_id = e.id " +
                    "JOIN departamentos d ON e.departamento_id = d.id " +
                    "JOIN dias di ON en.dia_id = di.id " +
                    "WHERE di.fecha BETWEEN '" + fechaInicioPicker.getValue() + "' AND '" + fechaFinPicker.getValue() + "' " +
                    "GROUP BY d.nombre, t.nombre";

            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Recorrer los resultados y llenar las series correspondientes
            while (resultSet.next()) {
                String departamento = resultSet.getString("departamento");
                String tipoAsistencia = resultSet.getString("tipo_asistencia");
                int cantidad = resultSet.getInt("cantidad");

                // Crear o actualizar la serie para el departamento actual
                XYChart.Series<String, Number> series = departmentSeriesMap.computeIfAbsent(departamento, k -> {
                    XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                    newSeries.setName(k);
                    barChart.getData().add(newSeries);
                    return newSeries;
                });

                // Añadir los datos a la serie correspondiente
                XYChart.Data<String, Number> data = new XYChart.Data<>(tipoAsistencia, cantidad);
                series.getData().add(data);

                // Asegurarse de que el nodo esté disponible antes de trabajar con él
                data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        // Añadir la etiqueta dentro de la barra
                        Tooltip.install(newValue, new Tooltip(String.valueOf(cantidad)));
                        Label label = new Label(String.valueOf(cantidad));
                        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                        StackPane stackPane = (StackPane) newValue;
                        stackPane.getChildren().add(label);

                        // Añadir evento de clic en la barra
                        newValue.setOnMouseClicked(event -> {
                            showDetails(departamento, tipoAsistencia);
                        });
                    }
                });
            }

            // Crear una serie para el total general si se seleccionaron todos los departamentos
            if ("Todos los departamentos".equals(departamentoChoiceBox.getValue())) {
                XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
                totalSeries.setName("Total General");

                // Crear una consulta para el total general
                String totalQuery = "SELECT t.nombre AS tipo_asistencia, COUNT(*) AS cantidad " +
                        "FROM entradas_salidas en " +
                        "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                        "JOIN dias di ON en.dia_id = di.id " +
                        "WHERE di.fecha BETWEEN '" + fechaInicioPicker.getValue() + "' AND '" + fechaFinPicker.getValue() + "' " +
                        "GROUP BY t.nombre";

                ResultSet totalResultSet = statement.executeQuery(totalQuery);
                while (totalResultSet.next()) {
                    String tipoAsistencia = totalResultSet.getString("tipo_asistencia");
                    int cantidad = totalResultSet.getInt("cantidad");

                    XYChart.Data<String, Number> totalData = new XYChart.Data<>(tipoAsistencia, cantidad);
                    totalSeries.getData().add(totalData);

                    // Asegurarse de que el nodo esté disponible antes de trabajar con él
                    totalData.nodeProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            // Añadir la etiqueta dentro de la barra
                            Tooltip.install(newValue, new Tooltip(String.valueOf(cantidad)));
                            Label label = new Label(String.valueOf(cantidad));
                            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                            StackPane stackPane = (StackPane) newValue;
                            stackPane.getChildren().add(label);

                            // Añadir evento de clic en la barra
                            newValue.setOnMouseClicked(event -> {
                                showDetails("Todos los departamentos", tipoAsistencia);
                            });
                        }
                    });
                }

                barChart.getData().add(totalSeries);
            }

            connectDB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Añadir el gráfico al Pane
        chartPane.getChildren().add(barChart);
    }


    private void showDetails(String departamento, String tipoAsistencia) {
        detailsListView.getItems().clear(); // Limpiar detalles previos

        // Consulta para obtener los detalles de las personas en la barra seleccionada
        try {
            DatabaseConnection connectNow = new DatabaseConnection();
            Connection connectDB = connectNow.getConnection();

            String query = "SELECT e.nombres, e.apellido_paterno, e.apellido_materno, en.fecha " +
                    "FROM entradas_salidas en " +
                    "JOIN empleados e ON en.empleado_id = e.id " +
                    "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                    "JOIN departamentos d ON e.departamento_id = d.id " +
                    "WHERE d.nombre = '" + departamento + "' AND t.nombre = '" + tipoAsistencia + "'";

            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            ObservableList<String> items = FXCollections.observableArrayList();

            while (resultSet.next()) {
                String nombreCompleto = resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno");
                String fecha = resultSet.getString("fecha");
                items.add(nombreCompleto + " - " + fecha);
            }

            detailsListView.setItems(items);

            // Añadir evento de clic en la persona para mostrar detalles adicionales
            detailsListView.setOnMouseClicked(event -> {
                String selectedItem = detailsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    System.out.println("Seleccionado: " + selectedItem);
                    // Aquí puedes añadir más lógica para mostrar detalles adicionales si lo deseas
                }
            });

            connectDB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
