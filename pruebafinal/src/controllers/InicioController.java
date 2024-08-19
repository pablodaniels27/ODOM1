package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

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
    private ListView<VBox> employeeListView;

    @FXML
    private VBox calendarContainer;

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

    private List<Employee> employees;
    private YearMonth currentYearMonth;

    @FXML
    private void initialize() {
        // Mostrar la fecha y hora actuales
        currentDate.setText(LocalDate.now().toString()); // Simplificado para ejemplo

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

        for (Employee employee : employees) {
            VBox employeeBox = new VBox();
            Label nameLabel = new Label(employee.getFullName());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            employeeBox.getChildren().add(nameLabel);
            employeeListView.getItems().add(employeeBox);
        }
    }

    // Método que maneja la selección de un empleado en la lista
    @FXML
    private void handleEmployeeSelection(MouseEvent event) {
        int selectedIndex = employeeListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Employee selectedEmployee = employees.get(selectedIndex);
            updateCalendarForEmployee(selectedEmployee);
        }
    }


    // Método para actualizar el calendario con los datos del empleado seleccionado
    private void updateCalendarForEmployee(Employee employee) {
        employeeNameLabel.setText(employee.getFullName());
        visitsInfoLabel.setText("Este mes " + employee.getFullName() + " tiene " + employee.getVisits() + " visitas a realizar");
        updateCalendar(); // Actualiza el calendario según el mes actual
    }

    // Método para actualizar el calendario
    private void updateCalendar() {
        // Limpiar el calendario anterior
        calendarGrid.getChildren().clear();

        // Configurar el título del mes
        currentMonthLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentYearMonth.getYear());

        // Obtener el primer día del mes y el número total de días en el mes
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 (lunes) - 7 (domingo)

        // Poblar el calendario con los días del mes
        int row = 1;
        int column = dayOfWeek - 1;
        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            calendarGrid.add(dayLabel, column, row);

            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }
    }

    // Ejemplo de método para obtener los empleados desde la base de datos
    private List<Employee> getEmployeesFromDatabase() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Hugo Contreras", "Programador: Va a reprogramar una máquina", 3));
        employees.add(new Employee("Britton Grajales", "Técnico de campo: Mantenimiento de computadoras", 5));
        return employees;
    }

    // Clase para representar un empleado
    public static class Employee {
        private final String fullName;
        private final String taskDescription;
        private final int visits;

        public Employee(String fullName, String taskDescription, int visits) {
            this.fullName = fullName;
            this.taskDescription = taskDescription;
            this.visits = visits;
        }

        public String getFullName() {
            return fullName;
        }

        public String getTaskDescription() {
            return taskDescription;
        }

        public int getVisits() {
            return visits;
        }
    }
}
