package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegistroSucursalController {

    @FXML
    private VBox empleadosContainer; // Esto conecta el VBox del FXML con el controlador

    @FXML
    public void initialize() {
        System.out.println("Inicializando la vista y cargando empleados...");
        cargarEmpleados();
    }

    private void cargarEmpleados() {
        empleadosContainer.getChildren().clear();



        // Código para cargar empleados reales de la base de datos
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT nombres, profesion FROM empleados";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String nombre = resultSet.getString("nombres");
                String profesion = resultSet.getString("profesion");

                VBox realEmpleadoBox = new VBox();
                realEmpleadoBox.setStyle("-fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: white;");
                realEmpleadoBox.setSpacing(5);

                Label realNombreLabel = new Label(nombre.toUpperCase());
                realNombreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                Label realProfesionLabel = new Label(profesion != null ? profesion : "Profesión no especificada");
                realProfesionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: grey;");

                realEmpleadoBox.getChildren().addAll(realNombreLabel, realProfesionLabel);
                empleadosContainer.getChildren().add(realEmpleadoBox);

                System.out.println("Empleado añadido al VBox: " + nombre + " - " + profesion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
