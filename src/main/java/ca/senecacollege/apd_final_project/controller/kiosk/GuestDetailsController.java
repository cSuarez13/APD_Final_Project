package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class GuestDetailsController extends BaseController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtAddress;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnRules;

    // Services
    private GuestService guestService;
    private ReservationService reservationService;

    // Booking data
    private BookingData bookingData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        guestService = ServiceLocator.getService(GuestService.class);
        reservationService = ServiceLocator.getService(ReservationService.class);

        // Apply proper text styling to ensure visibility
        applyStyles();

        // Adjust window size
        adjustStageSize();

        // Call parent initialize
        super.initialize(url, resourceBundle);

        LoggingManager.logSystemInfo("GuestDetailsController initialized");
    }

    /**
     * Initialize with booking data from previous screens
     * @param bookingData The booking data object
     */
    public void initBookingData(BookingData bookingData) {
        this.bookingData = bookingData;
        LoggingManager.logSystemInfo("Guest details screen initialized with booking data: " + bookingData);
    }

    /**
     * Apply styles to ensure text is visible
     */
    private void applyStyles() {
        String textFieldStyle = "-fx-text-fill: white; -fx-font-size: 16px;";
        txtName.setStyle(textFieldStyle);
        txtPhone.setStyle(textFieldStyle);
        txtEmail.setStyle(textFieldStyle);
        txtAddress.setStyle(textFieldStyle);

        txtName.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                txtName.getParent().getParent().lookupAll(".label").forEach(node -> {
                    if (node instanceof Label) {
                        node.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                    }
                });
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize() {
        try {
            txtName.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && newScene.getWindow() != null) {
                    Stage stage = (Stage) newScene.getWindow();

                    // Use ScreenSizeManager to set appropriate dimensions
                    double stageWidth = ScreenSizeManager.calculateStageWidth(900);
                    double stageHeight = ScreenSizeManager.calculateStageHeight(750);

                    // Get center position
                    double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

                    // Set the stage's size and position
                    stage.setWidth(stageWidth);
                    stage.setHeight(stageHeight);
                    stage.setX(centerPos[0]);
                    stage.setY(centerPos[1]);

                    // Make sure it's not maximized
                    stage.setMaximized(false);

                    LoggingManager.logSystemInfo("GuestDetailsScreen size adjusted to fit screen");
                }
            });
        } catch (Exception e) {
            LoggingManager.logException("Error adjusting stage size", e);
        }
    }

    @FXML
    private void handleNextButton() {
        // Validate inputs
        if (validateFields()) {
            try {
                // Create guest object
                Guest guest = new Guest();
                guest.setName(txtName.getText().trim());
                guest.setPhoneNumber(txtPhone.getText().trim());
                guest.setEmail(txtEmail.getText().trim());
                guest.setAddress(txtAddress.getText().trim());

                // Save guest to database and get guest ID
                int guestId = guestService.saveGuest(guest);

                // Track the IDs of created reservations
                List<Integer> reservationIds = new ArrayList<>();

                // Create reservations for each room type
                createReservationsForRoomType(guestId, RoomType.SINGLE, bookingData.getSingleRoomCount(), reservationIds);
                createReservationsForRoomType(guestId, RoomType.DOUBLE, bookingData.getDoubleRoomCount(), reservationIds);
                createReservationsForRoomType(guestId, RoomType.DELUXE, bookingData.getDeluxeRoomCount(), reservationIds);
                createReservationsForRoomType(guestId, RoomType.PENT_HOUSE, bookingData.getPentHouseCount(), reservationIds);

                // Log the successful reservations
                LoggingManager.logSystemInfo("Created " + reservationIds.size() + " reservations for guest: " + guest.getName());

                // Load the confirmation screen with the first reservation ID
                if (!reservationIds.isEmpty()) {
                    navigateToConfirmation(reservationIds.get(0));
                } else {
                    // This should not happen if validation is correct
                    ErrorPopupManager.showErrorPopup(getStage(), "Failed to create any reservations.");
                }

            } catch (Exception e) {
                LoggingManager.logException("Error processing guest details", e);
                ErrorPopupManager.showErrorPopup(getStage(), "Error processing guest details: " + e.getMessage());
            }
        }
    }

    /**
     * Create reservations for a specific room type
     *
     * @param guestId The guest ID
     * @param roomType The room type
     * @param count Number of rooms to create
     * @param reservationIds List to collect created reservation IDs
     * @throws Exception If there's an error creating reservations
     */
    private void createReservationsForRoomType(int guestId, RoomType roomType, int count, List<Integer> reservationIds) throws Exception {
        for (int i = 0; i < count; i++) {
            // Create reservation
            Reservation reservation = new Reservation();
            reservation.setGuestID(guestId);
            reservation.setCheckInDate(bookingData.getCheckInDate());
            reservation.setCheckOutDate(bookingData.getCheckOutDate());

            // Set guest count based on room type and available space
            int roomCapacity = roomType.getMaxOccupancy();
            int remainingGuests = Math.max(0, bookingData.getGuestCount() -
                    reservationIds.size() * roomCapacity);
            int guestsToAssign = Math.min(remainingGuests, roomCapacity);

            // If this is the last room and no guests would be assigned, assign at least one
            if (i == count - 1 && guestsToAssign == 0 && remainingGuests == 0) {
                guestsToAssign = 1;
            }

            reservation.setNumberOfGuests(guestsToAssign);

            // Create the reservation
            int reservationId = reservationService.createReservation(reservation, roomType);
            reservationIds.add(reservationId);
        }
    }

    /**
     * Navigate to the confirmation screen
     *
     * @param reservationId The primary reservation ID to show
     */
    private void navigateToConfirmation(int reservationId) throws IOException {
        // Load the confirmation screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_CONFIRMATION));
        Parent confirmationRoot = loader.load();

        // Get the controller and pass the reservation data
        ConfirmationController confirmationController = loader.getController();
        confirmationController.initReservationData(reservationId);

        // Get the current stage
        Stage stage = getStage();

        // Create new scene
        Scene confirmationScene = new Scene(confirmationRoot);
        confirmationScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
        confirmationScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

        // Set the new scene
        stage.setScene(confirmationScene);

        // Use ScreenSizeManager to set proper size and position
        double stageWidth = ScreenSizeManager.calculateStageWidth(900);
        double stageHeight = ScreenSizeManager.calculateStageHeight(700);
        double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

        stage.setWidth(stageWidth);
        stage.setHeight(stageHeight);
        stage.setX(centerPos[0]);
        stage.setY(centerPos[1]);

        LoggingManager.logSystemInfo("Navigated to confirmation screen with reservation ID: " + reservationId);
    }

    @FXML
    private void handleBackButton() {
        try {
            // Load the room selection screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_ROOM_SELECTION));
            Parent roomSelectionRoot = loader.load();

            // Get the controller and pass the booking data
            RoomSelectionController roomSelectionController = loader.getController();
            roomSelectionController.initBookingData(
                    bookingData.getGuestCount(),
                    bookingData.getCheckInDate(),
                    bookingData.getCheckOutDate()
            );

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene roomSelectionScene = new Scene(roomSelectionRoot);
            roomSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            roomSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the new scene
            stage.setScene(roomSelectionScene);

            // Size the stage properly
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            LoggingManager.logSystemInfo("Returned to room selection screen from guest details screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to room selection screen", e);
            ErrorPopupManager.showErrorPopup(getStage(), "Error returning to room selection screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleRulesButton() {
        RulesDialogUtility.showRulesDialog(btnRules);
    }

    @Override
    protected boolean validateFields() {
        try {
            // Validate name
            if (ValidationUtils.isNotNullOrEmpty(txtName.getText())) {
                throw new ValidationException("Please enter your name");
            }

            // Validate phone
            if (ValidationUtils.isNotNullOrEmpty(txtPhone.getText())) {
                throw new ValidationException("Please enter your phone number");
            }
            if (ValidationUtils.isValidPhoneNumber(txtPhone.getText())) {
                throw new ValidationException("Please enter a valid phone number");
            }

            // Validate email
            if (ValidationUtils.isNotNullOrEmpty(txtEmail.getText())) {
                throw new ValidationException("Please enter your email");
            }
            if (ValidationUtils.isValidEmail(txtEmail.getText())) {
                throw new ValidationException("Please enter a valid email address");
            }

            // Validate address
            if (ValidationUtils.isNotNullOrEmpty(txtAddress.getText())) {
                throw new ValidationException("Please enter your address");
            }

            return true; // All validations passed

        } catch (ValidationException e) {
            ErrorPopupManager.showErrorPopup(getStage(), e.getMessage());
            return false; // Validation failed
        }
    }

    @Override
    protected Stage getStage() {
        if (btnNext != null && btnNext.getScene() != null) {
            return (Stage) btnNext.getScene().getWindow();
        }
        return null;
    }

    @Override
    protected void clearFields() {
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
    }

    // Override methods that reference lblError to do nothing,
    // since we no longer have the error label
    @Override
    protected void showError(String message) {
        // Use ErrorPopupManager instead of label
        ErrorPopupManager.showErrorPopup(getStage(), message);
    }

    @Override
    protected void hideError() {
        // Nothing to do, we don't have a label to hide
    }
}