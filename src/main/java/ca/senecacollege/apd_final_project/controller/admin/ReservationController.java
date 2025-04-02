package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.service.DialogService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.ServiceLocator;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationController extends BaseController {

    @FXML
    private ComboBox<String> cmbReservationFilter;

    @FXML
    private Button btnCancelReservation;

    @FXML
    private TableView<Reservation> tblAllReservations;

    private ReservationService reservationService;
    private final ObservableList<Reservation> allReservations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        LoggingManager.logSystemInfo("ReservationController initializing...");

        // Get services using ServiceLocator
        reservationService = ServiceLocator.getService(ReservationService.class);

        // Initialize reservation filter combo box
        cmbReservationFilter.setItems(FXCollections.observableArrayList(
                "All", "Active", "Confirmed", "Checked In", "Checked Out", "Cancelled"));
        cmbReservationFilter.getSelectionModel().selectFirst();

        LoggingManager.logSystemInfo("Setting up reservations table columns manually");

        // Set up columns manually
        setupTableColumns();

        // Bind data to table
        tblAllReservations.setItems(allReservations);

        // Add listener for filter changes
        cmbReservationFilter.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> refreshReservations());

        // Initially disable cancel button until a reservation is selected
        btnCancelReservation.setDisable(true);

        // Add listener to table selection for enabling/disabling cancel button
        tblAllReservations.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateCancelButtonState());

        LoggingManager.logSystemInfo("ReservationController initialization complete");
    }

    /**
     * Setup table columns manually instead of using TableUtils
     */
    private void setupTableColumns() {
        // Clear any existing columns
        tblAllReservations.getColumns().clear();

        // Set the column resize policy to fill the entire width
        tblAllReservations.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ID Column
        TableColumn<Reservation, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("reservationID"));
        idColumn.setMinWidth(80);

        // Guest ID Column
        TableColumn<Reservation, Integer> guestIdColumn = new TableColumn<>("Guest ID");
        guestIdColumn.setCellValueFactory(new PropertyValueFactory<>("guestID"));
        guestIdColumn.setMinWidth(100);

        // Room ID Column
        TableColumn<Reservation, Integer> roomIdColumn = new TableColumn<>("Room");
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("roomID"));
        roomIdColumn.setMinWidth(100);

        // Check-in Date Column
        TableColumn<Reservation, LocalDate> checkInColumn = new TableColumn<>("Check-in");
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkInColumn.setMinWidth(150);

        // Check-out Date Column
        TableColumn<Reservation, LocalDate> checkOutColumn = new TableColumn<>("Check-out");
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        checkOutColumn.setMinWidth(150);

        // Status Column
        TableColumn<Reservation, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData ->
                cellData.getValue().statusDisplayNameProperty());
        statusColumn.setMinWidth(120);

        // Add columns to the table
        tblAllReservations.getColumns().addAll(
                idColumn, guestIdColumn, roomIdColumn, checkInColumn, checkOutColumn, statusColumn);

        // Style to make headers more visible
        tblAllReservations.getStyleClass().add("admin-table");
    }

    /**
     * Update the cancel button state based on the selected reservation
     */
    private void updateCancelButtonState() {
        Reservation selectedReservation = tblAllReservations.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            btnCancelReservation.setDisable(true);
            return;
        }

        // Only enable cancel for reservations in PENDING or CONFIRMED status
        boolean canCancel = selectedReservation.getStatus() == ReservationStatus.PENDING ||
                selectedReservation.getStatus() == ReservationStatus.CONFIRMED;

        btnCancelReservation.setDisable(!canCancel);
    }

    @Override
    public void initData(Admin admin) {
        super.initData(admin);
        LoggingManager.logSystemInfo("ReservationController initData called, refreshing reservations...");
        refreshReservations();
        logAdminActivity("Viewed reservations list");
    }

    /**
     * Refresh reservations based on filter
     */
    private void refreshReservations() {
        try {
            String filter = cmbReservationFilter.getValue();
            LoggingManager.logSystemInfo("Refreshing reservations with filter: " + filter);

            List<Reservation> reservations;

            if ("All".equals(filter)) {
                reservations = reservationService.getActiveReservations();
            } else if ("Active".equals(filter)) {
                reservations = reservationService.getActiveReservations();
            } else if ("Confirmed".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMED);
            } else if ("Checked In".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_IN);
            } else if ("Checked Out".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_OUT);
            } else if ("Cancelled".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CANCELLED);
            } else {
                reservations = reservationService.getActiveReservations(); // Default
            }

            LoggingManager.logSystemInfo("Retrieved " + (reservations != null ? reservations.size() : 0) + " reservations");

            // For testing - add a dummy reservation if the list is empty
            if (reservations == null || reservations.isEmpty()) {
                LoggingManager.logSystemInfo("No reservations found, adding dummy data for testing");
                Reservation dummyReservation = new Reservation();
                dummyReservation.setReservationID(999);
                dummyReservation.setGuestID(1);
                dummyReservation.setRoomID(101);
                dummyReservation.setCheckInDate(LocalDate.now());
                dummyReservation.setCheckOutDate(LocalDate.now().plusDays(2));
                dummyReservation.setNumberOfGuests(2);
                dummyReservation.setStatus(ReservationStatus.CONFIRMED);

                reservations = FXCollections.observableArrayList(dummyReservation);
            }

            // Clear and add to the observable list
            allReservations.clear();
            allReservations.addAll(reservations);

            LoggingManager.logSystemInfo("Added " + allReservations.size() + " reservations to the table");

            // Update button states after refresh
            updateCancelButtonState();

        } catch (DatabaseException e) {
            LoggingManager.logException("Database error refreshing reservations", e);
            showError("Error refreshing reservations: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error refreshing reservations", e);
            showError("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Handle cancel reservation button click
     */
    @FXML
    private void handleCancelReservation() {
        Reservation selectedReservation = tblAllReservations.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showError("Please select a reservation to cancel.");
            return;
        }

        // Double-check the status to ensure it can be cancelled
        if (selectedReservation.getStatus() != ReservationStatus.PENDING &&
                selectedReservation.getStatus() != ReservationStatus.CONFIRMED) {
            showError("This reservation cannot be cancelled. Current status: " +
                    selectedReservation.getStatusDisplayName());
            return;
        }

        boolean confirmed = DialogService.showConfirmation(
                getStage(),
                "Confirm Cancellation",
                "Are you sure you want to cancel reservation #" +
                        selectedReservation.getReservationID() + "?"
        );

        if (confirmed) {
            try {
                reservationService.cancelReservation(selectedReservation.getReservationID());

                // Log the action
                logAdminActivity("Cancelled reservation #" + selectedReservation.getReservationID());

                // Show success message
                DialogService.showInformation(
                        getStage(),
                        "Success",
                        "Reservation #" + selectedReservation.getReservationID() +
                                " has been cancelled successfully."
                );

                // Refresh the table
                refreshReservations();

            } catch (Exception e) {
                LoggingManager.logException("Error cancelling reservation", e);
                showError("Error cancelling reservation: " + e.getMessage());
            }
        }
    }

    @Override
    protected Stage getStage() {
        if (btnCancelReservation != null && btnCancelReservation.getScene() != null) {
            return (Stage) btnCancelReservation.getScene().getWindow();
        }
        return null;
    }
}