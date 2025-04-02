package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.service.AdminService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    private AdminService adminService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        adminService = new AdminService();

        // Set up enter key event for password field
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLoginButton();
            }
        });

        // Adjust window size when scene is available
        txtUsername.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                adjustStageSize((Stage) newScene.getWindow());
            }
        });

        LoggingManager.logSystemInfo("LoginController initialized");
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize(Stage stage) {
        try {
            // Calculate appropriate size
            double stageWidth = ScreenSizeManager.calculateStageWidth(500);
            double stageHeight = ScreenSizeManager.calculateStageHeight(400);

            // Get center position
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Set size and position
            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            // Make sure it's not maximized
            stage.setMaximized(false);

            LoggingManager.logSystemInfo("Login screen size adjusted");
        } catch (Exception e) {
            LoggingManager.logException("Error adjusting login screen size", e);
        }
    }

    @FXML
    private void handleLoginButton() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Get current stage for error popups
        Stage stage = (Stage) btnLogin.getScene().getWindow();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            ErrorPopupManager.showValidationErrorPopup(stage, "Login", "Username and password are required");
            return;
        }

        try {
            // Authenticate admin
            Admin admin = adminService.authenticateAdmin(username, password);

            if (admin != null) {
                LoggingManager.logAdminActivity(username, "Successful login");

                // Open admin dashboard
                openAdminDashboard(admin);

                // Close the login window
                ((Stage) btnLogin.getScene().getWindow()).close();
            } else {
                LoggingManager.logSystemWarning("Failed login attempt for username: " + username);
                ErrorPopupManager.showValidationErrorPopup(stage, "Authentication", "Invalid username or password");
            }
        } catch (Exception e) {
            LoggingManager.logException("Error during login", e);
            ErrorPopupManager.showSystemErrorPopup(stage, "LOGIN-001", "Login error: " + e.getMessage());
        }
    }

    private void openAdminDashboard(Admin admin) {
        try {
            // Load the admin dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_ADMIN_DASHBOARD));
            Parent dashboardRoot = loader.load();

            // Get the controller and pass the admin data
            AdminDashboardController dashboardController = loader.getController();
            dashboardController.initData(admin);

            // Create and configure the dashboard stage
            Stage dashboardStage = new Stage();
            Scene dashboardScene = new Scene(dashboardRoot);

            // Apply the admin CSS
            dashboardScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            dashboardScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());

            dashboardStage.setTitle("Admin Dashboard - " + admin.getName());
            dashboardStage.setScene(dashboardScene);

            // Use ScreenSizeManager to set appropriate size
            double stageWidth = ScreenSizeManager.calculateStageWidth(1200);
            double stageHeight = ScreenSizeManager.calculateStageHeight(800);

            // Get center position
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Set size and position
            dashboardStage.setWidth(stageWidth);
            dashboardStage.setHeight(stageHeight);
            dashboardStage.setX(centerPos[0]);
            dashboardStage.setY(centerPos[1]);

            // Show maximized for admin dashboard
            dashboardStage.setMaximized(true);
            dashboardStage.show();

            LoggingManager.logSystemInfo("Admin dashboard opened for: " + admin.getUsername());

        } catch (IOException e) {
            LoggingManager.logException("Error opening admin dashboard", e);
            // Get the main stage for error popup
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "DASHBOARD-001", "Error opening dashboard: " + e.getMessage());
        }
    }
}