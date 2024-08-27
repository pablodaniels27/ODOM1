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
    private Button previousButton;

    @FXML
    private Button page1Button;

    @FXML
    private Button page2Button;

    @FXML
    private Button page3Button;

    @FXML
    private Button nextButton;

    @FXML
    private ChoiceBox<Integer> itemsPerPageChoiceBox;

    private ObservableList<Map<String, Object>> employees = FXCollections.observableArrayList();

    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        System.out.println("Inicializando controlador...");

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

        // Configurar el ChoiceBox de cantidad de datos por página
        itemsPerPageChoiceBox.setItems(FXCollections.observableArrayList(10, 20, 50, 100, 200));
        itemsPerPageChoiceBox.setValue(itemsPerPage);
        itemsPerPageChoiceBox.setOnAction(event -> {
            System.out.println("Cambio en items por página");
            itemsPerPage = itemsPerPageChoiceBox.getValue();
            currentPage = 1;
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
            showPage(currentPage);
            updatePaginationButtons();
        });

        // Configurar el botón de búsqueda
        searchButton.setOnAction(event -> {
            System.out.println("Botón de búsqueda presionado");
            try {
                searchByDate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Calcular el total de páginas
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

        // Mostrar la primera página
        showPage(currentPage);

        // Configurar los botones de paginación
        configurePaginationButtons();

        // Ajustar el ancho de las columnas al contenido
        adjustColumnWidths();

        System.out.println("Inicialización completada");
    }

    private Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>> createCellValueFactory(String key) {
        return cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get(key);
            return new SimpleStringProperty(value != null ? value.toString() : "");  // Manejo de valores nulos
        };
    }

    private void searchByDate() throws SQLException {
        System.out.println("Iniciando búsqueda por fecha...");
        LocalDate fechaInicio = fechaInicioPicker.getValue();
        LocalDate fechaFin = fechaFinPicker.getValue();

        if (fechaInicio != null && fechaFin != null) {
            System.out.println("Fechas seleccionadas: " + fechaInicio + " a " + fechaFin);

            // Limpiar la lista actual de empleados
            employees.clear();

            // Convertir las fechas a formato SQL
            String fechaInicioSQL = fechaInicio.toString();
            String fechaFinSQL = fechaFin.toString();

            DatabaseConnection connectNow = new DatabaseConnection();
            Connection connectDB = connectNow.getConnection();

            String query = "SELECT e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                    "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida " +
                    "FROM entradas_salidas en " +
                    "JOIN empleados e ON en.empleado_id = e.id " +
                    "JOIN dias ON en.dia_id = dias.id " +
                    "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                    "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                    "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                    "WHERE dias.fecha BETWEEN '" + fechaInicioSQL + "' AND '" + fechaFinSQL + "'";

            System.out.println("Ejecutando consulta SQL: " + query);

            try {
                Statement statement = connectDB.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                int totalRegistros = 0;

                while (resultSet.next()) {
                    totalRegistros++;
                    System.out.println("Resultado obtenido de la base de datos");

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

                System.out.println("Número total de registros obtenidos: " + totalRegistros);

                connectDB.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Mostrar la primera página de resultados
            showPage(1);
            updatePaginationButtons();
        } else {
            System.out.println("Fechas no seleccionadas");
            // Si no se seleccionan fechas, mostrar un mensaje de error o advertencia
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Fechas no seleccionadas");
            alert.setContentText("Por favor, selecciona un rango de fechas válido.");
            alert.showAndWait();
        }
    }

    private void showPage(int pageNumber) {
        System.out.println("Mostrando página: " + pageNumber);
        int fromIndex = (pageNumber - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, employees.size());
        System.out.println("Mostrando registros desde " + fromIndex + " hasta " + toIndex);
        employeeTableView.setItems(FXCollections.observableArrayList(employees.subList(fromIndex, toIndex)));
    }

    private void adjustColumnWidths() {
        System.out.println("Ajustando ancho de columnas");
        employeeTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        employeeTableView.getColumns().forEach(column -> {
            column.setMinWidth(column.getText().length() * 13);
            column.setPrefWidth(Control.USE_COMPUTED_SIZE);
            column.setResizable(true);
        });
    }

    private void configurePaginationButtons() {
        previousButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                showPage(currentPage);
                updatePaginationButtons();
            }
        });

        nextButton.setOnAction(event -> {
            if (currentPage < totalPages) {
                currentPage++;
                showPage(currentPage);
                updatePaginationButtons();
            }
        });

        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        page1Button.setText(String.valueOf(currentPage));
        page1Button.setOnAction(event -> showPage(currentPage));

        if (currentPage + 1 <= totalPages) {
            page2Button.setText(String.valueOf(currentPage + 1));
            page2Button.setVisible(true);
            page2Button.setOnAction(event -> {
                currentPage++;
                showPage(currentPage);
                updatePaginationButtons();
            });
        } else {
            page2Button.setVisible(false);
        }

        if (currentPage + 2 <= totalPages) {
            page3Button.setText(String.valueOf(currentPage + 2));
            page3Button.setVisible(true);
            page3Button.setOnAction(event -> {
                currentPage += 2;
                showPage(currentPage);
                updatePaginationButtons();
            });
        } else {
            page3Button.setVisible(false);
        }
    }
}
