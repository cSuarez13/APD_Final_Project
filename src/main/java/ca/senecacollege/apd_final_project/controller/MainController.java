package ca.senecacollege.apd_final_project.controller;

import ca.senecacollege.apd_final_project.service.DialogService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController extends BaseController {

    public BorderPane mainBorderPane;
    @FXML
    private Button btnKiosk;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    @FXML
    private void handleKioskButton() {
        try {
            LoggingManager.logSystemInfo("Opening kiosk interface");

            // Load the kiosk welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent kioskRoot = loader.load();

            // Create a new stage for the kiosk
            Stage kioskStage = new Stage();
            Scene kioskScene = new Scene(kioskRoot);

            // Apply the kiosk CSS
            kioskScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());
            kioskScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());

            // Configure stage size and position
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);
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
            DialogService.showError(getStage(), "Navigation Error",
                    "Error loading kiosk interface", e);
        }
    }

    @FXML
    private void handleAdminButton() {
        try {
            LoggingManager.logSystemInfo("Opening admin login interface");

            // Load the admin login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_LOGIN));
            Parent adminLoginRoot = loader.load();

            // Create a new stage for admin login
            Stage adminLoginStage = new Stage();
            Scene adminLoginScene = new Scene(adminLoginRoot);

            // Apply the admin CSS
            adminLoginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());
            adminLoginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());

            // Configure stage size and position
            double stageWidth = ScreenSizeManager.calculateStageWidth(500);
            double stageHeight = ScreenSizeManager.calculateStageHeight(600);
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
            DialogService.showError(getStage(), "Navigation Error",
                    "Error loading admin login interface", e);
        }
    }

    @FXML
    private void handleFeedbackLink() {
        try {
            // Load the feedback screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_FEEDBACK));
            Parent feedbackRoot = loader.load();

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene feedbackScene = new Scene(feedbackRoot);
            feedbackScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            feedbackScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the scene
            stage.setScene(feedbackScene);

            // Size the stage properly
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            LoggingManager.logSystemInfo("Navigated to feedback screen");

        } catch (IOException e) {
            LoggingManager.logException("Error loading feedback screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error loading feedback screen", e);
        }
    }

    @Override
    protected Stage getStage() {
        if (btnKiosk != null && btnKiosk.getScene() != null) {
            return (Stage) btnKiosk.getScene().getWindow();
        }
        return null;
    }
}