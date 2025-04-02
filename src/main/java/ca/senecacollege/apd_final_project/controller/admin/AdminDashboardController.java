package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminDashboardController extends BaseController {

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        LoggingManager.logSystemInfo("AdminDashboardController initialized");
    }

    @Override
    public void initData(Admin admin) {
        super.initData(admin);
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
                view = loadFXML();
            } else if (clickedButton.equals(btnSearchGuests)) {
                view = loadControllerView(Constants.FXML_SEARCH_GUEST, SearchGuestController.class);
            } else if (clickedButton.equals(btnReservations)) {
                view = loadControllerView(Constants.FXML_RESERVATIONS, ReservationController.class);
            } else if (clickedButton.equals(btnCheckIn)) {
                view = loadControllerView(Constants.FXML_CHECKIN, CheckInController.class);
            } else if (clickedButton.equals(btnCheckOut)) {
                view = loadControllerView(Constants.FXML_CHECKOUT, CheckoutController.class);
            } else if (clickedButton.equals(btnReports)) {
                view = loadControllerView(Constants.FXML_REPORT, ReportController.class);
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

    /**
     * Load a FXML file
     */
    private Parent loadFXML() throws IOException {
        URL resourceUrl = getClass().getResource(Constants.FXML_DASHBOARD_CONTENT);
        if (resourceUrl == null) {
            throw new IOException("Resource not found: " + Constants.FXML_DASHBOARD_CONTENT);
        }
        return FXMLLoader.load(resourceUrl);
    }

    /**
     * Load a view with its controller and initialize data
     */
    private Parent loadControllerView(String fxmlPath, Class<?> controllerClass) throws IOException {
        URL resourceUrl = getClass().getResource(fxmlPath);
        if (resourceUrl == null) {
            throw new IOException("Resource not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resourceUrl);
        Parent view = loader.load();
        Object controller = loader.getController();

        // Use reflection to call initData method if it exists
        try {
            if (controller instanceof BaseController) {
                ((BaseController) controller).initData(currentAdmin);
            } else {
                // Try to find and invoke initData method manually
                Method initDataMethod = controllerClass.getMethod("initData", Admin.class);
                initDataMethod.invoke(controller, currentAdmin);
            }
        } catch (Exception e) {
            LoggingManager.logException("Error initializing controller data", e);
        }

        return view;
    }

    @FXML
    private void handleLogout() {
        logAdminActivity("Logged out");

        try {
            // Get the current stage and close it
            Stage currentStage = getStage();
            if (currentStage != null) {
                currentStage.close();
            }

            // Load the admin login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_LOGIN));
            Parent adminLoginRoot = loader.load();

            // Create a new stage for admin login
            Stage adminLoginStage = new Stage();
            Scene adminLoginScene = new Scene(adminLoginRoot);

            // Apply the admin CSS
            adminLoginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());
            adminLoginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());

            // Configure stage size and position
            double stageWidth = ScreenSizeManager.calculateStageWidth(500);
            double stageHeight = ScreenSizeManager.calculateStageHeight(600);
            double[] stagePosition = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Set stage properties
            adminLoginStage.setWidth(stageWidth);
            adminLoginStage.setHeight(stageHeight);
            adminLoginStage.setX(stagePosition[0]);
            adminLoginStage.setY(stagePosition[1]);
            adminLoginStage.setTitle("Admin Login");
            adminLoginStage.setScene(adminLoginScene);
            adminLoginStage.show();

            LoggingManager.logSystemInfo("Admin login interface opened");

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
        Button[] navButtons = {
                btnDashboard, btnSearchGuests, btnReservations,
                btnCheckIn, btnCheckOut, btnReports
        };

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-button-active");
        }
    }

    @Override
    protected Stage getStage() {
        return mainPane != null && mainPane.getScene() != null
                ? (Stage) mainPane.getScene().getWindow()
                : null;
    }
}