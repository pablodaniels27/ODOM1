package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class MonitoreoController {

    @FXML
    private ComboBox<String> zonaTrabajoComboBox;
    @FXML
    private ComboBox<String> supervisoresComboBox;
    @FXML
    private DatePicker fechaInicioPicker;
    @FXML
    private DatePicker fechaFinPicker;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<?> employeeTableView;

    @FXML
    public void initialize() {
        // Inicializa componentes específicos para la vista de monitoreo
    }
}

