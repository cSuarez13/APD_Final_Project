package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ConfirmationController implements Initializable {

    @FXML
    private Label lblReservationId;

    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblRoomInfo;

    @FXML
    private Label lblCheckIn;

    @FXML
    private Label lblCheckOut;

    @FXML
    private Label lblNights;

    @FXML
    private Label lblGuests;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblTax;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnDone;

    private int reservationId;
    private Reservation reservation;
    private Guest guest;
    private Room room;

    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reservationService = new ReservationService();
        guestService = new GuestService();
        roomService = new RoomService();

        LoggingManager.logSystemInfo("ConfirmationController initialized");
    }

    public void initReservationData(int reservationId) {
        this.reservationId = reservationId;

        try {
            // Load reservation details
            reservation = reservationService.getReservationById(reservationId);

            // Load guest details
            guest = guestService.getGuestById(reservation.getGuestID());

            // Load room details
            room = roomService.getRoomById(reservation.getRoomID());

            // Update UI with reservation details
            updateConfirmationDetails();

            LoggingManager.logSystemInfo("Confirmation screen loaded with reservation ID: " + reservationId);

        } catch (Exception e) {
            LoggingManager.logException("Error loading reservation details", e);
        }
    }

    private void updateConfirmationDetails() {
        // Format the reservation details for display
        lblReservationId.setText("Reservation #" + reservationId);
        lblGuestName.setText(guest.getName());
        lblRoomInfo.setText(room.getRoomType().getDisplayName() + " (Room #" + room.getRoomID() + ")");
        lblCheckIn.setText(reservation.getCheckInDate().format(dateFormatter));
        lblCheckOut.setText(reservation.getCheckOutDate().format(dateFormatter));

        // Calculate number of nights
        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        lblNights.setText(String.valueOf(nights));

        // Show number of guests
        lblGuests.setText(String.valueOf(reservation.getNumberOfGuests()));

        // Calculate and show pricing
        double subtotal = room.getPrice() * nights;
        double tax = subtotal * Constants.TAX_RATE;
        double total = subtotal + tax;

        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblTax.setText(String.format("$%.2f", tax));
        lblTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handlePrintButton(ActionEvent event) {
        // In a real system, this would send the confirmation to a printer
        // For now, just log the action
        LoggingManager.logSystemInfo("Print confirmation requested for reservation ID: " + reservationId);

        // Show a dialog confirming the print request
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Print Confirmation");
        alert.setHeaderText("Printing Confirmation");
        alert.setContentText("Your booking confirmation has been sent to the printer.\n" +
                "Please collect it from the front desk.");

        // Apply CSS to the dialog
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }

    @FXML
    private void handleDoneButton(ActionEvent event) {
        try {
            // Load the welcome screen to restart the process
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent welcomeRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) btnDone.getScene().getWindow();

            // Create new scene
            Scene welcomeScene = new Scene(welcomeRoot);
            welcomeScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the new scene
            stage.setScene(welcomeScene);

            LoggingManager.logSystemInfo("Returned to welcome screen after completing reservation");

        } catch (IOException e) {
            LoggingManager.logException("Error returning to welcome screen", e);
        }
    }
}