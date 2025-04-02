package ca.senecacollege.apd_final_project.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Utility class for displaying the hotel rules dialog
 */
public class RulesDialogUtility {

    /**
     * Show the hotel rules and regulations dialog
     * @param parentNode Any node from the current scene (used to get the parent stage)
     */
    public static void showRulesDialog(Node parentNode) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Hotel Rules & Regulations");

        // Get the stage from the source to properly parent the dialog
        Stage stage = (Stage) parentNode.getScene().getWindow();
        dialog.initOwner(stage);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(RulesDialogUtility.class.getResource(Constants.CSS_KIOSK)).toExternalForm());
        dialogPane.setStyle("-fx-background-color: #2d2d2d;");

        // Calculate dialog size based on screen dimensions
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
        rulesText.setPrefHeight(dialogHeight - 120); // Reduced height to prevent overflow
        rulesText.setMaxHeight(dialogHeight - 120);
        rulesText.setWrapText(true);
        rulesText.setStyle(
                "-fx-control-inner-background: #2c2c2c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-padding: 15px;" +
                        "-fx-background-color: #2c2c2c;" +
                        "-fx-background-insets: 0;" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-width: 0;"
        );

        // Vertical layout
        VBox content = new VBox(15);
        content.setStyle("-fx-background-color: #2d2d2d;");
        content.getChildren().addAll(header, rulesText);
        content.setPadding(new Insets(10, 10, 10, 10));

        dialogPane.setContent(content);

        // Customize close button
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.setStyle(
                "-fx-background-color: #7b1fa2;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        try {
            dialog.showAndWait();
        } catch (Exception e) {
            LoggingManager.logException("Error showing rules dialog", e);
            // Handle the error gracefully - show an error popup instead
            ErrorPopupManager.showSystemErrorPopup(stage, "DIALOG-001",
                    "Unable to display rules. Please try again later.");
        }
    }
}