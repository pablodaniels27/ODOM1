package controllers;

import Lector.EnrollmentFormController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistroController {

    @FXML
    private TextField nombreField;
    @FXML
    private TextField apellidoMaternoField;
    @FXML
    private TextField apellidoPaternoField;
    @FXML
    private TextField fechaNacimientoField;
    @FXML
    private TextField paisField;
    @FXML
    private TextField ciudadField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ladaField;
    @FXML
    private TextField telefonoField;
    @FXML
    private TextField rfcField;
    @FXML
    private TextField curpField;
    @FXML
    private TextField profesionField;

    @FXML
    private ComboBox<String> departamentoComboBox;
    @FXML
    private ComboBox<String> puestoComboBox;

    @FXML
    private ImageView imageView;

    @FXML
    private void initialize() {
        System.out.println("Inicializando...");
        departamentoComboBox.setMinWidth(150);  // Asegura un tamaño mínimo para el ComboBox
        departamentoComboBox.setPrefWidth(200); // Tamaño preferido para mejor visualización
        departamentoComboBox.setVisibleRowCount(5); // Ajusta la cantidad de elementos visibles


        // Crear ObservableList para departamentos
        ObservableList<String> departamentos = FXCollections.observableArrayList();
        departamentos.addAll("Administración", "Construcción", "Terminal 2");

        // Crear ObservableList para puestos
        ObservableList<String> puestos = FXCollections.observableArrayList();
        puestos.addAll("Líder", "Supervisor", "Empleado");

        // Asignar las listas al ComboBox
        departamentoComboBox.setItems(departamentos);
        puestoComboBox.setItems(puestos);

        // Verifica que los items se carguen correctamente
        System.out.println("Departamentos cargados: " + departamentoComboBox.getItems());
        System.out.println("Puestos cargados: " + puestoComboBox.getItems());

        // Seleccionar un valor predeterminado
        departamentoComboBox.getSelectionModel().select("Construcción");  // Selecciona "Construcción" por defecto
        puestoComboBox.getSelectionModel().select(1);  // Selecciona el segundo puesto (Supervisor) por defecto

        // Manejar la selección en el ComboBox de departamentos
        departamentoComboBox.setOnAction(event -> {
            String selectedDepartamento = departamentoComboBox.getSelectionModel().getSelectedItem();
            System.out.println("Departamento seleccionado: " + selectedDepartamento);
        });

        // Manejar la selección en el ComboBox de puestos
        puestoComboBox.setOnAction(event -> {
            String selectedPuesto = puestoComboBox.getSelectionModel().getSelectedItem();
            System.out.println("Puesto seleccionado: " + selectedPuesto);
        });

    }

    private void cargarDepartamentos() {
        ObservableList<String> departamentos = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM departamentos";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                departamentos.add(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        departamentoComboBox.setItems(departamentos);
        System.out.println("departamentos cargados");
    }

    private void cargarPuestos() {
        ObservableList<String> puestos = FXCollections.observableArrayList();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM jerarquias";  // La tabla `jerarquias` representa los puestos
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                puestos.add(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        puestoComboBox.setItems(puestos);
        System.out.println("puestos cargados");
    }

    @FXML
    private void enviarDatos() {
        // Obtener los datos de los campos de texto
        String nombre = nombreField.getText();
        String apellidoMaterno = apellidoMaternoField.getText();
        String apellidoPaterno = apellidoPaternoField.getText();
        String fechaNacimiento = fechaNacimientoField.getText();
        String pais = paisField.getText();
        String ciudad = ciudadField.getText();
        String email = emailField.getText();
        String lada = ladaField.getText();
        String telefono = telefonoField.getText();
        String rfc = rfcField.getText();
        String curp = curpField.getText();
        String profesion = profesionField.getText();

        // Obtener los valores seleccionados en los ComboBox
        String departamentoSeleccionado = departamentoComboBox.getSelectionModel().getSelectedItem();
        String puestoSeleccionado = puestoComboBox.getSelectionModel().getSelectedItem();

        // Conexión a la base de datos e inserción de datos
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Encontrar el ID del departamento seleccionado
            String departamentoQuery = "SELECT id FROM departamentos WHERE nombre = ?";
            PreparedStatement departamentoStatement = connection.prepareStatement(departamentoQuery);
            departamentoStatement.setString(1, departamentoSeleccionado);
            ResultSet departamentoResult = departamentoStatement.executeQuery();
            int departamentoId = 0;
            if (departamentoResult.next()) {
                departamentoId = departamentoResult.getInt("id");
            }

            // Encontrar el ID del puesto (jerarquía) seleccionado
            String puestoQuery = "SELECT id FROM jerarquias WHERE nombre = ?";
            PreparedStatement puestoStatement = connection.prepareStatement(puestoQuery);
            puestoStatement.setString(1, puestoSeleccionado);
            ResultSet puestoResult = puestoStatement.executeQuery();
            int jerarquiaId = 0;
            if (puestoResult.next()) {
                jerarquiaId = puestoResult.getInt("id");
            }

            // Insertar los datos del empleado
            String sql = "INSERT INTO empleados (nombres, apellido_materno, apellido_paterno, fecha_nacimiento, pais, ciudad, correo_electronico, lada, telefono, rfc, curp, profesion, departamento_id, jerarquia_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, apellidoMaterno);
            statement.setString(3, apellidoPaterno);
            statement.setString(4, fechaNacimiento);
            statement.setString(5, pais);
            statement.setString(6, ciudad);
            statement.setString(7, email);
            statement.setString(8, lada);
            statement.setString(9, telefono);
            statement.setString(10, rfc);
            statement.setString(11, curp);
            statement.setString(12, profesion);
            statement.setInt(13, departamentoId);  // Usar el ID del departamento
            statement.setInt(14, jerarquiaId);     // Usar el ID de la jerarquía

            statement.executeUpdate();

            // Aquí podrías añadir un mensaje de confirmación o redirigir a la vista de listado de empleados
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleIngresarHuellaButton(ActionEvent event) {
        openEnrollmentForm();
    }

    private void openEnrollmentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EnrollmentForm.fxml"));
            Parent root = loader.load();

            // Si necesitas pasar datos entre controladores, puedes obtener el controlador y configurarlo aquí.
            EnrollmentFormController controller = loader.getController();
            // Pasar datos al controlador si es necesario

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Enrollment Form");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
