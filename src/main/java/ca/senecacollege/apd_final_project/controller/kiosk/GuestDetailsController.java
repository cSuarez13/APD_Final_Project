package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ReservationException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.*;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
                String email = txtEmail.getText().trim();
                String name = txtName.getText().trim();
                String phone = txtPhone.getText().trim();
                String address = txtAddress.getText().trim();

                // Check if email already exists in the database
                Guest existingGuest = guestService.findGuestByEmail(email);

                if (existingGuest != null) {
                    // Email exists in the database
                    if (existingGuest.getName().equals(name) &&
                            existingGuest.getPhoneNumber().equals(phone) &&
                            existingGuest.getAddress().equals(address)) {
                        // All details match - proceed with the same guest ID
                        LoggingManager.logSystemInfo("Using existing guest with matching details, ID: " + existingGuest.getGuestID());
                        proceedWithReservation(existingGuest.getGuestID());
                    } else {
                        // Email matches but other details don't match
                        // Show dialog to confirm updating the existing record
                        showUpdateConfirmationDialog(existingGuest);
                    }
                } else {
                    // Email doesn't exist - create new guest
                    createNewGuestAndProceed();
                }
            } catch (DatabaseException e) {
                ErrorPopupManager.showErrorPopup(getStage(), "Database Error: " + e.getMessage());
                LoggingManager.logException("Database error processing guest details", e);
            } catch (IOException e) {
                ErrorPopupManager.showErrorPopup(getStage(), "Navigation Error: " + e.getMessage());
                LoggingManager.logException("Error navigating to confirmation screen", e);
            } catch (ReservationException e) {
                ErrorPopupManager.showErrorPopup(getStage(), "Reservation Error: " + e.getMessage());
                LoggingManager.logException("Error creating reservation", e);
            } catch (Exception e) {
                ErrorPopupManager.showErrorPopup(getStage(), "Unexpected Error: " + e.getMessage());
                LoggingManager.logException("Unexpected error processing guest details", e);
            }
        }
    }

    /**
     * Shows a confirmation dialog when email exists but other details don't match
     * Displays the differences and allows the user to update or cancel
     * @param existingGuest The existing guest with matching email
     */
    private void showUpdateConfirmationDialog(Guest existingGuest) {
        try {
            // Create the custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Email Already Registered");
            dialog.setHeaderText("The email is already registered with different information");

            // Get the dialog pane and apply styling
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");

            // Create content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20, 10, 10, 10));

            // Create an explanation label that clearly explains what's happening
            Label explanationLabel = new Label("We found this email in our system, but some details don't match. "
                    + "Would you like to update the existing guest information?");
            explanationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            explanationLabel.setWrapText(true);

            // Create a grid for comparing current and new values
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(10);
            grid.setPadding(new Insets(10));

            // Headers
            Label fieldHeader = new Label("Field");
            Label currentHeader = new Label("Current Information");
            Label newHeader = new Label("New Information");

            fieldHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            currentHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            newHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

            grid.add(fieldHeader, 0, 0);
            grid.add(currentHeader, 1, 0);
            grid.add(newHeader, 2, 0);

            // Track which fields are different
            StringBuilder mismatchFields = new StringBuilder();
            int row = 1;

            // Name row
            String currentName = existingGuest.getName();
            String newName = txtName.getText().trim();
            boolean nameChanged = !currentName.equals(newName);

            if (nameChanged) {
                addFieldRow(grid, "Name", currentName, newName, row++, true);
                mismatchFields.append("name");
            }

            // Phone row
            String currentPhone = existingGuest.getPhoneNumber();
            String newPhone = txtPhone.getText().trim();
            boolean phoneChanged = !currentPhone.equals(newPhone);

            if (phoneChanged) {
                addFieldRow(grid, "Phone", currentPhone, newPhone, row++, true);
                if (!mismatchFields.isEmpty()) mismatchFields.append(", ");
                mismatchFields.append("phone");
            }

            // Address row
            String currentAddress = existingGuest.getAddress();
            String newAddress = txtAddress.getText().trim();
            boolean addressChanged = !currentAddress.equals(newAddress);

            if (addressChanged) {
                addFieldRow(grid, "Address", currentAddress, newAddress, row++, true);
                if (!mismatchFields.isEmpty()) mismatchFields.append(", ");
                mismatchFields.append("address");
            }

            // Add explanation about the changes
            Label changesLabel = new Label("The following field(s) have different values: " + mismatchFields);
            changesLabel.setStyle("-fx-text-fill: #cf6679; -fx-font-size: 14px;");
            changesLabel.setWrapText(true);

            // Add all components to the content
            content.getChildren().addAll(explanationLabel, changesLabel, grid);
            dialogPane.setContent(content);

            // Add the buttons
            ButtonType confirmButtonType = new ButtonType("Update Information", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

            // Style the buttons
            Button confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            if (confirmButton != null) {
                confirmButton.setStyle("-fx-background-color: #7b1fa2; -fx-text-fill: white;");
            }

            // Show dialog and handle the result
            dialog.showAndWait().ifPresent(result -> {
                if (result == confirmButtonType) {
                    // User confirmed the update
                    try {
                        // Update the guest information
                        existingGuest.setName(newName);
                        existingGuest.setPhoneNumber(newPhone);
                        existingGuest.setAddress(newAddress);

                        // Update in database
                        guestService.updateGuest(existingGuest);
                        LoggingManager.logSystemInfo("Updated guest information for ID: " + existingGuest.getGuestID() +
                                " Fields updated: " + mismatchFields);

                        // Proceed with reservation using the existing guest ID
                        proceedWithReservation(existingGuest.getGuestID());
                    } catch (DatabaseException e) {
                        ErrorPopupManager.showErrorPopup(getStage(), "Database Error: " + e.getMessage());
                        LoggingManager.logException("Error updating guest", e);
                    } catch (IOException e) {
                        ErrorPopupManager.showErrorPopup(getStage(), "Navigation Error: " + e.getMessage());
                        LoggingManager.logException("Error navigating to confirmation", e);
                    } catch (ReservationException e) {
                        ErrorPopupManager.showErrorPopup(getStage(), "Reservation Error: " + e.getMessage());
                        LoggingManager.logException("Error creating reservation", e);
                    } catch (Exception e) {
                        ErrorPopupManager.showErrorPopup(getStage(), "Unexpected Error: " + e.getMessage());
                        LoggingManager.logException("Unexpected error after updating guest", e);
                    }
                }
                // If canceled, just stay on the current screen
            });

        } catch (Exception e) {
            ErrorPopupManager.showErrorPopup(getStage(), "Error showing confirmation dialog: " + e.getMessage());
            LoggingManager.logException("Error showing confirmation dialog", e);
        }
    }

    /**
     * Helper method to add a field row to the comparison grid
     */
    private void addFieldRow(GridPane grid, String fieldName, String currentValue, String newValue, int row, boolean changed) {
        Label fieldLabel = new Label(fieldName + ":");
        Label currentValueLabel = new Label(currentValue);
        Label newValueLabel = new Label(newValue);

        fieldLabel.setStyle("-fx-text-fill: white;");
        currentValueLabel.setStyle("-fx-text-fill: white;");
        newValueLabel.setStyle(changed ? "-fx-text-fill: #cf6679; -fx-font-weight: bold;" : "-fx-text-fill: white;");

        grid.add(fieldLabel, 0, row);
        grid.add(currentValueLabel, 1, row);
        grid.add(newValueLabel, 2, row);
    }

    /**
     * Create a new guest and proceed with reservation
     * @throws DatabaseException If there's a database error
     * @throws IOException If there's an error navigating to confirmation
     * @throws ReservationException If there's an error creating the reservation
     */
    private void createNewGuestAndProceed() throws DatabaseException, IOException, ReservationException {
        // Create and save guest
        Guest guest = new Guest();
        guest.setName(txtName.getText().trim());
        guest.setPhoneNumber(txtPhone.getText().trim());
        guest.setEmail(txtEmail.getText().trim());
        guest.setAddress(txtAddress.getText().trim());

        int guestId = guestService.saveGuest(guest);
        LoggingManager.logSystemInfo("Created new guest with ID: " + guestId);

        // Proceed with reservation
        proceedWithReservation(guestId);
    }

    /**
     * Process the reservation with the provided guest ID
     * @param guestId The guest ID to use for the reservation
     * @throws DatabaseException If there's a database error
     * @throws IOException If there's an error navigating to the confirmation screen
     * @throws ReservationException If there's an error creating the reservation
     */
    private void proceedWithReservation(int guestId) throws DatabaseException, IOException, ReservationException {
        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setGuestID(guestId);
        reservation.setCheckInDate(bookingData.getCheckInDate());
        reservation.setCheckOutDate(bookingData.getCheckOutDate());
        reservation.setNumberOfGuests(bookingData.getGuestCount());
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Create a list to hold ReservationRoom objects
        List<ReservationRoom> reservationRooms = new ArrayList<>();

        // Assign rooms by type
        assignRoomsByType(reservationRooms);

        // Save reservation and rooms
        int reservationId = reservationService.createReservationWithRooms(reservation, reservationRooms);

        // Navigate to confirmation
        navigateToConfirmation(reservationId);
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