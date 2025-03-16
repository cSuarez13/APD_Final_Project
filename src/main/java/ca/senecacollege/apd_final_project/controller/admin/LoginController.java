package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.service.AdminService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblError;

    private AdminService adminService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        adminService = new AdminService();
        lblError.setVisible(false);

        // Set up enter key event for password field
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLoginButton(new ActionEvent());
            }
        });

        LoggingManager.logSystemInfo("LoginController initialized");
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required");
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
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            LoggingManager.logException("Error during login", e);
            showError("Login error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
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
            dashboardScene.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            dashboardStage.setTitle("Admin Dashboard - " + admin.getName());
            dashboardStage.setScene(dashboardScene);
            dashboardStage.setMaximized(true);
            dashboardStage.show();

            LoggingManager.logSystemInfo("Admin dashboard opened for: " + admin.getUsername());

        } catch (IOException e) {
            LoggingManager.logException("Error opening admin dashboard", e);
            showError("Error opening dashboard: " + e.getMessage());
        }
    }
}