package controllers;

import Services.CacheService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @FXML
    private ScrollPane empleadosScroll;

    @FXML
    private ScrollPane supervisoresScroll;

    private HBox selectedBox;
    private int empleadosOffset = 0;
    private int supervisoresOffset = 0;
    private static final int ITEMS_PER_PAGE = 14;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Ajustar tamaño dinámico de los ScrollPane
        empleadosScroll.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                empleadosScroll.prefWidthProperty().bind(empleadosScroll.getScene().widthProperty().multiply(0.45));
                supervisoresScroll.prefWidthProperty().bind(supervisoresScroll.getScene().widthProperty().multiply(0.45));
                empleadosScroll.prefHeightProperty().bind(empleadosScroll.getScene().heightProperty().multiply(0.60));
                supervisoresScroll.prefHeightProperty().bind(supervisoresScroll.getScene().heightProperty().multiply(0.60));
            }
        });

        cargarEmpleados();
        cargarSupervisores();

        searchEmpleadosField.textProperty().addListener((observable, oldValue, newValue) -> {
            empleadosOffset = 0;
            empleadosContainer.getChildren().clear();
            cargarEmpleados(newValue);
        });

        searchSupervisoresField.textProperty().addListener((observable, oldValue, newValue) -> {
            supervisoresOffset = 0;
            supervisoresContainer.getChildren().clear();
            cargarSupervisores(newValue);
        });

        empleadosContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                setupScrollListeners();
            }
        });
    }

    private void setupScrollListeners() {
        empleadosScroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == empleadosScroll.getVmax()) {
                // Incrementar offset y cargar más datos
                empleadosOffset++;
                cargarEmpleados(searchEmpleadosField.getText());
            }
        });

        supervisoresScroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == supervisoresScroll.getVmax()) {
                // Incrementar offset y cargar más datos
                supervisoresOffset++;
                cargarSupervisores(searchSupervisoresField.getText());
            }
        });
    }

    private void cargarEmpleados() {
        cargarEmpleados("");
    }

    private void cargarSupervisores() {
        cargarSupervisores("");
    }

    private void cargarEmpleados(String filtro) {
        if (empleadosOffset == 0) {
            empleadosContainer.getChildren().clear();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Modificamos el SQL para que busque solo en las columnas nombres y apellido_paterno
            String sql = "SELECT id, nombres, apellido_paterno, profesion, estatus_id FROM empleados " +
                    "WHERE jerarquia_id = 3 AND estatus_id != 4 " +
                    "AND (nombres LIKE ? OR apellido_paterno LIKE ?) " +
                    "LIMIT ? OFFSET ?";
            PreparedStatement statement = connection.prepareStatement(sql);

            // Usamos el filtro para comparar con los nombres y apellido paterno
            String filtroSQL = "%" + filtro + "%";
            statement.setString(1, filtroSQL);
            statement.setString(2, filtroSQL);

            statement.setInt(3, ITEMS_PER_PAGE);
            statement.setInt(4, empleadosOffset * ITEMS_PER_PAGE);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String profesion = resultSet.getString("profesion");
                int estatusId = resultSet.getInt("estatus_id");

                HBox empleadoBox = crearEmpleadoBox(id, nombre, apellidoPaterno, profesion, estatusId);
                empleadosContainer.getChildren().add(empleadoBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarSupervisores(String filtro) {
        if (supervisoresOffset == 0) {
            supervisoresContainer.getChildren().clear();
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Similar ajuste al método de cargarEmpleados, búsqueda solo en nombres y apellido_paterno
            String sql = "SELECT id, nombres, apellido_paterno, profesion, estatus_id FROM empleados " +
                    "WHERE jerarquia_id = 2 AND estatus_id != 4 " +
                    "AND (nombres LIKE ? OR apellido_paterno LIKE ?) " +
                    "LIMIT ? OFFSET ?";
            PreparedStatement statement = connection.prepareStatement(sql);

            String filtroSQL = "%" + filtro + "%";
            statement.setString(1, filtroSQL);
            statement.setString(2, filtroSQL);

            statement.setInt(3, ITEMS_PER_PAGE);
            statement.setInt(4, supervisoresOffset * ITEMS_PER_PAGE);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String profesion = resultSet.getString("profesion");
                int estatusId = resultSet.getInt("estatus_id");

                HBox supervisorBox = crearEmpleadoBox(id, nombre, apellidoPaterno, profesion, estatusId);
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

        // Apilar nombre y apellido
        Label nombreLabel = new Label(nombre.toUpperCase());
        nombreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label apellidoPaternoLabel = new Label(apellidoPaterno.toUpperCase());
        apellidoPaternoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label profesionLabel = new Label(profesion != null ? profesion : "Profesión no especificada");
        profesionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: grey;");

        textContainer.getChildren().addAll(nombreLabel, apellidoPaternoLabel, profesionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox botonesContainer = new VBox();
        botonesContainer.setSpacing(5);
        botonesContainer.setVisible(false);

        Button editarButton = new Button("Editar");
        Button eliminarButton = new Button("Dar baja");
        eliminarButton.setOnAction(event -> darDeBajaEmpleado(empleadoId, empleadoBox));
        editarButton.setOnAction(event -> cargarVistaEdicion(empleadoId));
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
                selectedBox.getChildren().get(3).setVisible(false);
            }
            empleadoBox.setStyle("-fx-background-color: #d1e7dd; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-padding: 10;");
            selectedBox = empleadoBox;
            botonesContainer.setVisible(true);
        });

        return empleadoBox;
    }

    private void darDeBajaEmpleado(int empleadoId, HBox empleadoBox) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Baja de empleado");
        confirmacion.setHeaderText("¿Está seguro de dar de baja al empleado?");
        confirmacion.setContentText("El empleado será dado de baja y no aparecerá en la lista de empleados activos.");

        // Opción Sí o No en el popup
        ButtonType botonSi = new ButtonType("Continuar");
        ButtonType botonNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(botonSi, botonNo);

        // Mostrar la alerta y esperar la respuesta del usuario
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == botonSi) {
                // Si el usuario confirma, procedemos a dar de baja al empleado
                try (Connection connection = DatabaseConnection.getConnection()) {
                    String sql = "UPDATE empleados SET estatus_id = 4 WHERE id = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setInt(1, empleadoId);
                    statement.executeUpdate();

                    // Remover el empleado de la vista
                    empleadosContainer.getChildren().remove(empleadoBox);
                    supervisoresContainer.getChildren().remove(empleadoBox);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                // Si el usuario selecciona 'No', no se realiza ninguna acción.
                System.out.println("La acción de dar de baja fue cancelada.");
            }
        });
    }


    private void cargarVistaEdicion(int empleadoId) {
        try {
            Node root = empleadosContainer.getScene().getRoot();
            if (root instanceof Parent) {
                Parent parent = (Parent) root;
                StackPane mainContent = (StackPane) parent.lookup("#mainContent");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EdicionView.fxml"));
                Parent editRoot = loader.load();

                EdicionController edicionController = loader.getController();
                edicionController.cargarDatosEmpleado(empleadoId);

                mainContent.getChildren().clear();
                mainContent.getChildren().add(editRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
