package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class GuestDetailsController implements Initializable {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtAddress;

    @FXML
    private Label lblError;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnRules;

    // Booking data from previous screen
    private int numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private RoomType roomType;

    // Services
    private GuestService guestService;
    private ReservationService reservationService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Hide error label initially
        lblError.setVisible(false);

        LoggingManager.logSystemInfo("GuestDetailsController initialized");
    }

    public void initBookingData(int numberOfGuests, LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
        this.numberOfGuests = numberOfGuests;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomType = roomType;

        LoggingManager.logSystemInfo("Guest details screen initialized with booking data");
    }

    @FXML
    private void handleNextButton(ActionEvent event) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Create guest object
            Guest guest = new Guest();
            guest.setName(txtName.getText().trim());
            guest.setPhoneNumber(txtPhone.getText().trim());
            guest.setEmail(txtEmail.getText().trim());
            guest.setAddress(txtAddress.getText().trim());

            // Save guest to database and get guest ID
            int guestId = guestService.saveGuest(guest);

            // Create reservation
            Reservation reservation = new Reservation();
            reservation.setGuestID(guestId);
            reservation.setCheckInDate(checkInDate);
            reservation.setCheckOutDate(checkOutDate);
            reservation.setNumberOfGuests(numberOfGuests);
            reservation.setStatus(Reservation.STATUS_PENDING);

            // Save reservation and get reservation ID
            int reservationId = reservationService.createReservation(reservation, roomType);

            // Load the confirmation screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_CONFIRMATION));
            Parent confirmationRoot = loader.load();

            // Get the controller and pass the reservation data
            ConfirmationController confirmationController = loader.getController();
            confirmationController.initReservationData(reservationId);

            // Get the current stage
            Stage stage = (Stage) btnNext.getScene().getWindow();

            // Create new scene
            Scene confirmationScene = new Scene(confirmationRoot);
            confirmationScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the new scene
            stage.setScene(confirmationScene);

            LoggingManager.logSystemInfo("Navigated to confirmation screen with reservation ID: " + reservationId);

        } catch (Exception e) {
            LoggingManager.logException("Error processing guest details", e);
            showError("System error. Please try again or contact front desk.");
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            // Load the booking screen again
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_BOOKING));
            Parent bookingRoot = loader.load();

            // Get the controller
            BookingScreenController bookingController = loader.getController();

            // Get the current stage
            Stage stage = (Stage) btnBack.getScene().getWindow();

            // Create new scene
            Scene bookingScene = new Scene(bookingRoot);
            bookingScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the new scene
            stage.setScene(bookingScene);

            LoggingManager.logSystemInfo("Navigated back to booking screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to booking screen", e);
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        // Show rules and regulations dialog
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Rules & Regulations");
        alert.setHeaderText("Please Read Our Rules & Regulations");
        alert.setContentText(Constants.RULES_REGULATIONS);

        // Apply CSS to the dialog
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }

    private boolean validateInputs() {
        // Validate name
        if (!ValidationUtils.isNotNullOrEmpty(txtName.getText())) {
            showError("Please enter your name.");
            return false;
        }

        // Validate phone
        if (!ValidationUtils.isNotNullOrEmpty(txtPhone.getText())) {
            showError("Please enter your phone number.");
            return false;
        }

        if (!ValidationUtils.isValidPhoneNumber(txtPhone.getText())) {
            showError("Please enter a valid phone number.");
            return false;
        }

        // Validate email
        if (!ValidationUtils.isNotNullOrEmpty(txtEmail.getText())) {
            showError("Please enter your email.");
            return false;
        }

        if (!ValidationUtils.isValidEmail(txtEmail.getText())) {
            showError("Please enter a valid email address.");
            return false;
        }

        // Validate address
        if (!ValidationUtils.isNotNullOrEmpty(txtAddress.getText())) {
            showError("Please enter your address.");
            return false;
        }

        // All validations passed
        return true;
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}