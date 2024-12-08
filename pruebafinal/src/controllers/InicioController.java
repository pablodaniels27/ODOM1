package controllers;

import DAO.BaseDAO;
import Usuarios.Lider;
import Usuarios.SessionManager;
import Usuarios.Supervisor;
import Usuarios.Usuario;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InicioController {

    @FXML
    private Label currentDate;

    @FXML
    private SplitPane splitPane;

    @FXML
    private VBox employeeListContainer;

    @FXML
    private Label visitsInfoLabel;

    @FXML
    private Label currentMonthLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML
    private TextField searchBar;

    @FXML
    private Label mondayEntryLabel;
    @FXML
    private Label mondayExitLabel;
    @FXML
    private Label tuesdayEntryLabel;
    @FXML
    private Label tuesdayExitLabel;

    @FXML
    private Label wednesdayEntryLabel;
    @FXML
    private Label wednesdayExitLabel;

    @FXML
    private Label thursdayEntryLabel;
    @FXML
    private Label thursdayExitLabel;

    @FXML
    private Label fridayEntryLabel;
    @FXML
    private Label fridayExitLabel;

    @FXML
    private Label saturdayEntryLabel;
    @FXML
    private Label saturdayExitLabel;

    @FXML
    private Label sundayEntryLabel;
    @FXML
    private Label sundayExitLabel;

    private VBox selectedVBox = null; // Para rastrear el cuadro seleccionado

    private List<Employee> employees;
    private YearMonth currentYearMonth;
    private Employee selectedEmployee;

    @FXML
    private void initialize() {
        splitPane.setDividerPositions(0.5);

        // Hacer que el divisor sea invisible y no interactivo
        splitPane.lookupAll(".split-pane-divider").forEach(div -> {
            div.setStyle("-fx-background-color: transparent;");  // Hacer el divisor invisible
            div.setMouseTransparent(true);  // Desactivar la interacción del mouse
        });

        // Mostrar la fecha actual
        currentDate.setText(LocalDate.now().toString());

        // Inicializar el mes actual
        currentYearMonth = YearMonth.now();
        updateCalendar();

        // Configurar botones de navegación entre meses
        prevMonthButton.setOnAction(event -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        nextMonthButton.setOnAction(event -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        // Agregar listener al campo de búsqueda
        searchBar.setOnKeyReleased(this::handleSearch);

        // Cargar empleados desde la base de datos
        loadEmployeesFromDatabase("");
    }

    private void loadEmployeesFromDatabase(String filter) {
        employees = getActiveEmployeesFromDatabase(filter);
        System.out.println("Number of active employees loaded: " + employees.size());

        // Limpiar el contenedor de empleados antes de agregar nuevos elementos
        employeeListContainer.getChildren().clear();

        // Añadir todos los empleados al VBox
        for (Employee employee : employees) {
            VBox employeeBox = createEmployeeVBox(employee);
            employeeListContainer.getChildren().add(employeeBox);
        }
    }

    private VBox createEmployeeVBox(Employee employee) {
        VBox employeeBox = new VBox(5); // Espaciado de 5 entre elementos
        employeeBox.setStyle("-fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: white;");
        employeeBox.setSpacing(10);

        Label nameLabel = new Label(employee.fullName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label professionLabel = new Label(employee.profession());
        professionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        employeeBox.getChildren().addAll(nameLabel, professionLabel);

        // Efecto hover
        employeeBox.setOnMouseEntered(event -> {
            if (selectedVBox != employeeBox) {
                employeeBox.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
            }
        });

        employeeBox.setOnMouseExited(event -> {
            if (selectedVBox != employeeBox) {
                employeeBox.setStyle("-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
            }
        });

        // Efecto de selección
        employeeBox.setOnMouseClicked(event -> {
            if (selectedVBox != null) {
                selectedVBox.setStyle("-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
                handleEmployeeSelection(employee);
            }
            employeeBox.setStyle("-fx-background-color: #d1e7dd; -fx-border-color: lightgrey; -fx-border-width: 3; -fx-padding: 10;");
            selectedVBox = employeeBox;
        });

        return employeeBox;
    }

    // Método para manejar la búsqueda
    private void handleSearch(KeyEvent event) {
        String searchText = searchBar.getText().trim();
        loadEmployeesFromDatabase(searchText);
    }

    // Método para manejar la selección de un empleado en la lista
    private void handleEmployeeSelection( Employee employee) {
        if (employee != null) {
            this.selectedEmployee = employee;
            visitsInfoLabel.setText(employee.fullName());

            // Limpiar el cuadrante 3 antes de actualizar
            clearAttendanceLabels();

            updateCalendar(); // Actualiza el calendario según el mes actual
        }
    }

    // Método para actualizar el calendario con los datos del empleado seleccionado
    private void updateCalendar() {
        // Limpiar el calendario anterior
        calendarGrid.getChildren().clear();

        // Configurar el título del mes con la primera letra en mayúscula
        String month = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String formattedMonth = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
        String monthYear = formattedMonth + " " + currentYearMonth.getYear();

        currentMonthLabel.setText(monthYear);
        currentMonthLabel.setMinWidth(150);  // Incrementa el ancho mínimo
        currentMonthLabel.setMaxWidth(150);  // Incrementa el ancho máximo
        currentMonthLabel.setAlignment(Pos.CENTER);  // Centrar el texto

        // Añadir los días de la semana en la primera fila, usando abreviaciones de dos letras
        String[] daysOfWeek = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-font-weight: bold;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Obtener el primer día del mes y el número total de días en el mes
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 (lunes) - 7 (domingo)

        // Poblar el calendario con los días del mes, comenzando en la segunda fila (fila 1)
        int row = 1;
        int column = dayOfWeek - 1;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);

            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.getStyleClass().add("day-label"); // Aplica la clase CSS
            dayLabel.setOnMouseClicked(event -> handleWeekSelection(date));

            calendarGrid.add(dayLabel, column, row);

            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }
    }

    // Método para manejar la selección de una semana en el calendario
    private void handleWeekSelection(LocalDate selectedDate) {
        clearWeekSelection(); // Limpiar la selección previa

        LocalDate startOfWeek = calcularInicioDeSemana(selectedDate); // Calcular el lunes de la semana
        resaltarDiasDeLaSemana(startOfWeek); // Seleccionar y resaltar los días de la semana
        loadWeekAttendance(startOfWeek); // Cargar asistencias de la semana seleccionada
    }

    private LocalDate calcularInicioDeSemana(LocalDate selectedDate) {
        return selectedDate.minusDays((long) (selectedDate.getDayOfWeek().getValue() - 1));

    }

    private void resaltarDiasDeLaSemana(LocalDate startOfWeek) {
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            if (currentDay.getMonth().equals(currentYearMonth.getMonth())) {
                resaltarDiaSeleccionado(currentDay);
            }
        }
    }

    private void resaltarDiaSeleccionado(LocalDate currentDay) {
        for (Node node : calendarGrid.getChildren()) {
            if (esNodoValido(node)) {
                String text = ((Label) node).getText();
                try {
                    int day = Integer.parseInt(text);
                    if (currentDay.getDayOfMonth() == day) {
                        node.getStyleClass().add("selected-day");
                    }
                } catch (NumberFormatException e) {
                    // Ignorar nodos que no representan días numéricos
                }
            }
        }
    }

    private boolean esNodoValido(Node node) {
        return GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null;
    }


    private void clearWeekSelection() {
        // Limpiar cualquier selección previa
        for (Node node : calendarGrid.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0) { // No limpiar la primera fila (días de la semana)
                node.getStyleClass().remove("selected-day"); // Remover la clase de día seleccionado
            }
        }
    }

    // Método para cargar las asistencias de la semana seleccionada
    private void loadWeekAttendance(LocalDate startOfWeek) {
        if (selectedEmployee == null) return;

        try {
            // Obtener los datos de asistencia semanal del empleado desde el DAO
            List<Map<String, Object>> attendanceRecords = BaseDAO.obtenerAsistenciaSemanal(
                    selectedEmployee.id(), startOfWeek, startOfWeek.plusDays(6));

            clearAttendanceLabels(); // Limpiar los labels antes de cargar los nuevos datos

            // Procesar los registros de asistencia y actualizar los labels correspondientes
            for (Map<String, Object> record : attendanceRecords) {
                LocalDate date = ((Date) record.get("fecha")).toLocalDate();
                String horaEntrada = (String) record.get("hora_entrada");
                String horaSalida = (String) record.get("hora_salida");

                actualizarLabelsPorDia(date, horaEntrada, horaSalida);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void actualizarLabelsPorDia(LocalDate date, String horaEntrada, String horaSalida) {
        switch (date.getDayOfWeek()) {
            case MONDAY -> actualizarDia(mondayEntryLabel, mondayExitLabel, horaEntrada, horaSalida);
            case TUESDAY -> actualizarDia(tuesdayEntryLabel, tuesdayExitLabel, horaEntrada, horaSalida);
            case WEDNESDAY -> actualizarDia(wednesdayEntryLabel, wednesdayExitLabel, horaEntrada, horaSalida);
            case THURSDAY -> actualizarDia(thursdayEntryLabel, thursdayExitLabel, horaEntrada, horaSalida);
            case FRIDAY -> actualizarDia(fridayEntryLabel, fridayExitLabel, horaEntrada, horaSalida);
            case SATURDAY -> actualizarDia(saturdayEntryLabel, saturdayExitLabel, horaEntrada, horaSalida);
            case SUNDAY -> actualizarDia(sundayEntryLabel, sundayExitLabel, horaEntrada, horaSalida);
        }
    }

    private void actualizarDia(Label entryLabel, Label exitLabel, String horaEntrada, String horaSalida) {
        entryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
        exitLabel.setText(horaSalida != null ? horaSalida : "N/A");
    }



    // Limpiar los labels de asistencias
    private void clearAttendanceLabels() {
        mondayEntryLabel.setText("");
        mondayExitLabel.setText("");
        tuesdayEntryLabel.setText("");
        tuesdayExitLabel.setText("");
        wednesdayEntryLabel.setText("");
        wednesdayExitLabel.setText("");
        thursdayEntryLabel.setText("");
        thursdayExitLabel.setText("");
        fridayEntryLabel.setText("");
        fridayExitLabel.setText("");
        saturdayEntryLabel.setText("");
        saturdayExitLabel.setText("");
        sundayEntryLabel.setText("");
        sundayExitLabel.setText("");
    }


    // Método para obtener los empleados desde la base de datos
    private List<Employee> getActiveEmployeesFromDatabase(String filter) {
        try {
            Usuario usuario = SessionManager.getCurrentUser();

            if (usuario instanceof Lider) {
                // Obtener todos los empleados activos
                return BaseDAO.obtenerEmpleadosActivos(filter);
            } else if (usuario instanceof Supervisor) {
                // Obtener empleados activos del departamento del supervisor
                Supervisor supervisor = (Supervisor) usuario;
                int departamentoId = supervisor.getDepartamentoId();
                return BaseDAO.obtenerEmpleadosActivosPorDepartamento(filter, departamentoId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }




    // Clase para representar un empleado
    public record Employee(int id, String fullName, String profession) {
    }
}
