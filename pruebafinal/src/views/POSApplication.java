package views;

import controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class POSApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
        Parent root = loader.load();

        LoginController loginController = loader.getController();
        loginController.setPrimaryStage(primaryStage);  // Pasar la referencia de la ventana principal al controlador
        // Establecer el ícono de la ventana con la imagen ODOM.jpg


        // Configurar el título de la ventana


        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ODOM. SA DE CV");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/ODOM.jpg")));
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        primaryStage.show();



    }

    public static void main(String[] args) {
        launch(args);
    }
}
