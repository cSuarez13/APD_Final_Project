package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.*;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private Button btnRules;

    // Services
    private GuestService guestService;
    private ReservationService reservationService;
    private RoomService roomService;

    // Booking data
    private BookingData bookingData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        guestService = ServiceLocator.getService(GuestService.class);
        reservationService = ServiceLocator.getService(ReservationService.class);
        roomService = ServiceLocator.getService(RoomService.class);
        // Create instance of DAO

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
        if (validateFields()) {
            try {
                // 1. Create and save guest
                Guest guest = new Guest();
                guest.setName(txtName.getText().trim());
                guest.setPhoneNumber(txtPhone.getText().trim());
                guest.setEmail(txtEmail.getText().trim());
                guest.setAddress(txtAddress.getText().trim());
                int guestId = guestService.saveGuest(guest);

                // 2. Create reservation
                Reservation reservation = new Reservation();
                reservation.setGuestID(guestId);
                reservation.setCheckInDate(bookingData.getCheckInDate());
                reservation.setCheckOutDate(bookingData.getCheckOutDate());
                reservation.setNumberOfGuests(bookingData.getGuestCount());
                reservation.setStatus(ReservationStatus.CONFIRMED);

                // 3. Create a list to hold ReservationRoom objects
                List<ReservationRoom> reservationRooms = new ArrayList<>();

                // 4. Assign rooms by type using improved method
                assignRoomsByType(reservationRooms);

                // 5. Save reservation and rooms
                int reservationId = reservationService.createReservationWithRooms(reservation, reservationRooms);

                navigateToConfirmation(reservationId);

            } catch (Exception e) {
                ErrorPopupManager.showErrorPopup(getStage(), "Error: " + e.getMessage());
                LoggingManager.logSystemInfo("Error: " + e.getMessage());
            }
        }
    }

    /**
     * New method to properly assign rooms for all room types at once
     * This prevents duplicate room assignment issues
     */
    private void assignRoomsByType(List<ReservationRoom> reservationRooms) throws DatabaseException {
        // Track all room IDs to ensure no duplicates
        Set<Integer> assignedRoomIds = new HashSet<>();

        // Process each room type
        if (bookingData.getSingleRoomCount() > 0) {
            addRoomsByType(RoomType.SINGLE, bookingData.getSingleRoomCount(),
                    reservationRooms, assignedRoomIds);
        }

        if (bookingData.getDoubleRoomCount() > 0) {
            addRoomsByType(RoomType.DOUBLE, bookingData.getDoubleRoomCount(),
                    reservationRooms, assignedRoomIds);
        }

        if (bookingData.getDeluxeRoomCount() > 0) {
            addRoomsByType(RoomType.DELUXE, bookingData.getDeluxeRoomCount(),
                    reservationRooms, assignedRoomIds);
        }

        if (bookingData.getPentHouseCount() > 0) {
            addRoomsByType(RoomType.PENT_HOUSE, bookingData.getPentHouseCount(),
                    reservationRooms, assignedRoomIds);
        }
    }

    /**
     * Improved method to add rooms of a specific type,
     * ensuring no duplicate room IDs are used
     */
    private void addRoomsByType(RoomType roomType, int count,
                                List<ReservationRoom> reservationRooms,
                                Set<Integer> assignedRoomIds) throws DatabaseException {
        if (count <= 0) return;

        // Get the total number of guests for this room type
        int totalGuestsForType = bookingData.getGuestsForRoomType(roomType);
        int remainingGuests = totalGuestsForType;

        for (int i = 0; i < count; i++) {
            // Find an available room of this type that hasn't been assigned yet
            Room room = findUnassignedRoom(roomType, assignedRoomIds);

            if (room == null) {
                throw new DatabaseException("No available " + roomType.getDisplayName() + " rooms.");
            }

            // Add this room ID to our tracking set to prevent duplicates
            assignedRoomIds.add(room.getRoomID());

            // Calculate how many guests to assign to this room
            int maxCapacity = roomType.getMaxOccupancy();
            int guestsInRoom;

            if (i == count - 1) {
                // Last room gets all remaining guests
                guestsInRoom = remainingGuests;
            } else {
                // Distribute evenly among rooms
                guestsInRoom = Math.min(maxCapacity, totalGuestsForType / count);
            }

            // Ensure we don't exceed capacity
            guestsInRoom = Math.min(guestsInRoom, maxCapacity);

            // Update remaining guests
            remainingGuests -= guestsInRoom;

            // Create ReservationRoom
            ReservationRoom reservationRoom = new ReservationRoom();
            reservationRoom.setRoomID(room.getRoomID());
            reservationRoom.setPricePerNight(room.getPrice());
            reservationRoom.setGuestsInRoom(guestsInRoom);

            // Log for debugging
            LoggingManager.logSystemInfo("Adding room #" + room.getRoomID() +
                    " of type " + roomType + " with " + guestsInRoom + " guests");

            reservationRooms.add(reservationRoom);
        }
    }

    /**
     * Find an available room of the specified type that hasn't been assigned yet
     */
    private Room findUnassignedRoom(RoomType roomType, Set<Integer> assignedRoomIds)
            throws DatabaseException {
        // Get all available rooms of this type
        List<Room> availableRooms = roomService.getAllAvailableRoomsByType(
                roomType,
                bookingData.getCheckInDate(),
                bookingData.getCheckOutDate()
        );

        // Find the first room that hasn't been assigned yet
        for (Room room : availableRooms) {
            if (!assignedRoomIds.contains(room.getRoomID())) {
                return room;
            }
        }

        return null; // No unassigned rooms found
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

        // Get the controller and pass the reservation + booking data
        ConfirmationController confirmationController = loader.getController();
        confirmationController.initReservationData(reservationId, bookingData);

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