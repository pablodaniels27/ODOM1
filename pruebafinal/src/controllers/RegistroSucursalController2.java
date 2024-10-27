package controllers;

import DAO.BaseDAO;
import Services.CacheService;
import Usuarios.Supervisor;
import Usuarios.Usuario;
import Usuarios.Lider;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.geometry.Pos;


public class RegistroSucursalController2 {

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

    @FXML
    private HBox mainContainer;


    private HBox selectedBox;
    private int empleadosOffset = 0;
    private int supervisoresOffset = 0;
    private static final int ITEMS_PER_PAGE = 14;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }






    @FXML
    public void initialize(Usuario usuario) {
        // Verificar que el usuario autenticado esté cargado antes de continuar



    }

    private Usuario usuarioAutenticado;

    public void setUsuarioAutenticado(Usuario usuario) {
        this.usuarioAutenticado = usuario;

        // Resetear contenedores antes de volver a cargar
        empleadosContainer.getChildren().clear();




        // Ajustar tamaño dinámico de los ScrollPane
        empleadosScroll.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                empleadosScroll.prefWidthProperty().bind(empleadosScroll.getScene().widthProperty().multiply(0.45));
                empleadosScroll.prefHeightProperty().bind(empleadosScroll.getScene().heightProperty().multiply(0.60));
            }
        });

        // Recargar empleados
        cargarEmpleados();

        // Listeners para los campos de búsqueda
        searchEmpleadosField.textProperty().addListener((observable, oldValue, newValue) -> {
            empleadosOffset = 0;
            empleadosContainer.getChildren().clear();
            cargarEmpleados(newValue);
        });



        // Configurar listener para scroll de empleados
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


    }

    private void cargarEmpleados() {
        cargarEmpleados("");
    }



    private void cargarEmpleados(String filtro) {
        if (empleadosOffset == 0) {
            empleadosContainer.getChildren().clear();
        }

        try {
            List<Map<String, Object>> empleados;

            // Si el usuario autenticado es un supervisor, cargar solo los empleados de su departamento
            if (usuarioAutenticado instanceof Supervisor) {
                Supervisor supervisor = (Supervisor) usuarioAutenticado;
                System.out.println("Cargando empleados del departamento: " + supervisor.getDepartamentoId());

                // Cargar empleados solo del departamento del supervisor
                empleados = BaseDAO.obtenerEmpleados(filtro, ITEMS_PER_PAGE, empleadosOffset * ITEMS_PER_PAGE, supervisor.getDepartamentoId());
            } else {
                // Cargar todos los empleados si no es un supervisor
                empleados = BaseDAO.obtenerEmpleados(filtro, ITEMS_PER_PAGE, empleadosOffset * ITEMS_PER_PAGE, null);
            }

            for (Map<String, Object> empleado : empleados) {
                int id = (int) empleado.get("id");
                String nombre = (String) empleado.get("nombres");
                String apellidoPaterno = (String) empleado.get("apellido_paterno");
                String profesion = (String) empleado.get("profesion");
                int estatusId = (int) empleado.get("estatus_id");

                HBox empleadoBox = crearEmpleadoBox(id, nombre, apellidoPaterno, profesion, estatusId);
                empleadosContainer.getChildren().add(empleadoBox);
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

        // Define el ícono basado en el estatus del empleado
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

        // Verificar si el usuario es un Supervisor
        if (usuarioAutenticado instanceof Supervisor) {
            Supervisor supervisor = (Supervisor) usuarioAutenticado;
            System.out.println("Permisos del supervisor en gestión: " + supervisor.getPermisos());

            // Verificar los permisos del supervisor
            if (supervisor.tienePermiso("Editar empleados")) {
                Button editarButton = new Button("Editar");
                editarButton.setOnAction(event -> cargarVistaEdicion(empleadoId));
                botonesContainer.getChildren().add(editarButton);
            }

            if (supervisor.tienePermiso("Dar de baja empleados")) {
                Button eliminarButton = new Button("Dar baja");
                eliminarButton.setOnAction(event -> darDeBajaEmpleado(empleadoId, empleadoBox));
                botonesContainer.getChildren().add(eliminarButton);
            }
        } else {
            // Si es un líder (no es un Supervisor), permitir editar y dar de baja
            Button editarButton = new Button("Editar");
            editarButton.setOnAction(event -> cargarVistaEdicion(empleadoId));
            botonesContainer.getChildren().add(editarButton);

            Button eliminarButton = new Button("Dar baja");
            eliminarButton.setOnAction(event -> darDeBajaEmpleado(empleadoId, empleadoBox));
            botonesContainer.getChildren().add(eliminarButton);
        }

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
        confirmacion.setTitle("Confirmar Dar de Baja");
        confirmacion.setHeaderText("¿Está seguro de dar de baja al empleado?");
        confirmacion.setContentText("El empleado será dado de baja y no aparecerá en la lista de empleados activos.");

        // Opción Sí o No en el popup
        ButtonType botonSi = new ButtonType("Sí");
        ButtonType botonNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(botonSi, botonNo);

        // Mostrar la alerta y esperar la respuesta del usuario
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == botonSi) {
                // Si el usuario confirma, procedemos a dar de baja al empleado
                try {
                    // Cambiar el estatus del empleado a 'Baja' usando el método en el DAO
                    BaseDAO.darDeBajaEmpleado(empleadoId);

                    // Registrar en los logs si el usuario es un Supervisor o un Líder
                    if (usuarioAutenticado instanceof Supervisor || usuarioAutenticado instanceof Lider) {
                        int userId = usuarioAutenticado.getId(); // Obtener el ID del supervisor o líder
                        String detalles = "Empleado con ID " + empleadoId + " fue dado de baja.";

                        // Registrar el cambio en los logs
                        BaseDAO.registrarCambioLogCambios(userId, "Dar de baja empleado", empleadoId, detalles);
                    }

                    // Eliminar la HBox del empleado de la vista después de actualizar la base de datos
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
                edicionController.setUsuarioAutenticado(usuarioAutenticado);  // Pasar el usuario autenticado a la vista de edición

                mainContent.getChildren().clear();
                mainContent.getChildren().add(editRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}