package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

            // Create new scene
            Scene bookingScene = new Scene(bookingRoot);
            bookingScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Configure stage size using ScreenSizeManager
            Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);

            // Center the stage
            double[] stagePosition = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Set stage properties
            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(stagePosition[0]);
            stage.setY(stagePosition[1]);
            stage.setScene(bookingScene);

            LoggingManager.logSystemInfo("Navigated to booking screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating to booking screen", e);

            // Use ErrorPopupManager to show error
            Stage stage = (Stage) btnStart.getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "NAV-001", "Error loading booking screen");
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Hotel Rules & Regulations");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.setStyle("-fx-background-color: #2d2d2d;");

        Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();
        double dialogWidth = Math.min(600, screenBounds.getWidth() * 0.7);
        double dialogHeight = Math.min(700, screenBounds.getHeight() * 0.8);

        dialogPane.setPrefWidth(dialogWidth);
        dialogPane.setPrefHeight(dialogHeight);

        // Header
        Label header = new Label("Hotel Rules & Regulations");
        header.setStyle(
                "-fx-text-fill: #b491c8;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 20px;"
        );
        header.setAlignment(Pos.CENTER);
        header.setPrefWidth(dialogWidth);

        // Rules TextArea
        TextArea rulesText = new TextArea(Constants.RULES_REGULATIONS);
        rulesText.setEditable(false);
        rulesText.setPrefHeight(dialogHeight - 80);
        rulesText.setMaxHeight(dialogHeight - 80);
        rulesText.setPrefRowCount(Integer.MAX_VALUE);
        rulesText.setWrapText(true);
        rulesText.setStyle(
                "-fx-control-inner-background: #2c2c2c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-padding: 20px;" +
                        "-fx-background-color: #2c2c2c;" +
                        "-fx-background-insets: 0;" +
                        "-fx-background-radius: 0;" +
                        "-fx-box-border: none;"
        );

        // Vertical layout
        VBox content = new VBox(15);
        content.setStyle("-fx-background-color: #2d2d2d;");
        content.getChildren().addAll(header, rulesText);

        dialogPane.setContent(content);

        // Customize close button
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeButton instanceof Button) {
            ((Button) closeButton).setStyle(
                    "-fx-background-color: #7b1fa2;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;"
            );
        }

        dialog.showAndWait();
    }
}