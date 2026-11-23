package com.biblioteca;

import com.biblioteca.persistence.LibroRepositorio;
import com.biblioteca.persistence.LibroRepositorioImpl;
import com.biblioteca.persistence.SagaRepositorio;
import com.biblioteca.persistence.SagaRepositorioImpl;
import com.biblioteca.service.EstadisticasService;
import com.biblioteca.service.LibroService;
import com.biblioteca.service.SagaService;
import com.biblioteca.ui.MainController;
import com.biblioteca.util.StorageConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de inicio para la aplicación JavaFX.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inicialización de la capa de persistencia sobre archivos serializados
            LibroRepositorio libroRepositorio = new LibroRepositorioImpl(StorageConfig.getLibrosFilePath());
            SagaRepositorio sagaRepositorio = new SagaRepositorioImpl(StorageConfig.getSagasFilePath());

            // Inicialización de la capa de servicios / lógica de negocio
            SagaService sagaService = new SagaService(sagaRepositorio, libroRepositorio);
            LibroService libroService = new LibroService(libroRepositorio, sagaRepositorio);
            EstadisticasService estadisticasService = new EstadisticasService(libroRepositorio, sagaRepositorio);

            // Carga de la vista principal en FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            // Configuración del controlador
            MainController controller = loader.getController();
            controller.initServices(libroService, sagaService, estadisticasService, primaryStage);

            Scene scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("Gestor de Biblioteca Personal y Sagas");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.show();

        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("Error crítico al iniciar la interfaz gráfica: " + t.getMessage());
            try {
                String logContent = "Error en inicio: " + t + "\n" + java.util.Arrays.toString(t.getStackTrace());
                java.nio.file.Files.writeString(java.nio.file.Path.of("/tmp/biblioteca_error.log"), logContent);
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
