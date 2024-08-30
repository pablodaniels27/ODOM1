package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class RegistroSucursalController {

    @FXML
    private VBox empleadosContainer;

    @FXML
    private VBox supervisoresContainer;

    @FXML
    private TextField searchEmpleadosField;

    @FXML
    private TextField searchSupervisoresField;

    private HBox selectedBox;

    @FXML
    public void initialize() {
        searchEmpleadosField.textProperty().addListener((observable, oldValue, newValue) -> buscarEmpleados(newValue));
        searchSupervisoresField.textProperty().addListener((observable, oldValue, newValue) -> buscarSupervisores(newValue));

        cargarEmpleados();
        cargarSupervisores();
    }

    private void cargarEmpleados() {
        empleadosContainer.getChildren().clear();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombres, apellido_paterno, profesion, estatus_id FROM empleados WHERE jerarquia_id = 3";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int empleadoId = resultSet.getInt("id");
                String nombre = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String profesion = resultSet.getString("profesion");
                int estatusId = resultSet.getInt("estatus_id");

                HBox empleadoBox = crearEmpleadoBox(empleadoId, nombre, apellidoPaterno, profesion, estatusId);
                empleadosContainer.getChildren().add(empleadoBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarSupervisores() {
        supervisoresContainer.getChildren().clear();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombres, apellido_paterno, profesion, estatus_id FROM empleados WHERE jerarquia_id = 2";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int supervisorId = resultSet.getInt("id");
                String nombre = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String profesion = resultSet.getString("profesion");
                int estatusId = resultSet.getInt("estatus_id");

                HBox supervisorBox = crearEmpleadoBox(supervisorId, nombre, apellidoPaterno, profesion, estatusId);
                supervisoresContainer.getChildren().add(supervisorBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox crearEmpleadoBox(int empleadoId, String nombre, String apellidoPaterno, String profesion, int estatusId) {
        HBox empleadoBox = new HBox();
        empleadoBox.setStyle("-fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: white;");
        empleadoBox.setSpacing(10);
        empleadoBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        empleadoBox.setMinHeight(Region.USE_COMPUTED_SIZE);

        ImageView statusIcon = new ImageView();
        statusIcon.setFitHeight(10);
        statusIcon.setFitWidth(10);

        String statusImagePath;
        switch (estatusId) {
            case 1:
                statusImagePath = "/resources/verde.jpg";
                break;
            case 2:
                statusImagePath = "/resources/amarillo.jpg";
                break;
            case 3:
                statusImagePath = "/resources/gris.jpg";
                break;
            case 4:
                statusImagePath = "/resources/rojo.jpg";
                break;
            default:
                statusImagePath = "/resources/gris.jpg";
                break;
        }

        Image statusImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(statusImagePath)));
        statusIcon.setImage(statusImage);

        VBox textContainer = new VBox();
        textContainer.setSpacing(5);

        Label nombreLabel = new Label(nombre.toUpperCase() + " " + apellidoPaterno.toUpperCase());
        nombreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label profesionLabel = new Label(profesion != null ? profesion : "Profesión no especificada");
        profesionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: grey;");

        textContainer.getChildren().addAll(nombreLabel, profesionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox botonesContainer = new VBox();
        botonesContainer.setSpacing(5);
        botonesContainer.setVisible(false);

        Button editarButton = new Button("Editar");
        Button eliminarButton = new Button("Eliminar");
        botonesContainer.getChildren().addAll(editarButton, eliminarButton);

        empleadoBox.getChildren().addAll(statusIcon, textContainer, spacer, botonesContainer);

        // Efecto hover
        empleadoBox.setOnMouseEntered(event -> {
            if (selectedBox != empleadoBox) {
                empleadoBox.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
            }
        });

        empleadoBox.setOnMouseExited(event -> {
            if (selectedBox != empleadoBox) {
                empleadoBox.setStyle("-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
            }
        });

        // Efecto de selección
        empleadoBox.setOnMouseClicked(event -> {
            if (selectedBox != null) {
                selectedBox.setStyle("-fx-background-color: white; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
                ((VBox) selectedBox.getChildren().get(3)).setVisible(false);
            }
            empleadoBox.setStyle("-fx-background-color: #d1e7dd; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
            selectedBox = empleadoBox;
            botonesContainer.setVisible(true);
        });

        return empleadoBox;
    }

    private void buscarEmpleados(String query) {
        empleadosContainer.getChildren().clear();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombres, apellido_paterno, profesion, estatus_id FROM empleados WHERE jerarquia_id = 3 " +
                    "AND (nombres LIKE '%" + query + "%' OR apellido_paterno LIKE '%" + query + "%')";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int empleadoId = resultSet.getInt("id");
                String nombre = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String profesion = resultSet.getString("profesion");
                int estatusId = resultSet.getInt("estatus_id");

                HBox empleadoBox = crearEmpleadoBox(empleadoId, nombre, apellidoPaterno, profesion, estatusId);
                empleadosContainer.getChildren().add(empleadoBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buscarSupervisores(String query) {
        supervisoresContainer.getChildren().clear();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombres, apellido_paterno, profesion, estatus_id FROM empleados WHERE jerarquia_id = 2 " +
                    "AND (nombres LIKE '%" + query + "%' OR apellido_paterno LIKE '%" + query + "%')";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int supervisorId = resultSet.getInt("id");
                String nombre = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String profesion = resultSet.getString("profesion");
                int estatusId = resultSet.getInt("estatus_id");

                HBox supervisorBox = crearEmpleadoBox(supervisorId, nombre, apellidoPaterno, profesion, estatusId);
                supervisoresContainer.getChildren().add(supervisorBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
