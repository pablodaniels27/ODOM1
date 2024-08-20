package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    private List<Employee> employees;
    private YearMonth currentYearMonth;

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
            updateCalendarForEmployee(employee);
        }
    }

    // Método para actualizar el calendario con los datos del empleado seleccionado
    private void updateCalendarForEmployee(Employee employee) {
        employeeNameLabel.setText(employee.fullName());
        visitsInfoLabel.setText("Este mes " + employee.fullName() + " tiene visitas a realizar");
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

    // Método para obtener los empleados desde la base de datos
    private List<Employee> getEmployeesFromDatabase() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT CONCAT(nombres, ' ', apellido_paterno, ' ', apellido_materno) AS full_name, profesion FROM empleados";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String fullName = resultSet.getString("full_name");
                String profession = resultSet.getString("profesion");

                employees.add(new Employee(fullName, profession));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    // Clase para representar un empleado
    public record Employee(String fullName, String profession) {
    }
}
