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
import javafx.stage.Stage;
import javafx.util.Duration;
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
            // Load the booking screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_BOOKING));
            Parent bookingRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) btnStart.getScene().getWindow();

            // Create new scene
            Scene bookingScene = new Scene(bookingRoot);
            bookingScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the new scene
            stage.setScene(bookingScene);

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
        // Show rules and regulations dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Rules & Regulations");
        alert.setHeaderText("Please Read Our Rules & Regulations");
        alert.setContentText(Constants.RULES_REGULATIONS);

        // Apply CSS to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().add("root");

        for (Node node : dialogPane.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(javafx.scene.paint.Color.WHITE);
            }
        }

        // Set preferred width for better readability
        dialogPane.setPrefWidth(500);

        // Center the alert dialog on screen when it appears
        alert.setOnShown(e -> {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.centerOnScreen();
        });

        alert.showAndWait();
    }
}