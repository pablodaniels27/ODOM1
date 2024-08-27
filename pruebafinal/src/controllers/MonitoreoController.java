package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
    private TableColumn<Employee, String> tipoAsistenciaColumn;

    @FXML
    private TableColumn<Employee, String> tipoSalidaColumn;

    @FXML
    private TableColumn<Employee, String> estadoColumn;

    @FXML
    private Button previousButton;

    @FXML
    private Button page1Button;

    @FXML
    private Button page2Button;

    @FXML
    private Button page3Button;

    @FXML
    private Button nextButton;

    @FXML
    private ChoiceBox<Integer> itemsPerPageChoiceBox;

    private final ObservableList<Employee> employees = FXCollections.observableArrayList();

    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        // Configurar las columnas con los nombres de las propiedades
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fechaEntradaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaEntrada"));
        horaEntradaColumn.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        horaSalidaColumn.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        tiempoLaboradoColumn.setCellValueFactory(new PropertyValueFactory<>("tiempoLaborado"));
        tipoAsistenciaColumn.setCellValueFactory(new PropertyValueFactory<>("tipoAsistencia"));
        tipoSalidaColumn.setCellValueFactory(new PropertyValueFactory<>("tipoSalida"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Agregar empleados de ejemplo
        for (int i = 1; i <= 50; i++) {
            employees.add(new Employee(
                    "Empleado " + i,
                    String.format("%03d", i),
                    "2024-08-20",
                    "08:00",
                    "17:00",
                    "8h",
                    "Asistencia",
                    "Salió a tiempo",
                    "Activo"
            ));
        }

        // Configurar el ChoiceBox de cantidad de datos por página
        itemsPerPageChoiceBox.setItems(FXCollections.observableArrayList(10, 20, 50, 100, 200));
        itemsPerPageChoiceBox.setValue(itemsPerPage);
        itemsPerPageChoiceBox.setOnAction(event -> {
            itemsPerPage = itemsPerPageChoiceBox.getValue();
            currentPage = 1;
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);
            showPage(currentPage);
            updatePaginationButtons();
        });

        // Calcular el total de páginas
        totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

        // Mostrar la primera página
        showPage(currentPage);

        // Configurar los botones de paginación
        configurePaginationButtons();

        // Ajustar el ancho de las columnas al contenido
        adjustColumnWidths();
    }

    private void showPage(int pageNumber) {
        int fromIndex = (pageNumber - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, employees.size());
        employeeTableView.setItems(FXCollections.observableArrayList(employees.subList(fromIndex, toIndex)));
    }

    private void adjustColumnWidths() {
        employeeTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        employeeTableView.getColumns().forEach(column -> {
            column.setMinWidth(column.getText().length() * 13);
            column.setPrefWidth(Control.USE_COMPUTED_SIZE);
            column.setResizable(true);
        });
    }

    private void configurePaginationButtons() {
        previousButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                showPage(currentPage);
                updatePaginationButtons();
            }
        });

        nextButton.setOnAction(event -> {
            if (currentPage < totalPages) {
                currentPage++;
                showPage(currentPage);
                updatePaginationButtons();
            }
        });

        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        page1Button.setText(String.valueOf(currentPage));
        page1Button.setOnAction(event -> showPage(currentPage));

        if (currentPage + 1 <= totalPages) {
            page2Button.setText(String.valueOf(currentPage + 1));
            page2Button.setVisible(true);
            page2Button.setOnAction(event -> {
                currentPage++;
                showPage(currentPage);
                updatePaginationButtons();
            });
        } else {
            page2Button.setVisible(false);
        }

        if (currentPage + 2 <= totalPages) {
            page3Button.setText(String.valueOf(currentPage + 2));
            page3Button.setVisible(true);
            page3Button.setOnAction(event -> {
                currentPage += 2;
                showPage(currentPage);
                updatePaginationButtons();
            });
        } else {
            page3Button.setVisible(false);
        }
    }

    public static class Employee {
        private final String nombreCompleto;
        private final String id;
        private final String fechaEntrada;
        private final String horaEntrada;
        private final String horaSalida;
        private final String tiempoLaborado;
        private final String tipoAsistencia;
        private final String tipoSalida;
        private final String estado;

        public Employee(String nombreCompleto, String id, String fechaEntrada, String horaEntrada, String horaSalida, String tiempoLaborado, String tipoAsistencia, String tipoSalida, String estado) {
            this.nombreCompleto = nombreCompleto;
            this.id = id;
            this.fechaEntrada = fechaEntrada;
            this.horaEntrada = horaEntrada;
            this.horaSalida = horaSalida;
            this.tiempoLaborado = tiempoLaborado;
            this.tipoAsistencia = tipoAsistencia;
            this.tipoSalida = tipoSalida;
            this.estado = estado;
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

        public String getTipoAsistencia() {
            return tipoAsistencia;
        }

        public String getTipoSalida() {
            return tipoSalida;
        }

        public String getEstado() {
            return estado;
        }
    }
}
