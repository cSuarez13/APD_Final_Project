package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label lblAdminName;

    @FXML
    private Label lblAdminRole;

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

    // Content area
    @FXML
    private StackPane contentArea;

    // Dashboard components
    @FXML
    private Label lblTodayCheckIns;

    @FXML
    private Label lblTodayCheckOuts;

    @FXML
    private Label lblActiveReservations;

    @FXML
    private TableView<Reservation> tblTodayCheckIns;

    @FXML
    private TableView<Reservation> tblTodayCheckOuts;

    // Admin data
    private Admin currentAdmin;

    // Services
    private GuestService guestService;
    private ReservationService reservationService;

    // Data collections for dashboard
    private ObservableList<Reservation> todayCheckIns = FXCollections.observableArrayList();
    private ObservableList<Reservation> todayCheckOuts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Initialize data collections
        todayCheckIns = FXCollections.observableArrayList();
        todayCheckOuts = FXCollections.observableArrayList();

        // Set up tables for dashboard
        setupDashboardTables();

        // Ensure data is loaded
        refreshDashboard();

        LoggingManager.logSystemInfo("AdminDashboardController initialized");
    }

    public void initData(Admin admin) {
        this.currentAdmin = admin;

        // Set admin info - add null check for lblAdminRole
        lblAdminName.setText(admin.getName());

        // Check if lblAdminRole exists before setting text
        if (lblAdminRole != null) {
            lblAdminRole.setText(admin.getRole());
        }

        // Load initial data
        refreshDashboard();

        LoggingManager.logSystemInfo("AdminDashboardController initialized with admin: " + admin.getUsername());
    }

    /**
     * Handle navigation button clicks
     */
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
            FXMLLoader loader = null;

            if (clickedButton.equals(btnDashboard)) {
                // Dashboard is handled in this controller
                loader = new FXMLLoader(getClass().getResource(Constants.FXML_ADMIN_DASHBOARD));
                Parent view = loader.load();
                contentArea.getChildren().add(view);
                refreshDashboard();
            } else if (clickedButton.equals(btnSearchGuests)) {
                loader = new FXMLLoader(getClass().getResource(Constants.FXML_SEARCH_GUEST));
                Parent view = loader.load();
                SearchGuestController controller = loader.getController();
                controller.initData(currentAdmin);
                contentArea.getChildren().add(view);
            } else if (clickedButton.equals(btnReservations)) {
                loader = new FXMLLoader(getClass().getResource(Constants.FXML_RESERVATIONS));
                Parent view = loader.load();
                ReservationController controller = loader.getController();
                controller.initData(currentAdmin);
                contentArea.getChildren().add(view);
            } else if (clickedButton.equals(btnCheckIn)) {
                loader = new FXMLLoader(getClass().getResource(Constants.FXML_CHECKIN));
                Parent view = loader.load();
                CheckInController controller = loader.getController();
                controller.initData(currentAdmin);
                contentArea.getChildren().add(view);
            } else if (clickedButton.equals(btnCheckOut)) {
                loader = new FXMLLoader(getClass().getResource(Constants.FXML_CHECKOUT));
                Parent view = loader.load();
                CheckoutController controller = loader.getController();
                controller.initData(currentAdmin);
                contentArea.getChildren().add(view);
            } else if (clickedButton.equals(btnReports)) {
                loader = new FXMLLoader(getClass().getResource(Constants.FXML_REPORT));
                Parent view = loader.load();
                ReportController controller = loader.getController();
                controller.initData(currentAdmin);
                contentArea.getChildren().add(view);
            }

        } catch (IOException e) {
            LoggingManager.logException("Error loading view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error loading requested view: " + e.getMessage());
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
     * Set up tables for dashboard view
     */
    private void setupDashboardTables() {
        // Setup tables for dashboard
        TableUtils.setupReservationsTable(tblTodayCheckIns, todayCheckIns, guestService);
        TableUtils.setupReservationsTable(tblTodayCheckOuts, todayCheckOuts, guestService);

        // Configure table column widths
        TableUtils.configureTableColumnWidth(tblTodayCheckIns);
        TableUtils.configureTableColumnWidth(tblTodayCheckOuts);
    }

    /**
     * Refresh the dashboard data
     */
    private void refreshDashboard() {
        try {
            // Load today's check-ins
            List<Reservation> checkIns = reservationService.getTodayCheckIns();
            todayCheckIns.clear();
            todayCheckIns.addAll(checkIns);
            lblTodayCheckIns.setText(String.valueOf(checkIns.size()));

            // Load today's check-outs
            List<Reservation> checkOuts = reservationService.getTodayCheckOuts();
            todayCheckOuts.clear();
            todayCheckOuts.addAll(checkOuts);
            lblTodayCheckOuts.setText(String.valueOf(checkOuts.size()));

            // Load active reservations count
            List<Reservation> active = reservationService.getActiveReservations();
            lblActiveReservations.setText(String.valueOf(active.size()));

        } catch (Exception e) {
            LoggingManager.logException("Error refreshing dashboard", e);
            showAlert(Alert.AlertType.ERROR, "Dashboard Error",
                    "Error refreshing dashboard data: " + e.getMessage());
        }
    }

    /**
     * Handle logout button click
     */
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