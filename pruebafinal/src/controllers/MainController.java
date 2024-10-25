package controllers;

import Usuarios.Empleado;
import Usuarios.Lider;
import Usuarios.Supervisor;
import Usuarios.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import views.POSApplication;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private Button permisosButton;  // Botón de Permisos

    @FXML
    private Button auditoriaButton;  // Botón de Auditoría

    @FXML
    private Button registroButton;

    @FXML
    private  Button registroSucursalButton;

    @FXML
    private Button iniciobutton;

    @FXML
    private VBox loginContainer;

    @FXML
    private VBox mainContainer;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private StackPane mainContent;

    @FXML
    private Button editButton; // Botón que será usado para cargar la vista de edición

    private Usuario usuarioAutenticado;  // Para almacenar el usuario autenticado


    @FXML
    public void initialize() {
        // Inicialmente, el botón de edición está oculto
        editButton.setVisible(false);

        // Cargar la vista de inicio al cargar el MainView
        //showInicio();
    }



    public void setUsuarioAutenticado(Usuario usuario) {
        this.usuarioAutenticado = usuario;

        // Configurar la interfaz según el tipo de usuario
        if (usuario instanceof Supervisor) {
            Supervisor supervisor = (Supervisor) usuario;
            System.out.println("Permisos del supervisor: " + supervisor.getPermisos());  // Depuración para verificar permisos
            System.out.println("Usuario autenticado es un Supervisor. Aplicando restricciones...");
            permisosButton.setVisible(false);
            auditoriaButton.setVisible(false);
            showInicio();  // Mostrar inicio para el supervisor
        } else if (usuario instanceof Empleado) {
            System.out.println("Usuario autenticado es un Empleado. Aplicando restricciones...");
            permisosButton.setVisible(false);
            auditoriaButton.setVisible(false);
            registroButton.setVisible(false);
            registroSucursalButton.setVisible(false);
            iniciobutton.setVisible(false);
            showMonitoreo();  // Mostrar directamente la vista de monitoreo
        } else if (usuario instanceof Lider) {
            System.out.println("Usuario autenticado es un Líder. No se aplican restricciones.");
            showInicio();  // Mostrar inicio para el líder
        }
    }


    // Método para verificar permisos antes de acceder a una vista
    private boolean tienePermiso(String permiso) {
        if (usuarioAutenticado instanceof Supervisor) {
            Supervisor supervisor = (Supervisor) usuarioAutenticado;
            return supervisor.tienePermiso(permiso);
        }
        return true; // Para otros tipos de usuario que no son supervisores, asumimos que tienen acceso.
    }


    @FXML
    public void showInicio() {
        loadContent("/views/InicioView.fxml");
    }


    @FXML
    public void showRegistro() {
        if (tienePermiso("Ver Registro")) { // Verificar si tiene el permiso necesario
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegistroView.fxml"));
                Parent root = loader.load();

                // Obtener el controlador de la vista de registro
                RegistroController registroController = loader.getController();

                // Pasar el usuario autenticado al controlador de registro
                registroController.setUsuarioAutenticado(usuarioAutenticado);


                // Cargar la vista en el mainContent
                mainContent.getChildren().clear();
                mainContent.getChildren().add(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAccessDeniedAlert();
        }
    }

    @FXML
    public void showRegistroSucursal() {


        if (tienePermiso("Ver Gestión de empleados") && (usuarioAutenticado instanceof Lider)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegistroSucursalView.fxml"));
                Parent root = loader.load();

                // Obtener el controlador de la vista
                RegistroSucursalController registroSucursalController = loader.getController();
                registroSucursalController.setUsuarioAutenticado(usuarioAutenticado); // Pasar el usuario autenticado

                mainContent.getChildren().clear();
                mainContent.getChildren().add(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showRegistroSucursal2();

        }
    }

    @FXML
    public void showRegistroSucursal2() {
        if (tienePermiso("Ver Gestión de empleados")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegistroSucursalView2.fxml"));
                Parent root = loader.load();

                // Obtener el controlador de la vista
                RegistroSucursalController2 registroSucursalController = loader.getController();
                registroSucursalController.setUsuarioAutenticado(usuarioAutenticado); // Pasar el usuario autenticado

                mainContent.getChildren().clear();
                mainContent.getChildren().add(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAccessDeniedAlert();
        }
    }


    @FXML
    public void showMonitoreo() {
        if (tienePermiso("Ver Monitoreo")) { // Verificar si tiene el permiso necesario
            loadContent("/views/MonitoreoView.fxml");
        } else {
            showAccessDeniedAlert();
        }
    }




    @FXML
    public void showPermisos() {
        if (!(usuarioAutenticado instanceof Supervisor)) {
            loadContent("/views/Permisos.fxml");
        } else {
            showAccessDeniedAlert();
        }
    }

    @FXML
    public void showAuditoria() {
        if (!(usuarioAutenticado instanceof Supervisor)) {
            loadContent("/views/Auditoria.fxml");
        } else {
            showAccessDeniedAlert();
        }
    }

    @FXML
    public void showAyuda() {
        loadContent("/views/AyudaView.fxml");
    }

    @FXML
    public void showConfiguración() {
        loadContent("/views/ConfiguracionView.fxml");
    }


    private void showAccessDeniedAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acceso denegado");
        alert.setHeaderText(null);
        alert.setContentText("No tienes permiso para acceder a esta vista.");
        alert.showAndWait();
    }


    @FXML
    public void showEdicion(int empleadoId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EdicionView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador de la vista de edición
            EdicionController edicionController = loader.getController();
            edicionController.cargarDatosEmpleado(empleadoId); // Método para cargar los datos del empleado en la vista de edición

            // Cargar la vista en el mainContent manteniendo la barra de navegación
            mainContent.getChildren().clear();
            mainContent.getChildren().add(root);

            // Mostrar el botón de edición en la barra azul
            editButton.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            Parent content = FXMLLoader.load(fxmlUrl);
            mainContent.getChildren().clear();
            mainContent.getChildren().add(content);

            // Ocultar el botón de edición siempre que se cargue otra vista
            editButton.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            POSApplication mainApp = new POSApplication();
            mainApp.start(window);  // Iniciar la aplicación principal

            // Obtener los límites visuales de la pantalla principal
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            // Establecer la posición y el tamaño de la ventana
            window.setX(bounds.getMinX());
            window.setY(bounds.getMinY());
            window.setWidth(bounds.getWidth());
            window.setHeight(bounds.getHeight());

            // Opcional: Eliminar la decoración de la ventana si lo deseas
            // window.initStyle(StageStyle.UNDECORATED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Método para ser llamado desde el botón de la barra azul para cargar la vista de edición
    @FXML
    private void handleEditAction() {
        showEdicion(-1); // Utiliza un id específico si tienes que cargar algo en particular
    }
}