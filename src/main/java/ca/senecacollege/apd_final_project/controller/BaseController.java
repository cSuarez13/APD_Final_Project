package ca.senecacollege.apd_final_project.controller;

import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Base controller class that provides common functionality
 * Controllers should extend this class to inherit common methods and properties
 */
public abstract class BaseController implements Initializable {

    // Common properties for all controllers
    protected Admin currentAdmin;
    protected Label lblError;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Default implementation - can be overridden by subclasses
        LoggingManager.logSystemInfo(this.getClass().getSimpleName() + " initialized");
    }

    /**
     * Initialize controller with admin data
     * This method should be called after the controller is created to set the admin context
     * @param admin The admin user
     */
    public void initData(Admin admin) {
        this.currentAdmin = admin;
        LoggingManager.logSystemInfo(this.getClass().getSimpleName() + " initialized with admin: " + admin.getUsername());
    }

    /**
     * Show an error message in the UI
     * If lblError is defined, it will display the message there
     * Otherwise, it will show a popup
     * @param message The error message to display
     */
    protected void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        } else {
            // Get current scene's window as a Stage
            Stage stage = getStage();
            if (stage != null) {
                ErrorPopupManager.showSystemErrorPopup(stage, null, message);
            } else {
                LoggingManager.logException("Unable to show error: " + message, new Exception("No stage available"));
            }
        }
    }

    /**
     * Hide any displayed error message
     */
    protected void hideError() {
        if (lblError != null) {
            lblError.setVisible(false);
        }
    }

    /**
     * Show an alert dialog
     * @param type Alert type (INFO, WARNING, ERROR, etc.)
     * @param title Dialog title
     * @param message Dialog message
     */
    protected void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply CSS to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());

        alert.showAndWait();
    }

    /**
     * Get the stage from any control in the controller
     * This method should be overridden by controllers that need to access the stage
     * @return The stage
     */
    protected Stage getStage() {
        return null; // Override in concrete controllers
    }

    /**
     * Log admin activity
     * @param activity The activity description
     */
    protected void logAdminActivity(String activity) {
        if (currentAdmin != null) {
            LoggingManager.logAdminActivity(currentAdmin.getUsername(), activity);
        } else {
            LoggingManager.logSystemInfo("Admin activity (no admin context): " + activity);
        }
    }

    /**
     * Clear form fields
     * This method should be overridden by controllers with form fields to clear
     */
    protected void clearFields() {
        // Default empty implementation - override in subclasses
    }

    /**
     * Validate form fields
     * This method should be overridden by controllers with form fields to validate
     * @return true if validation passes, false otherwise
     */
    protected boolean validateFields() {
        // Default implementation - override in subclasses
        return false;
    }

    /**
     * Close the current window/stage
     * This is a convenience method for controllers that need to close their window
     */
    protected void closeWindow() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }
}