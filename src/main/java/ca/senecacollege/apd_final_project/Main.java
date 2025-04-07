package ca.senecacollege.apd_final_project;

import ca.senecacollege.apd_final_project.server.AdminServer;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {
    private static ExecutorService executorService;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize logging
            LoggingManager.initialize();
            LoggingManager.logSystemInfo("Application starting...");

            // Load the main FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_MAIN));
            Parent root = loader.load();

            // Set up the scene with CSS
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());

            // Configure and show the primary stage
            primaryStage.setTitle(Constants.APP_NAME);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            LoggingManager.logSystemInfo("Main window loaded successfully");

            // Start the admin server in a background thread
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                try {
                    AdminServer server = new AdminServer(Constants.SERVER_PORT);
                    server.start();
                } catch (IOException e) {
                    LoggingManager.logException("Failed to start admin server", e);
                }
            });

        } catch (Exception e) {
            LoggingManager.logException("Error starting application", e);
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        LoggingManager.logSystemInfo("Application stopping...");

        // Shutdown the executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}