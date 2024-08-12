package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;

public class InicioController {

    @FXML
    private ListView<String> employeeListView;
    @FXML
    private DatePicker calendarDatePicker;

    @FXML
    public void initialize() {
        // Inicializa componentes específicos para la vista de inicio
        // Por ejemplo, puedes agregar algunos datos de prueba a la lista
        employeeListView.getItems().addAll("Empleado 1", "Empleado 2", "Empleado 3");
    }
}
