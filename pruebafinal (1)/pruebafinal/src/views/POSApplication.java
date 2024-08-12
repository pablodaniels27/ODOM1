package views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class POSApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));

        // Carga la escena desde el archivo FXML
        Scene scene = new Scene(loader.load());

        // Aplica el archivo CSS
        scene.getStylesheets().add(getClass().getResource("/resources/style.css").toExternalForm());

        // Establecer el ícono de la ventana con la imagen ODOM.jpg
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/ODOM.jpg")));

        // Configurar el título de la ventana
        primaryStage.setTitle("ODOM. SA DE CV");

        // Establecer la resolución a 720p
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        primaryStage.setMaximized(true);

        // Mostrar la ventana
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
