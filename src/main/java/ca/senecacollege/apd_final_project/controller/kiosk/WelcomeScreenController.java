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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    @FXML
    private MediaView mediaView;

    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set welcome message
        lblWelcome.setText(Constants.WELCOME_MESSAGE);

        // Setup welcome animation
        setupWelcomeAnimation();

        // Setup video guide if available
        setupVideoGuide();

        LoggingManager.logSystemInfo("WelcomeScreenController initialized");
    }

    private void setupWelcomeAnimation() {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), lblWelcome);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void setupVideoGuide() {
        try {
            URL videoResource = getClass().getResource("/videos/kiosk_guide.mp4");
            if (videoResource != null) {
                Media media = new Media(videoResource.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);

                // Loop the video
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();
            } else {
                // If video resource not found, hide the media view
                mediaView.setVisible(false);
                LoggingManager.logSystemWarning("Kiosk guide video not found");
            }
        } catch (Exception e) {
            // Handle media loading errors
            mediaView.setVisible(false);
            LoggingManager.logException("Error loading kiosk guide video", e);
        }
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
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        // Show rules and regulations dialog
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Rules & Regulations");
        alert.setHeaderText("Please Read Our Rules & Regulations");
        alert.setContentText(Constants.RULES_REGULATIONS);

        // Apply CSS to the dialog
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }

    @Override
    public void stop() {
        // Stop media player if it's running
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}