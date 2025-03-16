package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ReservationException;
import ca.senecacollege.apd_final_project.model.Billing;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.service.BillingService;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class CheckoutController implements Initializable {

    @FXML
    private TextField txtReservationId;

    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblRoomInfo;

    @FXML
    private Label lblCheckInDate;

    @FXML
    private Label lblCheckOutDate;

    @FXML
    private Label lblNights;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblTax;

    @FXML
    private TextField txtDiscount;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnCheckout;

    @FXML
    private Button btnCancel;

    @FXML
    private CheckBox chkFeedbackReminder;

    @FXML
    private Label lblError;

    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;
    private BillingService billingService;

    private Reservation currentReservation;
    private Guest currentGuest;
    private Room currentRoom;
    private Billing currentBill;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        reservationService = new ReservationService();
        guestService = new GuestService();
        roomService = new RoomService();
        billingService = new BillingService();

        // Set feedback reminder checkbox to checked by default
        chkFeedbackReminder.setSelected(true);

        // Hide error message initially
        lblError.setVisible(false);

        // Disable checkout button until reservation is found
        btnCheckout.setDisable(true);

        // Add listener to discount field to recalculate total when changed
        txtDiscount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (currentReservation != null) {
                if (!newValue.matches("\\d*(\\.\\d*)?")) {
                    txtDiscount.setText(oldValue);
                } else {
                    calculateBill();
                }
            }
        });

        LoggingManager.logSystemInfo("CheckoutController initialized");
    }

    @FXML
    private void handleSearchButton(ActionEvent event) {
        // Clear previous data
        clearFields();

        String reservationIdText = txtReservationId.getText().trim();

        // Validate input
        if (reservationIdText.isEmpty()) {
            showError("Please enter a reservation ID");
            return;
        }

        if (!ValidationUtils.isPositiveInteger(reservationIdText)) {
            showError("Please enter a valid reservation ID");
            return;
        }

        int reservationId = Integer.parseInt(reservationIdText);

        try {
            // Fetch reservation information
            currentReservation = reservationService.getReservationById(reservationId);

            if (currentReservation == null) {
                showError("Reservation not found");
                return;
            }

            // Check if the reservation status is "Checked In"
            if (!currentReservation.getStatus().equals(Reservation.STATUS_CHECKED_IN)) {
                showError("This reservation cannot be checked out. Current status: " +
                        currentReservation.getStatus());
                return;
            }

            // Fetch guest and room information
            currentGuest = guestService.getGuestById(currentReservation.getGuestID());
            currentRoom = roomService.getRoomById(currentReservation.getRoomID());

            // Display information
            displayReservationInfo();

            // Calculate the bill
            calculateBill();

            // Enable checkout button
            btnCheckout.setDisable(false);

            LoggingManager.logSystemInfo("Found reservation #" + reservationId + " for checkout");

        } catch (DatabaseException e) {
            LoggingManager.logException("Error fetching reservation for checkout", e);
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error during checkout search", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckoutButton(ActionEvent event) {
        if (currentReservation == null) {
            return;
        }

        try {
            // Create billing record
            currentBill = new Billing();
            currentBill.setReservationID(currentReservation.getReservationID());

            // Set bill amount based on room price and nights
            double subtotal = calculateSubtotal();
            currentBill.setAmount(subtotal);

            // Apply discount if provided
            double discount = 0.0;
            if (!txtDiscount.getText().isEmpty()) {
                try {
                    discount = Double.parseDouble(txtDiscount.getText());
                    if (discount > subtotal) {
                        showError("Discount cannot be greater than the subtotal");
                        return;
                    }
                    currentBill.setDiscount(discount);
                } catch (NumberFormatException e) {
                    showError("Invalid discount amount");
                    return;
                }
            }

            // Set billing datetime and paid status
            currentBill.setBillingDateTime(LocalDateTime.now());
            currentBill.setPaid(true);

            // Save the bill
            int billId = billingService.saveBill(currentBill);

            // Update reservation status to checked out
            reservationService.checkOut(currentReservation.getReservationID());

            // Log the checkout
            String adminUser = "Admin"; // In a real app, this would be the logged-in admin
            LoggingManager.logAdminActivity(adminUser, "Checked out reservation #" +
                    currentReservation.getReservationID());

            // Show feedback reminder if the checkbox is selected
            if (chkFeedbackReminder.isSelected()) {
                showFeedbackReminder();
            }

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Checkout Successful");
            alert.setHeaderText("Guest Checked Out Successfully");
            alert.setContentText("Reservation #" + currentReservation.getReservationID() +
                    " has been successfully checked out.\nBill #" + billId +
                    " has been generated.");

            // Apply CSS to the dialog
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            alert.showAndWait();

            // Clear the form
            clearAll();

        } catch (ReservationException e) {
            LoggingManager.logException("Error checking out reservation", e);
            showError("Checkout error: " + e.getMessage());
        } catch (DatabaseException e) {
            LoggingManager.logException("Database error during checkout", e);
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error during checkout", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        // Clear all fields and close the window
        clearAll();

        // Get the stage and close it
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Display the reservation information in the UI
     */
    private void displayReservationInfo() {
        if (currentReservation == null || currentGuest == null || currentRoom == null) {
            return;
        }

        lblGuestName.setText(currentGuest.getName());
        lblRoomInfo.setText(currentRoom.getRoomType().getDisplayName() + " (Room #" +
                currentRoom.getRoomID() + ")");
        lblCheckInDate.setText(currentReservation.getCheckInDate().toString());
        lblCheckOutDate.setText(currentReservation.getCheckOutDate().toString());

        int nights = currentReservation.calculateNumberOfNights();
        lblNights.setText(String.valueOf(nights));
    }

    /**
     * Calculate the bill and update the UI
     */
    private void calculateBill() {
        if (currentReservation == null || currentRoom == null) {
            return;
        }

        // Calculate subtotal (room price * nights)
        double subtotal = calculateSubtotal();
        lblSubtotal.setText(String.format("$%.2f", subtotal));

        // Calculate tax
        double tax = subtotal * Constants.TAX_RATE;
        lblTax.setText(String.format("$%.2f", tax));

        // Apply discount if provided
        double discount = 0.0;
        if (!txtDiscount.getText().isEmpty()) {
            try {
                discount = Double.parseDouble(txtDiscount.getText());
            } catch (NumberFormatException e) {
                // Invalid discount, ignore
            }
        }

        // Calculate total
        double total = subtotal + tax - discount;
        lblTotal.setText(String.format("$%.2f", total));
    }

    /**
     * Calculate the subtotal (room price * nights)
     *
     * @return The subtotal
     */
    private double calculateSubtotal() {
        int nights = currentReservation.calculateNumberOfNights();
        return currentRoom.getPrice() * nights;
    }

    /**
     * Show a reminder to the admin about guest feedback
     */
    private void showFeedbackReminder() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feedback Reminder");
        alert.setHeaderText("Remind Guest About Feedback");
        alert.setContentText("Please remind the guest that they can use the kiosk " +
                "to provide feedback about their stay.");

        // Apply CSS to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

        alert.showAndWait();
    }

    /**
     * Clear all fields and reset the form
     */
    private void clearAll() {
        txtReservationId.clear();
        clearFields();
        currentReservation = null;
        currentGuest = null;
        currentRoom = null;
        currentBill = null;
        btnCheckout.setDisable(true);
        lblError.setVisible(false);
    }

    /**
     * Clear the fields that display reservation information
     */
    private void clearFields() {
        lblGuestName.setText("");
        lblRoomInfo.setText("");
        lblCheckInDate.setText("");
        lblCheckOutDate.setText("");
        lblNights.setText("");
        lblSubtotal.setText("$0.00");
        lblTax.setText("$0.00");
        txtDiscount.clear();
        lblTotal.setText("$0.00");
    }

    /**
     * Show an error message in the UI
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}