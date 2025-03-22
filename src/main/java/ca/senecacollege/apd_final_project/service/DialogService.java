package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Centralized service for creating and showing dialog boxes
 */
public class DialogService {

    /**
     * Show an information alert dialog
     *
     * @param owner The owner stage
     * @param title The dialog title
     * @param message The dialog message
     */
    public static void showInformation(Stage owner, String title, String message) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, owner, title, message);
        alert.showAndWait();
    }

    /**
     * Show a warning alert dialog
     *
     * @param owner The owner stage
     * @param title The dialog title
     * @param message The dialog message
     */
    public static void showWarning(Stage owner, String title, String message) {
        Alert alert = createAlert(Alert.AlertType.WARNING, owner, title, message);
        alert.showAndWait();
    }

    /**
     * Show an error alert dialog
     *
     * @param owner The owner stage
     * @param title The dialog title
     * @param message The dialog message
     */
    public static void showError(Stage owner, String title, String message) {
        Alert alert = createAlert(Alert.AlertType.ERROR, owner, title, message);
        alert.showAndWait();

        // Log the error
        LoggingManager.logSystemError(title + ": " + message, null);
    }

    /**
     * Show an error alert dialog with exception details
     *
     * @param owner The owner stage
     * @param title The dialog title
     * @param message The dialog message
     * @param exception The exception
     */
    public static void showError(Stage owner, String title, String message, Throwable exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (exception != null) {
            // Create expandable Exception section
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("Exception details:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
        }

        // Apply CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(DialogService.class.getResource(Constants.CSS_ADMIN).toExternalForm());

        // Log the error
        LoggingManager.logException(title + ": " + message, exception);

        alert.showAndWait();
    }

    /**
     * Show a non-intrusive error popup
     *
     * @param owner The owner stage
     * @param message The error message
     */
    public static void showErrorPopup(Stage owner, String message) {
        ErrorPopupManager.showErrorPopup(owner, message);
    }

    /**
     * Show a confirmation dialog
     *
     * @param owner The owner stage
     * @param title The dialog title
     * @param message The dialog message
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(Stage owner, String title, String message) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, owner, title, message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show a custom input dialog
     *
     * @param owner The owner stage
     * @param title The dialog title
     * @param message The dialog message
     * @param defaultValue The default value for the input field
     * @return The user input, or null if the dialog was cancelled
     */
    public static String showInputDialog(Stage owner, String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        if (owner != null) {
            dialog.initOwner(owner);
        }

        // Apply CSS
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(DialogService.class.getResource(Constants.CSS_ADMIN).toExternalForm());

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Show a custom dialog with content
     *
     * @param owner The owner stage
     * @param title The dialog title
     * @param content The dialog content
     * @return The button type that was clicked
     */
    public static ButtonType showCustomDialog(Stage owner, String title, VBox content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        if (owner != null) {
            dialog.initOwner(owner);
        }

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getStylesheets().add(DialogService.class.getResource(Constants.CSS_ADMIN).toExternalForm());

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        return dialog.showAndWait().orElse(ButtonType.CANCEL);
    }

    /**
     * Create an alert with the appropriate type and styling
     *
     * @param type The alert type
     * @param owner The owner stage
     * @param title The dialog title
     * @param message The dialog message
     * @return The configured alert
     */
    private static Alert createAlert(Alert.AlertType type, Stage owner, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (owner != null) {
            alert.initOwner(owner);
        }

        // Apply CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(DialogService.class.getResource(Constants.CSS_ADMIN).toExternalForm());

        return alert;
    }
}