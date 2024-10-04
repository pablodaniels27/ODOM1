package controllers;

import DAO.BaseDAO;
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



import javafx.scene.input.KeyEvent;

import static Usuarios.Supervisor.getCurrentSupervisorId;

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

            // Llamar al DAO para actualizar el tipo de asistencia y guardar las notas
            try {
                int tipoAsistenciaId = BaseDAO.obtenerIdTipoAsistencia(newTipoAsistencia);
                if (tipoAsistenciaId != -1) {
                    int empleadoId = Integer.parseInt(employeeData.get("id").toString());
                    String fechaEntrada = employeeData.get("fechaEntrada").toString();

                    // Actualizar el tipo de asistencia del empleado
                    BaseDAO.actualizarTipoAsistencia(empleadoId, fechaEntrada, tipoAsistenciaId);

                    // Registrar el cambio en los logs
                    int supervisorId = getCurrentSupervisorId(); // Obtener el ID del supervisor actual
                    BaseDAO.registrarCambioLog(supervisorId, "Cambio de tipo de asistencia", empleadoId, notas);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Recargar la tabla para mostrar el nuevo valor
            employeeTableView.refresh();
        });
    }


    private void cargarDepartamentos() {
        departamentoChoiceBox.getItems().add("Todos los departamentos"); // Agregar opción para todos los departamentos

        try {
            List<String> departamentos = BaseDAO.obtenerDepartamentos();
            departamentoChoiceBox.getItems().addAll(departamentos);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Seleccionar la opción "Todos los departamentos" por defecto
        departamentoChoiceBox.getSelectionModel().selectFirst();
    }


    private void loadAllEntries() throws SQLException {
        employees.clear();

        try {
            List<Map<String, Object>> entries = BaseDAO.obtenerTodasLasEntradas();
            for (Map<String, Object> employeeData : entries) {
                // Calcular el tiempo laborado si hay hora de entrada y salida
                String horaEntrada = (String) employeeData.get("horaEntrada");
                String horaSalida = (String) employeeData.get("horaSalida");
                if (horaEntrada != null && horaSalida != null) {
                    employeeData.put("tiempoLaborado", calculateTiempoLaborado(horaEntrada, horaSalida));
                } else {
                    employeeData.put("tiempoLaborado", "N/A");
                }
                employees.add(employeeData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Después de cargar los empleados, calcula el total de páginas
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

        // Mostrar la primera página de resultados
        showPage(1);
    }


    private void searchByDateAndDepartment(String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados, String fechaInicio, String fechaFin) throws SQLException {
        employees.clear();

        try {
            List<Map<String, Object>> searchResults = BaseDAO.buscarPorFechaYDepartamento(departamentoSeleccionado, searchQuery, incluirSupervisores, incluirEmpleados, fechaInicio, fechaFin);
            for (Map<String, Object> employeeData : searchResults) {
                // Calcular el tiempo laborado si hay hora de entrada y salida
                String horaEntrada = (String) employeeData.get("horaEntrada");
                String horaSalida = (String) employeeData.get("horaSalida");
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

        try {
            // Obtener los valores del rango de fechas
            String fechaInicio = (fechaInicioPicker.getValue() != null) ? fechaInicioPicker.getValue().toString() : "1900-01-01"; // Valor por defecto si no hay fecha seleccionada
            String fechaFin = (fechaFinPicker.getValue() != null) ? fechaFinPicker.getValue().toString() : "2100-12-31"; // Valor por defecto si no hay fecha seleccionada

            boolean incluirSupervisores = supervisoresCheckBox.isSelected();
            boolean incluirEmpleados = empleadosCheckBox.isSelected();

            Set<String> empleadosUnicos = BaseDAO.buscarNombresPorAsistencia(departamento, tipoAsistencia, fechaInicio, fechaFin, searchQuery, incluirSupervisores, incluirEmpleados);

            // Convertir el Set en una lista observable para el TableView
            ObservableList<Empleado> empleados = FXCollections.observableArrayList();
            for (String nombre : empleadosUnicos) {
                empleados.add(new Empleado(nombre));  // Agregar el nombre único al TableView
            }

            // Asignar los datos al TableView
            personTableView.setItems(empleados);

            System.out.println("Nombres únicos añadidos al TableView: " + empleados.size());

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        try {
            Set<String> fechasUnicas = BaseDAO.buscarFechasPorEmpleado(departamento, tipoAsistencia, nombreEmpleado, fechaInicio, fechaFin);

            // Convertir el Set en una lista observable para el TableView
            ObservableList<Fecha> fechas = FXCollections.observableArrayList();
            for (String fecha : fechasUnicas) {
                fechas.add(new Fecha(fecha));
            }

            // Asignar los datos al dateTableView
            dateTableView.setItems(fechas);
            System.out.println("Fechas asignadas al TableView");

        } catch (SQLException e) {
            System.err.println("Error al obtener fechas: ");
            e.printStackTrace();
        }
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

        if (!incluirSupervisores && !incluirEmpleados) {
            incluirSupervisores = true;
            incluirEmpleados = true;
        }

        // Obtener el departamento seleccionado
        String departamentoSeleccionado = departamentoChoiceBox.getSelectionModel().getSelectedItem();

        try {
            ObservableList<String> results = BaseDAO.buscarNombresPorConsulta(searchQuery, departamentoSeleccionado, incluirSupervisores, incluirEmpleados);

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