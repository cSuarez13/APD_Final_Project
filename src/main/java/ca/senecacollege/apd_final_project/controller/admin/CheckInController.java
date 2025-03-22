package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CheckInController implements Initializable {

    @FXML
    private Label lblGuestName;

    @FXML
    private TextField txtReservationId;

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
    private Reservation currentReservation;

    private Admin currentAdmin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reservationService = new ReservationService();
        guestService = new GuestService();

        lblError.setVisible(false);
    }

    /**
     * Initialize controller with admin data
     */
    public void initData(Admin admin) {
        this.currentAdmin = admin;
        LoggingManager.logSystemInfo("CheckInController initialized with admin: " + admin.getUsername());
    }

    /**
     * Initialize with reservation data
     *
     * @param reservationId The reservation ID
     */
    public void initData(int reservationId) {
        try {
            currentReservation = reservationService.getReservationById(reservationId);

            if (currentReservation == null) {
                showError("Reservation not found");
                return;
            }

            Guest guest = guestService.getGuestById(currentReservation.getGuestID());

            lblGuestName.setText(guest.getName());
            lblReservationDetails.setText(
                    "Reservation #" + reservationId + "\n" +
                            "Check-in: " + currentReservation.getCheckInDate() + "\n" +
                            "Check-out: " + currentReservation.getCheckOutDate() + "\n" +
                            "Room: " + currentReservation.getRoomID()
            );

        } catch (DatabaseException e) {
            showError("Error loading reservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfirmButton(ActionEvent event) {
        try {
            reservationService.checkIn(currentReservation.getReservationID());

            // Close the window
            Stage stage = (Stage) btnConfirm.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showError("Error checking in: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

    @FXML
    private void handleSearchButton(ActionEvent event) {
        try {
            // Get the reservation ID from the text field
            int reservationId = Integer.parseInt(txtReservationId.getText().trim());

            // Fetch the reservation details using the reservation ID
            currentReservation = reservationService.getReservationById(reservationId);

            if (currentReservation == null) {
                showError("Reservation not found");
                return;
            }

            // Fetch the guest details using the guest ID from the reservation
            Guest guest = guestService.getGuestById(currentReservation.getGuestID());

            if (guest == null) {
                showError("Guest not found");
                return;
            }

            // Update the UI with the guest and reservation details
            lblGuestName.setText(guest.getName());
            lblReservationDetails.setText(
                    "Reservation #" + reservationId + "\n" +
                            "Check-in: " + currentReservation.getCheckInDate() + "\n" +
                            "Check-out: " + currentReservation.getCheckOutDate() + "\n" +
                            "Room: " + currentReservation.getRoomID()
            );

            // Hide any previous error messages
            lblError.setVisible(false);

        } catch (NumberFormatException e) {
            showError("Invalid reservation ID. Please enter a valid number.");
        } catch (DatabaseException e) {
            showError("Error retrieving reservation: " + e.getMessage());
        }
    }
}