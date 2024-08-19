package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InicioController {

    public VBox employeeList;
    @FXML
    private Label currentDate;

    @FXML
    private ListView<VBox> employeeListView;

    @FXML
    private void initialize() {
        // Mostrar la fecha actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy 'a las' HH:mm a");
        String formattedDate = LocalDate.now().format(formatter);
        currentDate.setText(formattedDate);

        // Cargar el departamento actual (Ejemplo: Consultar la base de datos)
        String departmentName = getDepartmentNameFromDatabase();
        Label locationLabel = new Label(departmentName);

        // Cargar empleados desde la base de datos
        List<Employee> employees = getEmployeesFromDatabase();
        for (Employee employee : employees) {
            VBox employeeBox = new VBox();
            Label nameLabel = new Label(employee.getName());
            Label taskLabel = new Label(employee.getTaskDescription());
            employeeBox.getChildren().addAll(nameLabel, taskLabel);
            employeeListView.getItems().add(employeeBox);
        }
    }

    private String getDepartmentNameFromDatabase() {
        // Código para obtener el nombre del departamento desde la base de datos
        return "Departamento de Ejemplo";
    }

    private List<Employee> getEmployeesFromDatabase() {
        // Código para obtener los empleados desde la base de datos
        return List.of(
                new Employee("Hugo Contreras", "Programador: Va a reprogramar una máquina"),
                new Employee("Britton Grajales", "Técnico de campo: Mantenimiento de computadoras")
        );
    }

    // Clase interna para representar empleados
    public class Employee {
        private String name;
        private String taskDescription;

        public Employee(String name, String taskDescription) {
            this.name = name;
            this.taskDescription = taskDescription;
        }

        public String getName() {
            return name;
        }

        public String getTaskDescription() {
            return taskDescription;
        }
    }
}