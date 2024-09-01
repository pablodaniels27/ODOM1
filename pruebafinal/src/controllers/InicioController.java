package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InicioController {

    @FXML
    private Label currentDate;

    @FXML
    private VBox employeeListContainer;

    @FXML
    private Label employeeNameLabel;

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
    private Label mondayEntryLabel, mondayExitLabel;
    @FXML
    private Label tuesdayEntryLabel, tuesdayExitLabel;
    @FXML
    private Label wednesdayEntryLabel, wednesdayExitLabel;
    @FXML
    private Label thursdayEntryLabel, thursdayExitLabel;
    @FXML
    private Label fridayEntryLabel, fridayExitLabel;
    @FXML
    private Label saturdayEntryLabel, saturdayExitLabel;
    @FXML
    private Label sundayEntryLabel, sundayExitLabel;

    private List<Employee> employees;
    private YearMonth currentYearMonth;
    private Employee selectedEmployee;

    @FXML
    private void initialize() {
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

        // Cargar empleados desde la base de datos
        loadEmployeesFromDatabase();
    }

    // Método para cargar empleados desde la base de datos
    private void loadEmployeesFromDatabase() {
        employees = getEmployeesFromDatabase();
        System.out.println("Number of employees loaded: " + employees.size());

        // Limpiar el contenedor de empleados antes de agregar nuevos elementos
        employeeListContainer.getChildren().clear();

        // Añadir todos los empleados al VBox
        for (Employee employee : employees) {
            VBox employeeBox = createEmployeeVBox(employee);
            employeeListContainer.getChildren().add(employeeBox);
        }
    }

    // Crear un VBox para representar a un empleado
    private VBox createEmployeeVBox(Employee employee) {
        VBox vbox = new VBox(5); // Espaciado de 5 entre elementos
        vbox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label nameLabel = new Label(employee.fullName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label professionLabel = new Label(employee.profession());
        professionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        // Añadir evento de clic al VBox
        vbox.setOnMouseClicked(event -> handleEmployeeSelection(event, employee));

        vbox.getChildren().addAll(nameLabel, professionLabel);

        return vbox;
    }

    // Método que maneja la selección de un empleado en la lista
    private void handleEmployeeSelection(MouseEvent event, Employee employee) {
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
        // Limpiar la selección previa
        clearWeekSelection();

        // Encontrar el lunes de la semana seleccionada
        LocalDate startOfWeek = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);

        // Seleccionar toda la semana (de lunes a domingo)
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            if (currentDay.getMonth().equals(currentYearMonth.getMonth())) {
                // Buscar y resaltar el Label correspondiente al día seleccionado
                for (Node node : calendarGrid.getChildren()) {
                    if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                        String text = ((Label) node).getText();
                        try {
                            int day = Integer.parseInt(text);
                            if (currentDay.getDayOfMonth() == day) {
                                node.getStyleClass().add("selected-day");
                            }
                        } catch (NumberFormatException e) {
                            // Ignorar, ya que este nodo no es un número (es un día de la semana)
                        }
                    }
                }
            }
        }

        // Cargar las asistencias de la semana seleccionada
        loadWeekAttendance(startOfWeek);
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

        String query = "SELECT dia.fecha, es.hora_entrada, es.hora_salida " +
                "FROM entradas_salidas es " +
                "JOIN dias dia ON es.dia_id = dia.id " +
                "WHERE es.empleado_id = ? AND dia.fecha BETWEEN ? AND ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, selectedEmployee.id()); // Suponiendo que tienes el ID del empleado en la clase Employee
            preparedStatement.setDate(2, java.sql.Date.valueOf(startOfWeek));
            preparedStatement.setDate(3, java.sql.Date.valueOf(startOfWeek.plusDays(6)));

            ResultSet resultSet = preparedStatement.executeQuery();

            clearAttendanceLabels();

            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("fecha").toLocalDate();
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");

                // Asignar las horas de entrada y salida a los labels correspondientes
                switch (date.getDayOfWeek()) {
                    case MONDAY -> {
                        mondayEntryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
                        mondayExitLabel.setText(horaSalida != null ? horaSalida : "N/A");
                    }
                    case TUESDAY -> {
                        tuesdayEntryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
                        tuesdayExitLabel.setText(horaSalida != null ? horaSalida : "N/A");
                    }
                    case WEDNESDAY -> {
                        wednesdayEntryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
                        wednesdayExitLabel.setText(horaSalida != null ? horaSalida : "N/A");
                    }
                    case THURSDAY -> {
                        thursdayEntryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
                        thursdayExitLabel.setText(horaSalida != null ? horaSalida : "N/A");
                    }
                    case FRIDAY -> {
                        fridayEntryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
                        fridayExitLabel.setText(horaSalida != null ? horaSalida : "N/A");
                    }
                    case SATURDAY -> {
                        saturdayEntryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
                        saturdayExitLabel.setText(horaSalida != null ? horaSalida : "N/A");
                    }
                    case SUNDAY -> {
                        sundayEntryLabel.setText(horaEntrada != null ? horaEntrada : "N/A");
                        sundayExitLabel.setText(horaSalida != null ? horaSalida : "N/A");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    private List<Employee> getEmployeesFromDatabase() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT id, CONCAT(nombres, ' ', apellido_paterno, ' ', apellido_materno) AS full_name, profesion FROM empleados";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String fullName = resultSet.getString("full_name");
                String profession = resultSet.getString("profesion");

                employees.add(new Employee(id, fullName, profession));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    // Clase para representar un empleado
    public record Employee(int id, String fullName, String profession) {
    }
}
