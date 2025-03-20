package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.util.*;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeScreenController implements Initializable {

    @FXML
    private Label lblWelcome;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnRules;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set welcome message
        lblWelcome.setText(Constants.WELCOME_MESSAGE);

        // Setup welcome animation
        setupWelcomeAnimation();

        LoggingManager.logSystemInfo("WelcomeScreenController initialized");
    }

    private void setupWelcomeAnimation() {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), lblWelcome);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @FXML
    private void handleStartButton(ActionEvent event) {
        try {
            LoggingManager.logSystemInfo("Opening booking interface");

            // Load the booking screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_BOOKING));
            Parent bookingRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) btnStart.getScene().getWindow();

            // Calculate dimensions using ScreenSizeManager - use 85% of screen height
            Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();
            double aspectRatio = 1024.0 / 768.0; // Original aspect ratio
            double targetHeight = screenBounds.getHeight() * 0.95; // 85% of screen height
            double targetWidth = targetHeight * aspectRatio;

            // Create new scene with calculated dimensions
            Scene bookingScene = new Scene(bookingRoot, targetWidth, targetHeight);
            // Apply both stylesheets - order matters
            bookingScene.getStylesheets().add(getClass().getResource(Constants.CSS_MAIN).toExternalForm());
            bookingScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
            // Calculate center position
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(targetWidth, targetHeight);

            // Apply position and scene
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);
            stage.setWidth(targetWidth);
            stage.setHeight(targetHeight);
            stage.setScene(bookingScene);
            stage.setMaximized(false);
            stage.show();

            LoggingManager.logSystemInfo("Navigated to booking screen with dimensions: " +
                    targetWidth + "x" + targetHeight);
        } catch (IOException e) {
            LoggingManager.logException("Error navigating to booking screen", e);

            // Use ErrorPopupManager to show error
            Stage stage = (Stage) btnStart.getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "NAV-001", "Error loading booking screen");
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        RulesDialogUtility.showRulesDialog(btnRules);
    }
}