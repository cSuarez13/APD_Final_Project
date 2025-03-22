package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label lblAdminName;

    @FXML
    private StackPane contentArea;

    // Navigation buttons
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnSearchGuests;
    @FXML
    private Button btnReservations;
    @FXML
    private Button btnCheckIn;
    @FXML
    private Button btnCheckOut;
    @FXML
    private Button btnReports;

    // Admin data
    private Admin currentAdmin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LoggingManager.logSystemInfo("AdminDashboardController initialized");
    }

    public void initData(Admin admin) {
        this.currentAdmin = admin;
        lblAdminName.setText(admin.getName());
        LoggingManager.logSystemInfo("AdminDashboardController initialized with admin: " + admin.getUsername());
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        try {
            // Clear content area
            contentArea.getChildren().clear();

            Button clickedButton = (Button) event.getSource();

            // Reset all button styles
            resetNavButtonStyles();

            // Set active style to clicked button
            clickedButton.getStyleClass().add("nav-button-active");

            // Load appropriate view
            Parent view = null;

            if (clickedButton.equals(btnDashboard)) {
                view = FXMLLoader.load(getClass().getResource(Constants.FXML_DASHBOARD_CONTENT));
            } else if (clickedButton.equals(btnSearchGuests)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_SEARCH_GUEST));
                view = loader.load();
                SearchGuestController controller = loader.getController();
                controller.initData(currentAdmin);
            } else if (clickedButton.equals(btnReservations)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_RESERVATIONS));
                view = loader.load();
                ReservationController controller = loader.getController();
                controller.initData(currentAdmin);
            } else if (clickedButton.equals(btnCheckIn)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_CHECKIN));
                view = loader.load();
                CheckInController controller = loader.getController();
                controller.initData(currentAdmin);
            } else if (clickedButton.equals(btnCheckOut)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_CHECKOUT));
                view = loader.load();
                CheckoutController controller = loader.getController();
                controller.initData(currentAdmin);
            } else if (clickedButton.equals(btnReports)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_REPORT));
                view = loader.load();
                ReportController controller = loader.getController();
                controller.initData(currentAdmin);
            }

            // Add view to content area
            if (view != null) {
                contentArea.getChildren().add(view);
            }

        } catch (IOException e) {
            LoggingManager.logException("Error loading view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error loading requested view: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LoggingManager.logAdminActivity(currentAdmin.getUsername(), "Logged out");

        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_LOGIN));
            Parent loginRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) mainPane.getScene().getWindow();

            // Create new scene
            Scene loginScene = new Scene(loginRoot);
            loginScene.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            // Set the new scene
            stage.setScene(loginScene);
            stage.setMaximized(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            LoggingManager.logException("Error navigating to login screen", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error returning to login screen: " + e.getMessage());
        }
    }

    /**
     * Reset all navigation button styles
     */
    private void resetNavButtonStyles() {
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnSearchGuests.getStyleClass().remove("nav-button-active");
        btnReservations.getStyleClass().remove("nav-button-active");
        btnCheckIn.getStyleClass().remove("nav-button-active");
        btnCheckOut.getStyleClass().remove("nav-button-active");
        btnReports.getStyleClass().remove("nav-button-active");
    }

    /**
     * Helper method to show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

        alert.showAndWait();
    }
}