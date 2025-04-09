package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.model.*;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CheckInController extends BaseController {

    @FXML
    public TextField txtReservationId;

    @FXML
    public Button btnSearch;

    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblRoomInfo;

    @FXML
    private Label lblCheckInDate;

    @FXML
    private Label lblCheckOutDate;

    @FXML
    private Label lblGuests;

    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnCancel;

    // Services
    private ReservationService reservationService;
    private GuestService guestService;
    private ValidationService validationService;

    // State Variables
    private Reservation currentReservation;
    private Guest currentGuest;
    private List<Room> reservationRooms;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        reservationService = ServiceLocator.getService(ReservationService.class);
        guestService = ServiceLocator.getService(GuestService.class);
        ServiceLocator.getService(RoomService.class);
        validationService = ServiceLocator.getService(ValidationService.class);

        // Disable confirm button until reservation is found
        btnConfirm.setDisable(true);

        // Call parent initialize
        super.initialize(url, resourceBundle);

        LoggingManager.logSystemInfo("CheckInController initialized");
    }

    @Override
    public void initData(Admin admin) {
        super.initData(admin);
    }

    @FXML
    private void handleSearchButton() {
        performReservationSearch();
    }

    /**
     * Perform reservation search
     */
    public void performReservationSearch() {
        // Clear previous data
        clearFields();
        hideError();

        String reservationIdText = txtReservationId.getText().trim();

        try {
            // Validate reservation ID
            if (reservationIdText.isEmpty()) {
                showError("Please enter a Reservation ID");
                return;
            }

            int reservationId;
            try {
                reservationId = Integer.parseInt(reservationIdText);
            } catch (NumberFormatException e) {
                showError("Reservation ID must be a number");
                return;
            }

            // Fetch reservation information
            currentReservation = reservationService.getReservationById(reservationId);

            if (currentReservation == null) {
                showError("Reservation not found");
                return;
            }

            // Check if the reservation status allows check-in
            if (!currentReservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
                showError("This reservation cannot be checked in. Current status: " +
                        currentReservation.getStatus().getDisplayName());
                return;
            }

            // Fetch guest information
            currentGuest = guestService.getGuestById(currentReservation.getGuestID());

            // Fetch all rooms for this reservation
            reservationRooms = reservationService.getRoomsForReservation(currentReservation.getReservationID());

            LoggingManager.logSystemInfo("Displaying reservation #" + currentReservation.getReservationID());
            LoggingManager.logSystemInfo("Guest: " + (currentGuest != null ? currentGuest.getName() : "null"));
            LoggingManager.logSystemInfo("Rooms found: " + (reservationRooms != null ? reservationRooms.size() : "null"));

            // Display information
            displayReservationInfo();

            // Enable confirm button
            btnConfirm.setDisable(false);

            logAdminActivity("Found reservation #" + reservationId + " for check-in");

        } catch (Exception e) {
            LoggingManager.logException("Error searching for reservation", e);
            showError(e.getMessage());
        }
    }

    /**
     * Display the reservation information in the UI
     */
    private void displayReservationInfo() {
        // Always check the reservation first
        if (currentReservation == null) {
            LoggingManager.logSystemInfo("Cannot display reservation info: currentReservation is null");
            return;
        }

        // Display guest information
        if (currentGuest == null) {
            LoggingManager.logSystemWarning("Guest info not found for reservation #" + currentReservation.getReservationID());
            lblGuestName.setText("Guest information not available");
        } else {
            lblGuestName.setText(currentGuest.getName());
        }

        // Display room information - now handling multiple rooms
        if (reservationRooms == null || reservationRooms.isEmpty()) {
            LoggingManager.logSystemWarning("Room info not found for reservation #" + currentReservation.getReservationID());
            lblRoomInfo.setText("Room information not available");
        } else {
            // Build a room summary
            String roomSummary;
            if (reservationRooms.size() == 1) {
                Room room = reservationRooms.get(0);
                roomSummary = room.getRoomType().getDisplayName() + " (Room #" + room.getRoomID() + ")";
            } else {
                // Multiple rooms - display a summary
                roomSummary = reservationRooms.size() + " rooms: " +
                        reservationRooms.stream()
                                .map(room -> "#" + room.getRoomID())
                                .collect(Collectors.joining(", "));
            }
            lblRoomInfo.setText(roomSummary);
        }

        // Always update these fields even if guest/room is null
        lblCheckInDate.setText(currentReservation.getCheckInDate().toString());
        lblCheckOutDate.setText(currentReservation.getCheckOutDate().toString());
        lblGuests.setText(String.valueOf(currentReservation.getNumberOfGuests()));
    }

    @FXML
    private void handleConfirmButton() {
        if (currentReservation == null) {
            return;
        }

        try {
            // Validate check-in
            validationService.validateCheckIn(currentReservation);

            // Perform check-in
            reservationService.checkIn(currentReservation.getReservationID());

            // Log the check-in
            logAdminActivity("Checked in reservation #" + currentReservation.getReservationID());

            // Show success message
            DialogService.showInformation(
                    getStage(),
                    "Check-In Successful",
                    "Reservation #" + currentReservation.getReservationID() +
                            " has been successfully checked in."
            );

            // Clear the form
            clearAll();

        } catch (Exception e) {
            LoggingManager.logException("Error checking in reservation #" +
                    (currentReservation != null ? currentReservation.getReservationID() : "unknown"), e);
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancelButton() {
        // Clear all fields and close the window
        clearAll();
        closeWindow();
    }

    /**
     * Clear all fields and reset the form
     */
    private void clearAll() {
        txtReservationId.clear();
        clearFields();
        currentReservation = null;
        currentGuest = null;
        reservationRooms = null;
        btnConfirm.setDisable(true);
        hideError();
    }

    /**
     * Clear the fields that display reservation information
     */
    @Override
    protected void clearFields() {
        lblGuestName.setText("");
        lblRoomInfo.setText("");
        lblCheckInDate.setText("");
        lblCheckOutDate.setText("");
        lblGuests.setText("");
    }

    /**
     * Show an error message using ErrorPopupManager
     *
     * @param message The error message to display
     */
    @Override
    protected void showError(String message) {
        // Use ErrorPopupManager for displaying errors
        Stage stage = getStage();
        if (stage != null) {
            ErrorPopupManager.showErrorPopup(stage, message);
        } else {
            // Fallback to base class error handling if no stage is available
            super.showError(message);
        }
    }

    /**
     * Get the stage from a control in the scene
     */
    @Override
    protected Stage getStage() {
        if (btnCancel != null && btnCancel.getScene() != null) {
            return (Stage) btnCancel.getScene().getWindow();
        }
        return null;
    }

    /**
     * Set reservation ID programmatically (for integration with other screens)
     *
     * @param reservationId The reservation ID to set
     */
    public void setReservationId(int reservationId) {
        txtReservationId.setText(String.valueOf(reservationId));
        // Trigger the search action programmatically
        performReservationSearch();
    }
}