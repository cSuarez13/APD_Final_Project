package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.TextArea;

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
        // Create the alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Rules & Regulations");
        alert.setHeaderText("Please Read Our Rules & Regulations");

        // Create a TextArea
        TextArea textArea = new TextArea();
        textArea.setText(Constants.RULES_REGULATIONS);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        // Set explicit text color and background to ensure visibility
        textArea.setStyle("-fx-text-fill: white; -fx-control-inner-background: #333333;");

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Increase dimensions for the text area - make it larger
        textArea.setPrefWidth(screenBounds.getWidth() * 0.7);  // Increased from 0.6 to 0.7
        textArea.setPrefHeight(screenBounds.getHeight() * 0.6); // Increased from 0.5 to 0.6

        // Replace the content with our text area
        alert.getDialogPane().setContent(textArea);

        // Apply styling to the dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().add("root");

        // Increase the dialog size
        dialogPane.setPrefWidth(750);
        dialogPane.setPrefHeight(550);

        // Make sure the stage resizes properly
        alert.setResizable(true);

        // Show the dialog
        alert.showAndWait();
    }
}