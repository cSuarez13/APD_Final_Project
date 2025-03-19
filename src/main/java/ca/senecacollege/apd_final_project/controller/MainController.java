package ca.senecacollege.apd_final_project.controller;

import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
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
            kioskScene.getStylesheets().add(getClass().getResource(Constants.CSS_MAIN).toExternalForm());

            // Configure stage size using ScreenSizeManager
            Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);

            // Center the stage
            double[] stagePosition = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Set stage properties
            kioskStage.setWidth(stageWidth);
            kioskStage.setHeight(stageHeight);
            kioskStage.setX(stagePosition[0]);
            kioskStage.setY(stagePosition[1]);

            kioskStage.setTitle("Hotel ABC Kiosk");
            kioskStage.setScene(kioskScene);
            kioskStage.show();

            LoggingManager.logSystemInfo("Kiosk interface opened");

        } catch (IOException e) {
            LoggingManager.logException("Error opening kiosk interface", e);

            // Get the current stage
            Stage stage = (Stage) btnKiosk.getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "NAV-001", "Error loading kiosk interface");
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
            adminLoginScene.getStylesheets().add(getClass().getResource(Constants.CSS_MAIN).toExternalForm());

            // Configure stage size using ScreenSizeManager
            Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();
            double stageWidth = ScreenSizeManager.calculateStageWidth(500);
            double stageHeight = ScreenSizeManager.calculateStageHeight(600);

            // Center the stage
            double[] stagePosition = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Set stage properties
            adminLoginStage.setWidth(stageWidth);
            adminLoginStage.setHeight(stageHeight);
            adminLoginStage.setX(stagePosition[0]);
            adminLoginStage.setY(stagePosition[1]);

            adminLoginStage.setTitle("Admin Login");
            adminLoginStage.setScene(adminLoginScene);
            adminLoginStage.show();

            LoggingManager.logSystemInfo("Admin login interface opened");

        } catch (IOException e) {
            LoggingManager.logException("Error opening admin login interface", e);

            // Get the current stage
            Stage stage = (Stage) btnAdmin.getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "NAV-002", "Error loading admin login interface");
        }
    }
}