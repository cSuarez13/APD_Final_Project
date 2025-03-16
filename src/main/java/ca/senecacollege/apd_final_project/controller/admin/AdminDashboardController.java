package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label lblAdminName;

    @FXML
    private Label lblAdminRole;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabDashboard;

    @FXML
    private Tab tabSearch;

    @FXML
    private Tab tabReservations;

    @FXML
    private Tab tabCheckIn;

    @FXML
    private Tab tabCheckOut;

    @FXML
    private Tab tabReports;

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

    // Search tab components
    @FXML
    private TextField txtSearchTerm;

    @FXML
    private ComboBox<String> cmbSearchType;

    @FXML
    private TableView<Guest> tblSearchResults;

    @FXML
    private TableView<Reservation> tblGuestReservations;

    // Reservations tab components
    @FXML
    private TableView<Reservation> tblAllReservations;

    @FXML
    private ComboBox<String> cmbReservationFilter;

    @FXML
    private Button btnCancelReservation;

    // Check-in tab components
    @FXML
    private TextField txtCheckInReservationId;

    @FXML
    private Button btnSearchCheckIn;

    @FXML
    private Label lblCheckInGuestName;

    @FXML
    private Label lblCheckInDates;

    @FXML
    private Label lblCheckInRoomInfo;

    @FXML
    private Button btnConfirmCheckIn;

    // Check-out tab components
    @FXML
    private TextField txtCheckOutReservationId;

    @FXML
    private Button btnSearchCheckOut;

    @FXML
    private Label lblCheckOutGuestName;

    @FXML
    private Label lblCheckOutDates;

    @FXML
    private Label lblCheckOutRoomInfo;

    @FXML
    private TextField txtDiscount;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblTax;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnConfirmCheckOut;

    // Reports tab components
    @FXML
    private ComboBox<String> cmbReportType;

    @FXML
    private DatePicker dpReportStartDate;

    @FXML
    private DatePicker dpReportEndDate;

    @FXML
    private Button btnGenerateReport;

    // Admin data
    private Admin currentAdmin;

    // Services
    private GuestService guestService;
    private ReservationService reservationService;

    // Data collections
    private ObservableList<Guest> searchResults = FXCollections.observableArrayList();
    private ObservableList<Reservation> guestReservations = FXCollections.observableArrayList();
    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();
    private ObservableList<Reservation> todayCheckIns = FXCollections.observableArrayList();
    private ObservableList<Reservation> todayCheckOuts = FXCollections.observableArrayList();

    // Selected reservation for check-in/check-out
    private Reservation selectedCheckInReservation;
    private Reservation selectedCheckOutReservation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Initialize search type combo box
        cmbSearchType.setItems(FXCollections.observableArrayList("Name", "Phone", "Email"));
        cmbSearchType.getSelectionModel().selectFirst();

        // Initialize reservation filter combo box
        cmbReservationFilter.setItems(FXCollections.observableArrayList(
                "All", "Active", "Confirmed", "Checked In", "Checked Out", "Cancelled"));
        cmbReservationFilter.getSelectionModel().selectFirst();

        // Initialize report type combo box
        cmbReportType.setItems(FXCollections.observableArrayList(
                "Occupancy Report", "Revenue Report", "Guest Feedback Report"));
        cmbReportType.getSelectionModel().selectFirst();

        // Initialize date pickers for reports
        dpReportStartDate.setValue(LocalDate.now().minusMonths(1));
        dpReportEndDate.setValue(LocalDate.now());

        // Set up table views
        setupTableViews();

        // Set up listeners
        setupListeners();

        LoggingManager.logSystemInfo("AdminDashboardController initialized");
    }

    /**
     * Initialize the controller with admin data
     *
     * @param admin The logged-in admin
     */
    public void initData(Admin admin) {
        this.currentAdmin = admin;

        // Set admin info
        lblAdminName.setText(admin.getName());
        lblAdminRole.setText(admin.getRole());

        // Load initial data
        refreshDashboard();

        LoggingManager.logSystemInfo("AdminDashboardController initialized with admin: " + admin.getUsername());
    }

    /**
     * Set up the table views
     */
    private void setupTableViews() {
        // Setup search results table
        TableColumn<Guest, String> colGuestId = new TableColumn<>("ID");
        colGuestId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getGuestID())));

        TableColumn<Guest, String> colGuestName = new TableColumn<>("Name");
        colGuestName.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Guest, String> colGuestPhone = new TableColumn<>("Phone");
        colGuestPhone.setCellValueFactory(data -> data.getValue().phoneNumberProperty());

        TableColumn<Guest, String> colGuestEmail = new TableColumn<>("Email");
        colGuestEmail.setCellValueFactory(data -> data.getValue().emailProperty());

        tblSearchResults.getColumns().addAll(colGuestId, colGuestName, colGuestPhone, colGuestEmail);
        tblSearchResults.setItems(searchResults);

        // Setup guest reservations table
        setupReservationsTable(tblGuestReservations, guestReservations);

        // Setup all reservations table
        setupReservationsTable(tblAllReservations, allReservations);

        // Setup today's check-ins table
        setupReservationsTable(tblTodayCheckIns, todayCheckIns);

        // Setup today's check-outs table
        setupReservationsTable(tblTodayCheckOuts, todayCheckOuts);
    }

    /**
     * Helper method to setup a reservations table
     *
     * @param tableView The table view to setup
     * @param data The data to populate the table with
     */
    private void setupReservationsTable(TableView<Reservation> tableView, ObservableList<Reservation> data) {
        TableColumn<Reservation, String> colReservationId = new TableColumn<>("ID");
        colReservationId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getReservationID())));

        TableColumn<Reservation, String> colGuestName = new TableColumn<>("Guest");
        colGuestName.setCellValueFactory(cellData -> {
            try {
                Guest guest = guestService.getGuestById(cellData.getValue().getGuestID());
                return new SimpleStringProperty(guest.getName());
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown");
            }
        });

        TableColumn<Reservation, String> colCheckIn = new TableColumn<>("Check-in");
        colCheckIn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCheckInDate().toString()));

        TableColumn<Reservation, String> colCheckOut = new TableColumn<>("Check-out");
        colCheckOut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCheckOutDate().toString()));

        TableColumn<Reservation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        tableView.getColumns().addAll(colReservationId, colGuestName, colCheckIn, colCheckOut, colStatus);
        tableView.setItems(data);
    }

    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Listen for tab selection changes
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == tabDashboard) {
                refreshDashboard();
            } else if (newValue == tabReservations) {
                refreshAllReservations();
            }
        });

        // Listen for reservation filter changes
        cmbReservationFilter.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> refreshAllReservations());

        // Listen for search results selection
        tblSearchResults.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadGuestReservations(newValue.getGuestID());
                    } else {
                        guestReservations.clear();
                    }
                });

        // Listen for discount input changes to update total
        txtDiscount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedCheckOutReservation != null) {
                updateBillingInfo();
            }
        });
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
     * Refresh all reservations based on filter
     */
    private void refreshAllReservations() {
        try {
            String filter = cmbReservationFilter.getValue();
            List<Reservation> reservations;

            if ("All".equals(filter)) {
                // TODO: Implement method to get all reservations
                reservations = reservationService.getActiveReservations(); // Temporary
            } else if ("Active".equals(filter)) {
                reservations = reservationService.getActiveReservations();
            } else if ("Confirmed".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(Reservation.STATUS_CONFIRMED);
            } else if ("Checked In".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(Reservation.STATUS_CHECKED_IN);
            } else if ("Checked Out".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(Reservation.STATUS_CHECKED_OUT);
            } else if ("Cancelled".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(Reservation.STATUS_CANCELLED);
            } else {
                reservations = reservationService.getActiveReservations(); // Default
            }

            allReservations.clear();
            allReservations.addAll(reservations);

        } catch (Exception e) {
            LoggingManager.logException("Error refreshing reservations", e);
            showAlert(Alert.AlertType.ERROR, "Reservations Error",
                    "Error refreshing reservations: " + e.getMessage());
        }
    }

    /**
     * Load reservations for a specific guest
     *
     * @param guestId The guest ID
     */
    private void loadGuestReservations(int guestId) {
        try {
            List<Reservation> reservations = reservationService.getReservationsByGuest(guestId);
            guestReservations.clear();
            guestReservations.addAll(reservations);
        } catch (Exception e) {
            LoggingManager.logException("Error loading guest reservations", e);
            showAlert(Alert.AlertType.ERROR, "Guest Reservations Error",
                    "Error loading guest reservations: " + e.getMessage());
        }
    }

    /**
     * Update billing information for check-out
     */
    private void updateBillingInfo() {
        if (selectedCheckOutReservation == null) {
            return;
        }

        try {
            double discountAmount = 0.0;

            if (!txtDiscount.getText().isEmpty()) {
                try {
                    discountAmount = Double.parseDouble(txtDiscount.getText());
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }

            // Calculate subtotal (room price * nights)
            int nights = selectedCheckOutReservation.calculateNumberOfNights();
            double roomPrice = 100.0; // Placeholder - would get from room service
            double subtotal = roomPrice * nights;

            // Calculate tax
            double tax = subtotal * Constants.TAX_RATE;

            // Calculate total
            double total = subtotal + tax - discountAmount;

            // Update labels
            lblSubtotal.setText(String.format("$%.2f", subtotal));
            lblTax.setText(String.format("$%.2f", tax));
            lblTotal.setText(String.format("$%.2f", total));

        } catch (Exception e) {
            LoggingManager.logException("Error updating billing info", e);
        }
    }

    /**
     * Handle search button click
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = txtSearchTerm.getText().trim();

        if (searchTerm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Search",
                    "Please enter a search term.");
            return;
        }

        try {
            String searchType = cmbSearchType.getValue();
            List<Guest> results;

            if ("Name".equals(searchType)) {
                results = guestService.searchGuestsByName(searchTerm);
            } else if ("Phone".equals(searchType)) {
                results = guestService.searchGuestsByPhone(searchTerm);
            } else if ("Email".equals(searchType)) {
                results = guestService.searchGuestsByEmail(searchTerm);
            } else {
                results = guestService.searchGuestsByName(searchTerm);
            }

            searchResults.clear();
            searchResults.addAll(results);

            if (results.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Results",
                        "No guests found matching your search criteria.");
            }

        } catch (Exception e) {
            LoggingManager.logException("Error searching for guests", e);
            showAlert(Alert.AlertType.ERROR, "Search Error",
                    "Error searching for guests: " + e.getMessage());
        }
    }

    /**
     * Handle cancel reservation button click
     */
    @FXML
    private void handleCancelReservation(ActionEvent event) {
        Reservation selectedReservation = tblAllReservations.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a reservation to cancel.");
            return;
        }

        if (selectedReservation.getStatus().equals(Reservation.STATUS_CHECKED_IN) ||
                selectedReservation.getStatus().equals(Reservation.STATUS_CHECKED_OUT) ||
                selectedReservation.getStatus().equals(Reservation.STATUS_CANCELLED)) {
            showAlert(Alert.AlertType.WARNING, "Cannot Cancel",
                    "This reservation cannot be cancelled. Current status: " +
                            selectedReservation.getStatus());
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Cancellation");
        confirmDialog.setHeaderText("Cancel Reservation #" + selectedReservation.getReservationID());
        confirmDialog.setContentText("Are you sure you want to cancel this reservation?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationService.cancelReservation(selectedReservation.getReservationID());
                    LoggingManager.logAdminActivity(currentAdmin.getUsername(),
                            "Cancelled reservation #" + selectedReservation.getReservationID());

                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Reservation cancelled successfully.");

                    refreshAllReservations();

                } catch (Exception e) {
                    LoggingManager.logException("Error cancelling reservation", e);
                    showAlert(Alert.AlertType.ERROR, "Cancellation Error",
                            "Error cancelling reservation: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Handle search for check-in button click
     */
    @FXML
    private void handleSearchCheckIn(ActionEvent event) {
        String reservationIdText = txtCheckInReservationId.getText().trim();

        if (reservationIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Input",
                    "Please enter a reservation ID.");
            return;
        }

        try {
            int reservationId = Integer.parseInt(reservationIdText);
            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                showAlert(Alert.AlertType.WARNING, "Not Found",
                        "Reservation not found.");
                return;
            }

            if (!reservation.getStatus().equals(Reservation.STATUS_CONFIRMED)) {
                showAlert(Alert.AlertType.WARNING, "Cannot Check In",
                        "This reservation cannot be checked in. Current status: " +
                                reservation.getStatus());
                return;
            }

            selectedCheckInReservation = reservation;

            // Get guest info
            Guest guest = guestService.getGuestById(reservation.getGuestID());

            // Update UI
            lblCheckInGuestName.setText(guest.getName());
            lblCheckInDates.setText(reservation.getCheckInDate() + " to " +
                    reservation.getCheckOutDate());
            lblCheckInRoomInfo.setText("Room #" + reservation.getRoomID()); // Would include room type in a real app

            btnConfirmCheckIn.setDisable(false);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input",
                    "Please enter a valid reservation ID.");
        } catch (Exception e) {
            LoggingManager.logException("Error searching for check-in", e);
            showAlert(Alert.AlertType.ERROR, "Search Error",
                    "Error searching for reservation: " + e.getMessage());
        }
    }

    /**
     * Handle confirm check-in button click
     */
    @FXML
    private void handleConfirmCheckIn(ActionEvent event) {
        if (selectedCheckInReservation == null) {
            return;
        }

        try {
            reservationService.checkIn(selectedCheckInReservation.getReservationID());

            LoggingManager.logAdminActivity(currentAdmin.getUsername(),
                    "Checked in reservation #" + selectedCheckInReservation.getReservationID());

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Check-in completed successfully.");

            // Reset UI
            txtCheckInReservationId.clear();
            lblCheckInGuestName.setText("");
            lblCheckInDates.setText("");
            lblCheckInRoomInfo.setText("");
            btnConfirmCheckIn.setDisable(true);
            selectedCheckInReservation = null;

            // Refresh dashboard
            refreshDashboard();

        } catch (Exception e) {
            LoggingManager.logException("Error during check-in", e);
            showAlert(Alert.AlertType.ERROR, "Check-in Error",
                    "Error checking in: " + e.getMessage());
        }
    }

    /**
     * Handle search for check-out button click
     */
    @FXML
    private void handleSearchCheckOut(ActionEvent event) {
        String reservationIdText = txtCheckOutReservationId.getText().trim();

        if (reservationIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Input",
                    "Please enter a reservation ID.");
            return;
        }

        try {
            int reservationId = Integer.parseInt(reservationIdText);
            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                showAlert(Alert.AlertType.WARNING, "Not Found",
                        "Reservation not found.");
                return;
            }

            if (!reservation.getStatus().equals(Reservation.STATUS_CHECKED_IN)) {
                showAlert(Alert.AlertType.WARNING, "Cannot Check Out",
                        "This reservation cannot be checked out. Current status: " +
                                reservation.getStatus());
                return;
            }

            selectedCheckOutReservation = reservation;

            // Get guest info
            Guest guest = guestService.getGuestById(reservation.getGuestID());

            // Update UI
            lblCheckOutGuestName.setText(guest.getName());
            lblCheckOutDates.setText(reservation.getCheckInDate() + " to " +
                    reservation.getCheckOutDate());
            lblCheckOutRoomInfo.setText("Room #" + reservation.getRoomID()); // Would include room type in a real app

            // Update billing info
            txtDiscount.setText("0.00");
            updateBillingInfo();

            btnConfirmCheckOut.setDisable(false);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input",
                    "Please enter a valid reservation ID.");
        } catch (Exception e) {
            LoggingManager.logException("Error searching for check-out", e);
            showAlert(Alert.AlertType.ERROR, "Search Error",
                    "Error searching for reservation: " + e.getMessage());
        }
    }

    /**
     * Handle confirm check-out button click
     */
    @FXML
    private void handleConfirmCheckOut(ActionEvent event) {
        if (selectedCheckOutReservation == null) {
            return;
        }

        try {
            // In a real app, we would create a bill record here

            // Check out the guest
            reservationService.checkOut(selectedCheckOutReservation.getReservationID());

            LoggingManager.logAdminActivity(currentAdmin.getUsername(),
                    "Checked out reservation #" + selectedCheckOutReservation.getReservationID());

            // Show feedback reminder dialog
            Alert feedbackReminder = new Alert(Alert.AlertType.INFORMATION);
            feedbackReminder.setTitle("Feedback Reminder");
            feedbackReminder.setHeaderText("Remind Guest About Feedback");
            feedbackReminder.setContentText("Please remind the guest that they can use the kiosk " +
                    "to provide feedback about their stay.");
            feedbackReminder.showAndWait();

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Check-out completed successfully.");

            // Reset UI
            txtCheckOutReservationId.clear();
            lblCheckOutGuestName.setText("");
            lblCheckOutDates.setText("");
            lblCheckOutRoomInfo.setText("");
            txtDiscount.clear();
            lblSubtotal.setText("$0.00");
            lblTax.setText("$0.00");
            lblTotal.setText("$0.00");
            btnConfirmCheckOut.setDisable(true);
            selectedCheckOutReservation = null;

            // Refresh dashboard
            refreshDashboard();

        } catch (Exception e) {
            LoggingManager.logException("Error during check-out", e);
            showAlert(Alert.AlertType.ERROR, "Check-out Error",
                    "Error checking out: " + e.getMessage());
        }
    }

    /**
     * Handle generate report button click
     */
    @FXML
    private void handleGenerateReport(ActionEvent event) {
        String reportType = cmbReportType.getValue();
        LocalDate startDate = dpReportStartDate.getValue();
        LocalDate endDate = dpReportEndDate.getValue();

        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Dates",
                    "Please select start and end dates for the report.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date Range",
                    "Start date must be before or equal to end date.");
            return;
        }

        // In a real app, we would generate the actual report here

        LoggingManager.logAdminActivity(currentAdmin.getUsername(),
                "Generated " + reportType + " for " + startDate + " to " + endDate);

        showAlert(Alert.AlertType.INFORMATION, "Report Generated",
                reportType + " has been generated for the period " +
                        startDate + " to " + endDate + ".");
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
     *
     * @param type The alert type
     * @param title The alert title
     * @param message The alert message
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