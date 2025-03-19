package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;
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

            // Configure and show the stage - properly fit to screen
            stage.setScene(bookingScene);

            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Set stage size to fit screen (or use a percentage of screen)
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            // Set minimum size to ensure elements don't get squished
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            LoggingManager.logSystemInfo("Navigated to booking screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating to booking screen", e);

            // More detailed error information for debugging
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();

            // Create an alert to show the error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Error Loading Booking Screen");
            alert.setContentText("Error: " + e.getMessage() + "\nPlease contact technical support.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        // Create an improved rules and regulations dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Hotel Rules & Regulations");

        // Create a custom dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().addAll("root", "rules-dialog");

        // Set size for dialog
        dialogPane.setPrefWidth(800);
        dialogPane.setPrefHeight(600);

        // Create a VBox for content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Add header
        Label header = new Label("Please Read Our Rules & Regulations");
        header.getStyleClass().add("header");

        // Add scrollable text area for rules content
        TextArea rulesText = new TextArea(Constants.RULES_REGULATIONS);
        rulesText.setEditable(false);
        rulesText.setWrapText(true);
        rulesText.setPrefHeight(350);
        rulesText.setPrefWidth(600);
        rulesText.getStyleClass().add("content");

        // Add components to content
        content.getChildren().addAll(header, rulesText);

        // Set the content
        dialogPane.setContent(content);

        // Add the close button
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        // Show the dialog
        dialog.showAndWait();
    }
}