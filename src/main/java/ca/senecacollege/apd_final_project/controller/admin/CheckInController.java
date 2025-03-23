package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.ValidationService;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CheckInController extends BaseController {

    @FXML
    public TextField txtReservationId; // Changed to public

    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblReservationDetails;

    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnCancel;

    @FXML
    private Label lblError;

    private ReservationService reservationService;
    private GuestService guestService;
    private ValidationService validationService;
    private Reservation currentReservation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Initialize services
        reservationService = new ReservationService();
        guestService = new GuestService();
        validationService = new ValidationService();

        // Hide error label initially
        hideError();
    }

    @Override
    public void initData(Admin admin) {
        super.initData(admin);
    }

    // Changed to public and removed ActionEvent parameter
    public void searchReservation() {
        // Clear previous error and reset fields
        hideError();
        clearFields();

        try {
            // Validate reservation ID input
            String reservationIdText = txtReservationId.getText().trim();
            validationService.validateId(reservationIdText, "Reservation ID");

            // Parse reservation ID
            int reservationId = Integer.parseInt(reservationIdText);

            // Fetch reservation details
            currentReservation = reservationService.getReservationById(reservationId);

            if (currentReservation == null) {
                showError("Reservation not found");
                return;
            }

            // Validate check-in conditions
            if (!currentReservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
                showError("Cannot check in. Current status: " +
                        currentReservation.getStatus().getDisplayName());
                return;
            }

            // Fetch guest details
            Guest guest = guestService.getGuestById(currentReservation.getGuestID());

            if (guest == null) {
                showError("Guest not found");
                return;
            }

            // Display reservation details
            displayReservationDetails(guest);

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleSearchButton(ActionEvent event) {
        searchReservation();
    }

    @FXML
    private void handleConfirmButton(ActionEvent event) {
        if (currentReservation == null) {
            showError("No reservation selected");
            return;
        }

        try {
            // Validate check-in conditions
            validationService.validateCheckIn(currentReservation);

            // Perform check-in
            reservationService.checkIn(currentReservation.getReservationID());

            // Log the activity
            logAdminActivity("Checked in reservation #" + currentReservation.getReservationID());

            // Close the window
            closeWindow();

        } catch (Exception e) {
            showError("Error checking in: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        // Close the window
        closeWindow();
    }

    /**
     * Display reservation details for the selected reservation
     * @param guest The guest associated with the reservation
     */
    private void displayReservationDetails(Guest guest) {
        lblGuestName.setText(guest.getName());
        lblReservationDetails.setText(
                "Reservation #" + currentReservation.getReservationID() + "\n" +
                        "Check-in: " + currentReservation.getCheckInDate() + "\n" +
                        "Check-out: " + currentReservation.getCheckOutDate() + "\n" +
                        "Room: " + currentReservation.getRoomID()
        );
    }

    /**
     * Clear all input fields and reservation details
     */
    @Override
    protected void clearFields() {
        lblGuestName.setText("");
        lblReservationDetails.setText("");
        currentReservation = null;
    }

    /**
     * Get the current stage
     */
    @Override
    protected Stage getStage() {
        return btnConfirm != null && btnConfirm.getScene() != null
                ? (Stage) btnConfirm.getScene().getWindow()
                : null;
    }

    /**
     * Show an error message
     * @param message The error message to display
     */
    @Override
    protected void showError(String message) {
        super.showError(message);
    }
}