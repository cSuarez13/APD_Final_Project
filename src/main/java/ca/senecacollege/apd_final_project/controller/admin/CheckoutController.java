package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.model.*;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CheckoutController extends BaseController {

    // FXML Fields - Updated to use contact info instead of reservation ID
    @FXML
    private TextField txtContactInfo;

    @FXML
    private ComboBox<String> cmbSearchType;

    @FXML
    private Button btnSearch;

    @FXML
    private ComboBox<Reservation> cmbReservations;

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
    private Button btnCheckout;

    @FXML
    private Button btnCancel;

    @FXML
    private CheckBox chkFeedbackReminder;

    @FXML
    private VBox roomDetailsContainer; // Optional container for showing multiple rooms

    // Services
    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;
    private BillingService billingService;
    private ValidationService validationService;

    // State Variables
    private Reservation currentReservation;
    private Guest currentGuest;
    private List<Room> currentRooms;
    private Billing currentBill;

    // List to hold matching reservations
    private final ObservableList<Reservation> matchingReservations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services using ServiceLocator
        reservationService = ServiceLocator.getService(ReservationService.class);
        guestService = ServiceLocator.getService(GuestService.class);
        roomService = ServiceLocator.getService(RoomService.class);
        billingService = ServiceLocator.getService(BillingService.class);
        validationService = ServiceLocator.getService(ValidationService.class);

        // Set feedback reminder checkbox to checked by default
        chkFeedbackReminder.setSelected(true);

        // Initialize search type combo box
        cmbSearchType.setItems(FXCollections.observableArrayList("Email", "Phone Number"));
        cmbSearchType.getSelectionModel().selectFirst();

        // Setup reservations combo box
        setupReservationsComboBox();

        // Hide error message initially
        hideError();

        // Disable checkout button until reservation is selected
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

        // Add listener to reservation selection to update details
        cmbReservations.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadReservationDetails(newValue);
                    } else {
                        clearFields();
                        btnCheckout.setDisable(true);
                    }
                });

        // Call parent initialize
        super.initialize(url, resourceBundle);
    }

    /**
     * Setup the reservations combo box with custom cell factory
     */
    private void setupReservationsComboBox() {
        cmbReservations.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Reservation reservation) {
                if (reservation == null) return null;
                return "Reservation #" + reservation.getReservationID() +
                        " - Check-in: " + reservation.getCheckInDate();
            }

            @Override
            public Reservation fromString(String string) {
                // Not needed for our use case
                return null;
            }
        });

        cmbReservations.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Reservation reservation, boolean empty) {
                super.updateItem(reservation, empty);

                if (empty || reservation == null) {
                    setText(null);
                } else {
                    setText("Reservation #" + reservation.getReservationID() +
                            " - Check-in: " + reservation.getCheckInDate());
                }
            }
        });

        cmbReservations.setItems(matchingReservations);
    }

    @FXML
    private void handleSearchButton() {
        // Clear previous data
        clearFields();
        hideError();
        matchingReservations.clear();

        String contactInfo = txtContactInfo.getText().trim();
        String searchType = cmbSearchType.getValue();

        if (contactInfo.isEmpty()) {
            showError("Please enter " + searchType.toLowerCase());
            return;
        }

        try {
            // Find the guest based on contact info
            List<Guest> guests;
            if ("Email".equals(searchType)) {
                guests = guestService.searchGuestsByEmail(contactInfo);
            } else {
                guests = guestService.searchGuestsByPhone(contactInfo);
            }

            if (guests == null || guests.isEmpty()) {
                showError("No guest found with the provided " + searchType.toLowerCase());
                return;
            }

            // For each guest, get their active reservations
            for (Guest guest : guests) {
                List<Reservation> reservations = reservationService.getReservationsByGuest(guest.getGuestID());

                // Filter only checked-in reservations
                for (Reservation reservation : reservations) {
                    if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                        matchingReservations.add(reservation);
                    }
                }
            }

            if (matchingReservations.isEmpty()) {
                showError("No active check-ins found for the provided " + searchType.toLowerCase());
            } else {
                // Automatically select first reservation if there's only one match
                if (matchingReservations.size() == 1) {
                    cmbReservations.getSelectionModel().selectFirst();
                }

                logAdminActivity("Found " + matchingReservations.size() + " active reservations for checkout");
            }

        } catch (Exception e) {
            showError(e.getMessage());
            logAdminActivity("Error searching by " + searchType + ": " + e.getMessage());
        }
    }

    /**
     * Loads the details of the selected reservation
     *
     * @param reservation The reservation to load details for
     */
    private void loadReservationDetails(Reservation reservation) {
        try {
            currentReservation = reservation;

            // Fetch guest information
            currentGuest = guestService.getGuestById(reservation.getGuestID());

            // Fetch all rooms for this reservation
            currentRooms = reservationService.getRoomsForReservation(reservation.getReservationID());

            // Display information
            displayReservationInfo();

            // Calculate the bill
            calculateBill();

            // Enable checkout button
            btnCheckout.setDisable(false);

            logAdminActivity("Selected reservation #" + reservation.getReservationID() + " for checkout");

        } catch (Exception e) {
            showError("Error loading reservation details: " + e.getMessage());
            logAdminActivity("Error loading reservation details: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckoutButton() {
        if (currentReservation == null) {
            return;
        }

        try {
            // Validate checkout
            validationService.validateCheckOut(currentReservation);

            // Create billing record
            currentBill = new Billing();
            currentBill.setReservationID(currentReservation.getReservationID());

            // Set bill amount based on room price and nights
            double subtotal = calculateSubtotal();
            currentBill.setAmount(subtotal);

            // Calculate tax (13%)
            double tax = subtotal * Constants.TAX_RATE;
            currentBill.setTax(tax);

            // Apply discount if provided
            double discount = 0.0;
            if (!txtDiscount.getText().isEmpty()) {
                discount = Double.parseDouble(txtDiscount.getText());
                validationService.validateBilling(subtotal, discount);
                currentBill.setDiscount(discount);
            }

            // Calculate total amount
            double total = subtotal + tax - discount;
            currentBill.setTotalAmount(total);

            // Set billing datetime and paid status
            currentBill.setBillingDateTime(LocalDateTime.now());
            currentBill.setPaid(true);
            currentBill.setPaymentMethod("Credit Card"); // Default payment method

            // Save the bill
            int billId = billingService.saveBill(currentBill);

            // Update reservation status to checked out
            reservationService.checkOut(currentReservation.getReservationID());

            // Log the checkout
            logAdminActivity("Checked out reservation #" + currentReservation.getReservationID());

            // Show feedback reminder if the checkbox is selected
            if (chkFeedbackReminder.isSelected()) {
                showFeedbackReminder(billId);
            }

            // Show success message
            showAlert(Alert.AlertType.INFORMATION,
                    "Checkout Successful",
                    "Reservation #" + currentReservation.getReservationID() +
                            " has been successfully checked out.\nBill #" + billId +
                            " has been generated.");

            // Clear the form
            clearAll();

        } catch (Exception e) {
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
     * Display the reservation information in the UI
     */
    private void displayReservationInfo() {
        if (currentReservation == null || currentGuest == null || currentRooms == null || currentRooms.isEmpty()) {
            return;
        }

        lblGuestName.setText(currentGuest.getName());

        // Handle multiple rooms - create a summary
        if (currentRooms.size() == 1) {
            Room room = currentRooms.get(0);
            lblRoomInfo.setText(room.getRoomType().getDisplayName() + " (Room #" + room.getRoomID() + ")");
        } else {
            // Multiple rooms - show count and details if container exists
            String roomSummary = currentRooms.size() + " Rooms: " +
                    currentRooms.stream()
                            .map(room -> "#" + room.getRoomID())
                            .collect(Collectors.joining(", "));
            lblRoomInfo.setText(roomSummary);

            // If we have a container for detailed room info, display it
            if (roomDetailsContainer != null) {
                roomDetailsContainer.getChildren().clear();

                // Add a label for each room with details
                for (Room room : currentRooms) {
                    Label roomLabel = new Label(room.getRoomType().getDisplayName() +
                            " (Room #" + room.getRoomID() + ") - $" +
                            String.format("%.2f", room.getPrice()) + " per night");
                    roomLabel.setStyle("-fx-text-fill: white;");
                    roomDetailsContainer.getChildren().add(roomLabel);
                }
            }
        }

        lblCheckInDate.setText(currentReservation.getCheckInDate().toString());
        lblCheckOutDate.setText(currentReservation.getCheckOutDate().toString());

        int nights = currentReservation.calculateNumberOfNights();
        lblNights.setText(String.valueOf(nights));
    }

    /**
     * Calculate the bill and update the UI
     */
    private void calculateBill() {
        if (currentReservation == null || currentRooms == null || currentRooms.isEmpty()) {
            return;
        }

        // Calculate subtotal (sum of all room prices * nights)
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
     * Calculate the subtotal (sum of all room prices * nights)
     *
     * @return The subtotal
     */
    private double calculateSubtotal() {
        if (currentRooms == null || currentRooms.isEmpty()) {
            return 0.0;
        }

        int nights = currentReservation.calculateNumberOfNights();

        // Sum up the price for all rooms
        return currentRooms.stream()
                .mapToDouble(room -> room.getPrice() * nights)
                .sum();
    }

    /**
     * Show a reminder to the admin about guest feedback
     * With proper sizing to fit content
     */
    private void showFeedbackReminder(int billId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feedback Reminder");
        alert.setHeaderText("Feedback Reminder");

        // Create custom content with wrapping
        Label contentLabel = new Label(
                "Please inform the guest that they can use the kiosk " +
                        "to provide feedback about their stay.\n\n" +
                        "Their Bill ID is: " + billId + "\n\n" +
                        "Please remind them to use this Bill ID when submitting feedback.");

        // Set label properties for better display
        contentLabel.setWrapText(true);
        contentLabel.setPrefWidth(400);  // Set preferred width
        contentLabel.setMinHeight(150);  // Set minimum height
        contentLabel.setStyle("-fx-font-size: 14px;");

        // Set the content
        alert.getDialogPane().setContent(contentLabel);

        // Apply CSS to match the application theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

        // Adjust size based on content
        dialogPane.setPrefWidth(450);
        dialogPane.setPrefHeight(200);

        // Show the dialog
        alert.showAndWait();
    }

    /**
     * Clear all fields and reset the form
     */
    private void clearAll() {
        txtContactInfo.clear();
        matchingReservations.clear();
        clearFields();
        currentReservation = null;
        currentGuest = null;
        currentRooms = null;
        currentBill = null;
        btnCheckout.setDisable(true);
        hideError();

        // Clear room details container if it exists
        if (roomDetailsContainer != null) {
            roomDetailsContainer.getChildren().clear();
        }
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
        lblNights.setText("");
        lblSubtotal.setText("$0.00");
        lblTax.setText("$0.00");
        txtDiscount.clear();
        lblTotal.setText("$0.00");
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

    @Override
    protected void showError(String message) {
        // Use ErrorPopupManager for displaying errors
        if (getStage() != null) {
            ErrorPopupManager.showErrorPopup(getStage(), message);
        } else {
            // Fallback to base class error handling if no stage is available
            super.showError(message);
        }
    }

    public void setReservationId(int reservationId) {
        // This method is kept for backward compatibility
        // It will load the reservation directly by ID
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation != null) {
                matchingReservations.clear();
                matchingReservations.add(reservation);
                cmbReservations.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showError("Error loading reservation: " + e.getMessage());
        }
    }
}