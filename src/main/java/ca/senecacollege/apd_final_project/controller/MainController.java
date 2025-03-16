package ca.senecacollege.apd_final_project.controller;

import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button btnKiosk;

    @FXML
    private Button btnAdmin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LoggingManager.logSystemInfo("MainController initialized");
    }

    @FXML
    private void handleKioskButton(ActionEvent event) {
        try {
            LoggingManager.logSystemInfo("Opening kiosk interface");

            // Load the kiosk welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent kioskRoot = loader.load();

            // Create a new stage for the kiosk
            Stage kioskStage = new Stage();
            Scene kioskScene = new Scene(kioskRoot);

            // Apply the kiosk CSS
            kioskScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Configure and show the kiosk stage
            kioskStage.setTitle("Hotel ABC Kiosk");
            kioskStage.setScene(kioskScene);
            kioskStage.setMaximized(true); // Full screen for kiosk mode
            kioskStage.show();

        } catch (IOException e) {
            LoggingManager.logException("Error opening kiosk interface", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminButton(ActionEvent event) {
        try {
            LoggingManager.logSystemInfo("Opening admin login interface");

            // Load the admin login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_LOGIN));
            Parent adminLoginRoot = loader.load();

            // Create a new stage for admin login
            Stage adminLoginStage = new Stage();
            Scene adminLoginScene = new Scene(adminLoginRoot);

            // Apply the admin CSS
            adminLoginScene.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            // Configure and show the admin login stage
            adminLoginStage.setTitle("Admin Login");
            adminLoginStage.setScene(adminLoginScene);
            adminLoginStage.setResizable(false);
            adminLoginStage.show();

        } catch (IOException e) {
            LoggingManager.logException("Error opening admin login interface", e);
            e.printStackTrace();
        }
    }
}