package ca.senecacollege.apd_final_project.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class ErrorPopupManager {
    /**
     * Show a non-intrusive error popup
     * @param parentStage The parent stage to anchor the popup
     * @param message The error message to display
     */
    public static void showErrorPopup(Stage parentStage, String message) {
        showErrorPopup(parentStage, message, 3);
    }

    /**
     * Show a non-intrusive error popup with custom duration
     * @param parentStage The parent stage to anchor the popup
     * @param message The error message to display
     * @param durationSeconds How long to show the popup
     */
    public static void showErrorPopup(Stage parentStage, String message, int durationSeconds) {
        Platform.runLater(() -> {
            // Create popup
            Popup popup = new Popup();

            // Create error message label
            Label errorLabel = new Label(message);
            errorLabel.getStyleClass().addAll("error-message", "popup-error");
            errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            // Create container
            VBox container = new VBox(errorLabel);
            container.setAlignment(Pos.CENTER);
            container.getStyleClass().add("error-popup-container");
            container.setStyle("-fx-background-color: rgba(207, 102, 121, 0.9); " +
                    "-fx-padding: 15px 20px; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-border-radius: 6px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);");

            // Add container to popup
            popup.getContent().add(container);

            // Position popup in the center top of the screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double popupWidth; // Estimated width
            // Estimated height

            popup.show(parentStage);
            // Now we can get the actual dimensions
            popupWidth = container.getWidth();
            container.getHeight();

            // Center horizontally, position at top quarter of screen
            popup.hide();
            popup.show(parentStage,
                    screenBounds.getMinX() + (screenBounds.getWidth() - popupWidth) / 2,
                    screenBounds.getMinY() + screenBounds.getHeight() * 0.25);

            // Auto-hide after specified duration
            PauseTransition delay = new PauseTransition(Duration.seconds(durationSeconds));
            delay.setOnFinished(event -> popup.hide());
            delay.play();
        });
    }

    /**
     * Show a validation error popup
     * @param parentStage The parent stage to anchor the popup
     * @param field The field with validation error
     * @param message The specific validation error message
     */
    public static void showValidationErrorPopup(Stage parentStage, String field, String message) {
        showErrorPopup(parentStage, field + ": " + message);
    }

    /**
     * Show a system error popup
     * @param parentStage The parent stage to anchor the popup
     * @param errorCode Optional error code
     * @param message The error message
     */
    public static void showSystemErrorPopup(Stage parentStage, String errorCode, String message) {
        String fullMessage = (errorCode != null ? "Error " + errorCode + ": " : "") + message;
        showErrorPopup(parentStage, fullMessage, 5);
    }
}