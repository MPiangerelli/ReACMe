package grafo.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;


/**
 * Questa classe permette di eseguire il codice per il process mining grazie ad un'interfaccia grafica
 * fornita grazie a JavaFX.
 *
 */
public class LogAnalyzerUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(new File("src/main/resources/views/home_v1.fxml").toURI().toURL());
        primaryStage.setTitle("Log Analyzer");
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();

    }
}
