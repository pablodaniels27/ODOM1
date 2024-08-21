package views;

import controllers.PreLoginView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class POSApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar PreLoginView.fxml primero
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PreLoginView.fxml"));
        Parent root = loader.load();

        PreLoginView preLoginController = loader.getController();
        preLoginController.setPrimaryStage(primaryStage);  // Pasar la referencia de la ventana principal al controlador

        // Configurar la escena y la ventana principal
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ODOM. SA DE CV");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/ODOM.jpg"))));
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        // Establecer el tamaño mínimo de la ventana principal
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(480);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}