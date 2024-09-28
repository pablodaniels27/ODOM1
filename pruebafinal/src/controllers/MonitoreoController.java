package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.Modality;
import javafx.util.Callback;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.ContextMenu;

import java.util.Map;

import static Usuarios.Supervisor.getCurrentSupervisorId;

import javafx.scene.input.KeyEvent;

public class MonitoreoController {

    @FXML
    private TableView<Fecha> dateTableView;

    @FXML
    private TableColumn<Fecha, String> fechaColumn;

    @FXML
    private TableView<Empleado> personTableView;

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
    private HBox paginationBox;
    @FXML
    private HBox chartContainer;

    @FXML
    private Label asistenciaLabel;

    @FXML
    private TableColumn<Map<String, Object>, String> notasColum;

    @FXML
    private Label selectedDepartmentLabel;

    private String tipoAsistenciaSeleccionado = "Asistencia"; // Inicializar con un valor por defecto

    private ObservableList<Map<String, Object>> employees = FXCollections.observableArrayList();

    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    private GraficosController graficosController;

    private ContextMenu suggestionsMenu = new ContextMenu();

    @FXML
    public void initialize() {

        // Configurar el evento del searchField para capturar teclas presionadas
        configureSearchField();

        // Configurar la columna de notas con un enlace y estilo para mostrar un popup
        notasColum.setCellFactory(tc -> new TableCell<Map<String, Object>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null); // No hay nota, dejar vacío
                } else {
                    setText("ver nota");
                    setStyle("-fx-text-fill: blue; -fx-underline: true;"); // Estilo tipo enlace
                    setOnMouseClicked(event -> {
                        if (!isEmpty()) {
                            showNotePopup(item); // Mostrar el popup con la nota
                        }
                    });
                }
            }
        });

        // Vincular datos de la columna de notas
        notasColum.setCellValueFactory(createCellValueFactory("notas"));

        // Configurar las columnas de nombre completo y fecha
        nombreCompletoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Verificar que la columna tipoAsistenciaColumn no sea null
        if (tipoAsistenciaColumn == null) {
            System.out.println("Error: tipoAsistenciaColumn es null.");
            return;
        } else {
            System.out.println("tipoAsistenciaColumn se cargó correctamente.");
        }

        // Listener para seleccionar un empleado en la tabla personTableView
        personTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String nombreEmpleado = newSelection.getNombreCompleto();
                System.out.println("Empleado seleccionado: " + nombreEmpleado);

                String tipoAsistencia = obtenerTipoAsistenciaSeleccionado();
                String departamentoSeleccionado = obtenerDepartamentoSeleccionado();

                if (fechaInicioPicker.getValue() != null && fechaFinPicker.getValue() != null) {
                    String fechaInicio = fechaInicioPicker.getValue().toString();
                    String fechaFin = fechaFinPicker.getValue().toString();

                    // Mostrar las fechas filtradas por rango y empleado
                    mostrarFechasPorEmpleado(departamentoSeleccionado, tipoAsistencia, nombreEmpleado, fechaInicio, fechaFin);
                } else {
                    System.out.println("Por favor, selecciona un rango de fechas válido.");
                }
            }
        });

        // Configurar el controlador de gráficos
        graficosController = new GraficosController();
        graficosController.setMonitoreoController(this);

        // Configurar los CheckBoxes para supervisores y empleados
        supervisoresCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                empleadosCheckBox.setSelected(false); // Desmarcar empleadosCheckBox
            }
        });
        empleadosCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                supervisoresCheckBox.setSelected(false); // Desmarcar supervisoresCheckBox
            }
        });

        // Configurar columnas de la tabla de empleados
        nombreColumn.setCellValueFactory(createCellValueFactory("nombreCompleto"));
        idColumn.setCellValueFactory(createCellValueFactory("id"));
        fechaEntradaColumn.setCellValueFactory(createCellValueFactory("fechaEntrada"));
        horaEntradaColumn.setCellValueFactory(createCellValueFactory("horaEntrada"));
        horaSalidaColumn.setCellValueFactory(createCellValueFactory("horaSalida"));
        tiempoLaboradoColumn.setCellValueFactory(createCellValueFactory("tiempoLaborado"));
        tipoAsistenciaColumn.setCellValueFactory(createCellValueFactory("tipoAsistencia"));
        tipoSalidaColumn.setCellValueFactory(createCellValueFactory("tipoSalida"));
        estadoColumn.setCellValueFactory(createCellValueFactory("estado"));

        // Asignar los empleados a la tabla employeeTableView
        employeeTableView.setItems(employees);

        // Cargar departamentos en el ChoiceBox
        cargarDepartamentos();
        departamentoChoiceBox.getSelectionModel().selectedItemProperty();

        // Configurar el ChoiceBox para seleccionar la cantidad de ítems por página
        itemsPerPageChoiceBox.setItems(FXCollections.observableArrayList(10, 20, 30, 40));
        itemsPerPageChoiceBox.setValue(itemsPerPage);
        itemsPerPageChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            itemsPerPage = newVal;
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
            showPage(1); // Mostrar la primera página
        });

        // Configurar el comportamiento del botón de gráficos
        graphViewButton.setOnAction(event -> toggleGraphView());

        // Cargar todos los registros al inicio
        try {
            employees.clear();
            loadAllEntries();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Configurar el botón de búsqueda
        searchButton.setOnAction(event -> {
            try {
                if (fechaInicioPicker.getValue() == null || fechaFinPicker.getValue() == null) {
                    // Mostrar una alerta si falta alguna fecha
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Advertencia");
                    alert.setHeaderText("Faltan fechas");
                    alert.setContentText("Por favor, selecciona una fecha de inicio y una fecha final.");
                    alert.showAndWait();
                    return;
                }

                // Limpiar las selecciones y los datos de las tablas
                personTableView.getSelectionModel().clearSelection();
                dateTableView.getSelectionModel().clearSelection();
                personTableView.getItems().clear();
                dateTableView.getItems().clear();

                // Obtener los valores de los filtros
                String departamentoSeleccionado = departamentoChoiceBox.getValue();
                if (departamentoSeleccionado != null) {
                    selectedDepartmentLabel.setText("Departamento: " + departamentoSeleccionado);
                }

                String searchQuery = searchField.getText().trim();
                boolean incluirSupervisores = supervisoresCheckBox.isSelected();
                boolean incluirEmpleados = empleadosCheckBox.isSelected();
                String fechaInicio = fechaInicioPicker.getValue() != null ? fechaInicioPicker.getValue().toString() : "";
                String fechaFin = fechaFinPicker.getValue() != null ? fechaFinPicker.getValue().toString() : "";

                // Buscar y cargar datos en la tabla
                searchByDateAndDepartment(departamentoSeleccionado, searchQuery, incluirSupervisores, incluirEmpleados, fechaInicio, fechaFin);

                // Actualizar el gráfico con los filtros seleccionados
                graficosController.createBarChart(chartPane, fechaInicio, fechaFin, departamentoSeleccionado, searchQuery, incluirSupervisores, incluirEmpleados);

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

        // Calcular el total de páginas y mostrar la primera página
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
        showPage(1);

        // Configurar el tipo de asistencia con evento para cambiarlo
        tipoAsistenciaColumn.setCellFactory(tc -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    Map<String, Object> employeeData = employeeTableView.getItems().get(cell.getIndex());
                    System.out.println("Tipo de asistencia clickeado: " + employeeData.get("tipoAsistencia"));
                    showTipoAsistenciaPopup(employeeData); // Mostrar el popup para cambiar el tipo de asistencia
                }
            });

            return cell;
        });
    }

    private void configureSearchField() {
        searchField.setOnKeyReleased(this::searchForNames);
    }


    private void showNotePopup(String nota) {
        // Crear un nuevo dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nota");
        alert.setHeaderText("Detalles de la Nota:");
        alert.setContentText(nota);

        // Hacer que el diálogo sea modal
        alert.initModality(Modality.APPLICATION_MODAL);

        // Mostrar el diálogo
        alert.showAndWait();
    }


    private void showTipoAsistenciaPopup(Map<String, Object> employeeData) {
        // Crear el Dialog
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Tipo de Asistencia");

        // Crear el ChoiceBox con las opciones
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Asistencia", "No Asistencia", "Retardo", "Justificación");

        // Crear un TextArea para las notas
        TextArea notasTextArea = new TextArea();
        notasTextArea.setPromptText("Escribe la razón del cambio...");

        // Crear un VBox para añadir ambos elementos
        VBox vBox = new VBox(10, new Label("Tipo de Asistencia"), choiceBox, new Label("Notas"), notasTextArea);
        dialog.getDialogPane().setContent(vBox);

        // Añadir botones de OK y Cancelar
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Procesar el resultado cuando se hace clic en OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new String[]{choiceBox.getValue(), notasTextArea.getText()}; // Devolver el tipo de asistencia y las notas
            }
            return null;
        });

        // Mostrar el Dialog y esperar por la respuesta
        Optional<String[]> result = dialog.showAndWait();

        result.ifPresent(response -> {
            String newTipoAsistencia = response[0];
            String notas = response[1];

            // Actualizar el tipo de asistencia en los datos de la tabla
            employeeData.put("tipoAsistencia", newTipoAsistencia);
            employeeData.put("notas", notas); // Actualizar también las notas

            // Llamar a la base de datos para actualizar el tipo de asistencia y guardar las notas
            try (Connection connection = DatabaseConnection.getConnection()) {
                // Obtener el id correspondiente al tipo de asistencia seleccionado
                String query = "SELECT id FROM tipos_asistencia WHERE nombre = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, newTipoAsistencia);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int tipoAsistenciaId = resultSet.getInt("id");

                    // Actualizar en la tabla entradas_salidas SOLO para la fecha seleccionada
                    String updateQuery = "UPDATE entradas_salidas SET tipo_asistencia_id = ? WHERE empleado_id = ? AND dia_id = (SELECT id FROM dias WHERE fecha = ?)";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setInt(1, tipoAsistenciaId); // El nuevo ID del tipo de asistencia
                    updateStatement.setInt(2, Integer.parseInt(employeeData.get("id").toString())); // ID del empleado
                    updateStatement.setString(3, employeeData.get("fechaEntrada").toString()); // Fecha seleccionada

                    updateStatement.executeUpdate();
                    updateStatement.close();
                }

                preparedStatement.close();
                resultSet.close();

                // Guardar las notas en la tabla de logs
                String insertLogQuery = "INSERT INTO logs (supervisor_id, action, target_employee_id, details) VALUES (?, ?, ?, ?)";
                PreparedStatement logStatement = connection.prepareStatement(insertLogQuery);
                logStatement.setInt(1, getCurrentSupervisorId()); // Obtener el ID del supervisor actual
                logStatement.setString(2, "Cambio de tipo de asistencia");
                logStatement.setInt(3, Integer.parseInt(employeeData.get("id").toString()));
                logStatement.setString(4, notas); // Almacenar las notas

                logStatement.executeUpdate();
                logStatement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Recargar la tabla para mostrar el nuevo valor
            employeeTableView.refresh();
        });
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

        // Consulta SQL para obtener todas las entradas/salidas, incluyendo las notas desde la tabla 'logs'
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida, " +
                "l.details as notas " +  // Ahora las notas vienen de la tabla 'logs'
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "LEFT JOIN logs l ON l.target_employee_id = e.id " +  // JOIN para obtener las notas desde la tabla 'logs'
                "AND l.action = 'Cambio de tipo de asistencia' " +  // Filtro para obtener solo los registros relacionados con el cambio de tipo de asistencia
                "ORDER BY dias.fecha DESC, en.hora_entrada DESC";


        Statement statement = connectDB.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            Map<String, Object> employeeData = new HashMap<>();
            employeeData.put("id", String.valueOf(resultSet.getInt("id")));
            employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
            employeeData.put("fechaEntrada", resultSet.getString("fecha"));
            employeeData.put("horaEntrada", resultSet.getString("hora_entrada"));
            employeeData.put("horaSalida", resultSet.getString("hora_salida"));

            // Calcular el tiempo laborado
            String horaEntrada = resultSet.getString("hora_entrada");
            String horaSalida = resultSet.getString("hora_salida");
            if (horaEntrada != null && horaSalida != null) {
                employeeData.put("tiempoLaborado", calculateTiempoLaborado(horaEntrada, horaSalida));
            } else {
                employeeData.put("tiempoLaborado", "N/A");
            }

            employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia"));
            employeeData.put("tipoSalida", resultSet.getString("tipo_salida"));
            employeeData.put("estado", resultSet.getString("estado"));

            // Añadir las notas
            employeeData.put("notas", resultSet.getString("notas") != null ? resultSet.getString("notas") : "");  // Si no hay notas, mostrar vacío

            employees.add(employeeData);
        }
        // Después de cargar los empleados, calcula el total de páginas
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

        connectDB.close();

        // Mostrar la primera página de resultados
        showPage(1);
    }


    private void searchByDateAndDepartment(String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados, String fechaInicio, String fechaFin) throws SQLException {
        employees.clear();
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Construcción básica de la consulta SQL
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida, " +
                "l.details as notas " +  // Notas desde logs
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "LEFT JOIN logs l ON l.target_employee_id = e.id AND l.action = 'Cambio de tipo de asistencia' " +
                "WHERE dias.fecha BETWEEN ? AND ? ";  // Filtro por fechas (siempre se requieren)

        // Filtro opcional por departamento (solo si no es "Todos los departamentos")
        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            query += "AND e.departamento_id IN (SELECT id FROM departamentos WHERE nombre = ?) ";
        }

        // Filtros para supervisores o empleados
        if (incluirSupervisores || incluirEmpleados) {
            query += "AND (";
            if (incluirSupervisores) {
                query += "e.jerarquia_id = 2 ";  // Supervisores
            }
            if (incluirSupervisores && incluirEmpleados) {
                query += "OR ";
            }
            if (incluirEmpleados) {
                query += "e.jerarquia_id = 3 ";  // Empleados
            }
            query += ") ";
        }

        // Filtro por búsqueda en nombre completo (concatenando nombres y apellidos)
        if (!searchQuery.isEmpty()) {
            query += "AND CONCAT(LOWER(TRIM(e.nombres)), ' ', LOWER(TRIM(e.apellido_paterno)), ' ', LOWER(TRIM(e.apellido_materno))) LIKE ? ";
        }

        query += "ORDER BY dias.fecha ASC";  // Ordenar por fecha

        // Preparar la consulta y asignar los parámetros
        try (PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {
            int paramIndex = 1;

            // Siempre asignamos los parámetros de fecha
            preparedStatement.setString(paramIndex++, fechaInicio);
            preparedStatement.setString(paramIndex++, fechaFin);

            // Si se filtra por departamento
            if (!departamentoSeleccionado.equals("Todos los departamentos")) {
                preparedStatement.setString(paramIndex++, departamentoSeleccionado);
            }

            // Si hay un valor de búsqueda, asignamos el patrón de búsqueda
            if (!searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                preparedStatement.setString(paramIndex++, searchPattern);
            }

            // Ejecutar la consulta y procesar los resultados
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha"));
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada"));
                employeeData.put("horaSalida", resultSet.getString("hora_salida"));
                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia"));
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida"));
                employeeData.put("estado", resultSet.getString("estado"));
                employeeData.put("notas", resultSet.getString("notas") != null ? resultSet.getString("notas") : "");

                // Calcular el tiempo laborado
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                if (horaEntrada != null && horaSalida != null) {
                    employeeData.put("tiempoLaborado", calculateTiempoLaborado(horaEntrada, horaSalida));
                } else {
                    employeeData.put("tiempoLaborado", "N/A");
                }

                employees.add(employeeData);
            }

            // Mostrar los datos en la tabla
            showPage(1);
            updatePaginationButtons();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        // Asegúrate de que fromIndex y toIndex estén dentro de los límites de la lista
        if (fromIndex > employees.size()) {
            fromIndex = employees.size() - itemsPerPage;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        // Configurar los elementos que se deben mostrar en esta página
        employeeTableView.setItems(FXCollections.observableArrayList(employees.subList(fromIndex, toIndex)));

        // Actualizar botones de paginación dinámicamente
        updatePaginationButtons();

        // Actualizar botones de navegación (deshabilitar si estamos en la primera o última página)
        previousButton.setDisable(pageNumber == 1);
        nextButton.setDisable(pageNumber == totalPages);
        // Después de cargar los empleados, calcula el total de páginas
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
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

    // aqui estaba el Handlerfilterchange

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

        selectedDepartmentLabel.setText("Departamento: Todos los departamentos" );

        // Cargar los registros más recientes (esto ya está en el método loadAllEntries)
        try {
            employees.clear();
            loadAllEntries(); // Recargar las entradas más recientes sin filtros
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Mostrar la primera página
        showPage(1);
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
                "AND dias.fecha BETWEEN ? AND ? ";

        // Añadir condiciones adicionales para supervisores o empleados
        if (supervisoresCheckBox.isSelected() || empleadosCheckBox.isSelected()) {
            query += " AND (";
            if (supervisoresCheckBox.isSelected()) {
                query += "e.jerarquia_id = 2";
            }
            if (supervisoresCheckBox.isSelected() && empleadosCheckBox.isSelected()) {
                query += " OR ";
            }
            if (empleadosCheckBox.isSelected()) {
                query += "e.jerarquia_id = 3";
            }
            query += ")";
        }

        // Si hay una búsqueda, agregar el filtro al query para buscar por nombre, apellido o nombre completo
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query += " AND (e.nombres LIKE ? OR e.apellido_paterno LIKE ? OR e.apellido_materno LIKE ? OR CONCAT(e.nombres, ' ', e.apellido_paterno, ' ', e.apellido_materno) LIKE ?)";
        }

        try (Connection connectDB = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {

            int paramIndex = 1;

            // Asignar los parámetros de la consulta
            preparedStatement.setString(paramIndex++, departamento);
            preparedStatement.setString(paramIndex++, tipoAsistencia);

            // Obtener los valores del rango de fechas
            String fechaInicio = (fechaInicioPicker.getValue() != null) ? fechaInicioPicker.getValue().toString() : "1900-01-01"; // Valor por defecto si no hay fecha seleccionada
            String fechaFin = (fechaFinPicker.getValue() != null) ? fechaFinPicker.getValue().toString() : "2100-12-31"; // Valor por defecto si no hay fecha seleccionada

            // Asignar las fechas a los parámetros de la consulta
            preparedStatement.setString(paramIndex++, fechaInicio);
            preparedStatement.setString(paramIndex++, fechaFin);

            // Si hay un valor de búsqueda, agregarlo como parámetro
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.trim() + "%";
                preparedStatement.setString(paramIndex++, searchPattern); // e.nombres LIKE ?
                preparedStatement.setString(paramIndex++, searchPattern); // e.apellido_paterno LIKE ?
                preparedStatement.setString(paramIndex++, searchPattern); // e.apellido_materno LIKE ?
                preparedStatement.setString(paramIndex++, searchPattern); // CONCAT(...) LIKE ?
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

    private void searchForNames(KeyEvent event) {
        String searchQuery = searchField.getText().trim();

        // Si el texto tiene menos de 3 caracteres, no buscar
        if (searchQuery.length() < 3) {
            suggestionsMenu.hide();
            return;
        }

        // Obtener el estado de los CheckBox
        boolean incluirSupervisores = supervisoresCheckBox.isSelected();
        boolean incluirEmpleados = empleadosCheckBox.isSelected();

        // Si ninguno está seleccionado, asumimos que se deben mostrar todos
        if (!incluirSupervisores && !incluirEmpleados) {
            incluirSupervisores = true;
            incluirEmpleados = true;
        }

        // Obtener el departamento seleccionado
        String departamentoSeleccionado = departamentoChoiceBox.getSelectionModel().getSelectedItem();

        // Consulta SQL que concatena los campos de nombres y apellidos
        StringBuilder query = new StringBuilder("SELECT CONCAT(TRIM(nombres), ' ', TRIM(apellido_paterno), ' ', TRIM(apellido_materno)) AS nombreCompleto, jerarquia_id, d.nombre as departamento ");
        query.append("FROM empleados e ");
        query.append("JOIN departamentos d ON e.departamento_id = d.id ");
        query.append("WHERE ");

        // Filtrar por nombre completo usando CONCAT en lugar de buscar por partes
        query.append("CONCAT(LOWER(TRIM(nombres)), ' ', LOWER(TRIM(apellido_paterno)), ' ', LOWER(TRIM(apellido_materno))) LIKE ? ");

        // Filtros para supervisores o empleados
        if (incluirSupervisores || incluirEmpleados) {
            query.append("AND (");
            if (incluirSupervisores) {
                query.append("jerarquia_id = 2");  // Supervisores
            }
            if (incluirSupervisores && incluirEmpleados) {
                query.append(" OR ");
            }
            if (incluirEmpleados) {
                query.append("jerarquia_id = 3");  // Empleados
            }
            query.append(") ");
        }

        // Filtrar por departamento, si no se seleccionó "Todos los departamentos"
        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            query.append("AND d.nombre = ? ");
        }

        query.append("ORDER BY nombreCompleto ASC");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

            int paramIndex = 1;

            // Crear el patrón de búsqueda
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";

            // Asignar el patrón de búsqueda
            preparedStatement.setString(paramIndex++, searchPattern);

            // Si el departamento seleccionado no es "Todos los departamentos", agregarlo como parámetro
            if (!departamentoSeleccionado.equals("Todos los departamentos")) {
                preparedStatement.setString(paramIndex++, departamentoSeleccionado);
            }

            // Ejecutar la consulta
            ResultSet resultSet = preparedStatement.executeQuery();
            ObservableList<String> results = FXCollections.observableArrayList();

            // Procesar los resultados
            while (resultSet.next()) {
                String nombreCompleto = resultSet.getString("nombreCompleto");
                int jerarquiaId = resultSet.getInt("jerarquia_id");
                String departamento = resultSet.getString("departamento");

                results.add(nombreCompleto + "," + jerarquiaId + "," + departamento);  // Añadir el nombre completo, jerarquía, y departamento a los resultados
            }

            if (!results.isEmpty()) {
                populateSuggestions(results);
            } else {
                suggestionsMenu.hide();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Método para mostrar sugerencias en el ContextMenu
    private void populateSuggestions(ObservableList<String> suggestions) {
        suggestionsMenu.getItems().clear();

        // Crear un VBox para contener las sugerencias
        VBox suggestionBox = new VBox();
        suggestionBox.setFillWidth(true);
        suggestionBox.setStyle("-fx-background-color: white;"); // Asegurar que el fondo del VBox sea siempre blanco

        // Obtener el estado de los checkboxes
        boolean incluirSupervisores = supervisoresCheckBox.isSelected();
        boolean incluirEmpleados = empleadosCheckBox.isSelected();

        // Si ninguno de los checkboxes está seleccionado, mostramos todos (supervisores y empleados)
        if (!incluirSupervisores && !incluirEmpleados) {
            incluirSupervisores = true;
            incluirEmpleados = true;
        }

        // Obtener el departamento seleccionado
        String departamentoSeleccionado = departamentoChoiceBox.getSelectionModel().getSelectedItem();

        for (String suggestion : suggestions) {
            // Dividir la sugerencia para obtener el nombre completo, la jerarquía y el departamento
            String[] suggestionParts = suggestion.split(",");
            String nombreCompleto = suggestionParts[0];
            int jerarquiaId = Integer.parseInt(suggestionParts[1]);  // Jerarquía: 2 = Supervisor, 3 = Empleado
            String departamento = suggestionParts.length > 2 ? suggestionParts[2] : ""; // Obtener el departamento si está presente

            // Filtrar según el estado de los checkboxes y el departamento seleccionado
            boolean perteneceAlDepartamento = departamentoSeleccionado.equals("Todos los departamentos") || departamentoSeleccionado.equals(departamento);

            if (perteneceAlDepartamento && ((incluirSupervisores && jerarquiaId == 2) || (incluirEmpleados && jerarquiaId == 3))) {
                // Crear un Label para cada sugerencia en lugar de un MenuItem
                Label item = new Label(nombreCompleto);
                item.setStyle("-fx-padding: 5px; -fx-background-color: white;"); // Añadir algo de estilo
                // Añadir un evento de "hover" para que se destaque el Label cuando el mouse esté sobre él
                item.setOnMouseEntered(event -> item.setStyle("-fx-padding: 5px; -fx-background-color: #0078d7; -fx-text-fill: white;"));
                item.setOnMouseExited(event -> item.setStyle("-fx-padding: 5px; -fx-background-color: white; -fx-text-fill: black;"));

                // Evento al hacer clic sobre la sugerencia
                item.setOnMouseClicked(event -> {
                    searchField.setText(nombreCompleto);
                    suggestionsMenu.hide();
                });

                suggestionBox.getChildren().add(item); // Añadir el Label al VBox
            }
        }

        // Crear un ScrollPane para el VBox si hay muchas sugerencias
        ScrollPane scrollPane = new ScrollPane(suggestionBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150); // Altura máxima del ScrollPane
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Activar scroll vertical cuando sea necesario

        // Añadir el ScrollPane en un CustomMenuItem
        CustomMenuItem scrollableMenuItem = new CustomMenuItem(scrollPane, false);

        // Añadir el CustomMenuItem al ContextMenu
        suggestionsMenu.getItems().add(scrollableMenuItem);

        // Mostrar el menú justo debajo del TextField
        if (!suggestionsMenu.isShowing()) {
            Bounds boundsInScreen = searchField.localToScreen(searchField.getBoundsInLocal());
            suggestionsMenu.show(searchField, boundsInScreen.getMinX(), boundsInScreen.getMaxY());
        }
    }






}