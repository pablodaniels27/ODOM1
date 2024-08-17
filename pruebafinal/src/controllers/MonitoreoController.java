package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MonitoreoController {

    @FXML
    private TableView<Employee> employeeTableView;

    @FXML
    private TableColumn<Employee, String> nombreColumn;

    @FXML
    private TableColumn<Employee, String> idColumn;

    @FXML
    private TableColumn<Employee, String> fechaEntradaColumn;

    @FXML
    private TableColumn<Employee, String> horaEntradaColumn;

    @FXML
    private TableColumn<Employee, String> horaSalidaColumn;

    @FXML
    private TableColumn<Employee, String> tiempoLaboradoColumn;

    @FXML
    private TableColumn<Employee, String> infoColumn;

    private ObservableList<Employee> employees = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar las columnas con los nombres de las propiedades
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fechaEntradaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaEntrada"));
        horaEntradaColumn.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        horaSalidaColumn.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        tiempoLaboradoColumn.setCellValueFactory(new PropertyValueFactory<>("tiempoLaborado"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<>("info"));

        // Agregar algunos empleados de ejemplo
        employees.add(new Employee("Juan Pérez", "001", "2024-08-20", "08:00", "17:00", "8h", "Info 1"));
        employees.add(new Employee("María Gómez", "002", "2024-08-20", "08:15", "17:15", "8h", "Info 2"));
        employees.add(new Employee("Luis Rodríguez", "003", "2024-08-20", "08:30", "17:30", "8h", "Info 3"));

        employeeTableView.setItems(employees);

        // Ajustar el ancho de las columnas al contenido
        adjustColumnWidths();
    }

    // Método para ajustar el ancho de las columnas
    private void adjustColumnWidths() {
        employeeTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        employeeTableView.getColumns().forEach(column -> {
            column.setMinWidth(column.getText().length() * 13); // Ajustar el ancho mínimo de la columna
            column.setPrefWidth(Control.USE_COMPUTED_SIZE);
            column.setResizable(true); // Asegurar que la columna sea redimensionable
        });
    }

    // Clase interna para representar a un empleado
    public static class Employee {
        private final String nombreCompleto;
        private final String id;
        private final String fechaEntrada;
        private final String horaEntrada;
        private final String horaSalida;
        private final String tiempoLaborado;
        private final String info;

        public Employee(String nombreCompleto, String id, String fechaEntrada, String horaEntrada, String horaSalida, String tiempoLaborado, String info) {
            this.nombreCompleto = nombreCompleto;
            this.id = id;
            this.fechaEntrada = fechaEntrada;
            this.horaEntrada = horaEntrada;
            this.horaSalida = horaSalida;
            this.tiempoLaborado = tiempoLaborado;
            this.info = info;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public String getId() {
            return id;
        }

        public String getFechaEntrada() {
            return fechaEntrada;
        }

        public String getHoraEntrada() {
            return horaEntrada;
        }

        public String getHoraSalida() {
            return horaSalida;
        }

        public String getTiempoLaborado() {
            return tiempoLaborado;
        }

        public String getInfo() {
            return info;
        }
    }
}
