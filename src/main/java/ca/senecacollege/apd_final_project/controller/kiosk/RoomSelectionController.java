package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.dao.ReservationRoomDAO;
import ca.senecacollege.apd_final_project.model.ReservationRoom;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.DialogService;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.service.ServiceLocator;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.RulesDialogUtility;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class RoomSelectionController extends BaseController {

    @FXML
    private Button btnBack;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnRules;

    @FXML
    private Label lblGuestSummary;

    @FXML
    private Label lblRoomSummary;

    @FXML
    private Label lblValidationMessage;

    // Single Room Controls
    @FXML
    private Button btnSingleMinus;

    @FXML
    private Button btnSinglePlus;

    @FXML
    private Label lblSingleCount;

    // Double Room Controls
    @FXML
    private Button btnDoubleMinus;

    @FXML
    private Button btnDoublePlus;

    @FXML
    private Label lblDoubleCount;

    // Deluxe Room Controls
    @FXML
    private Button btnDeluxeMinus;

    @FXML
    private Button btnDeluxePlus;

    @FXML
    private Label lblDeluxeCount;

    // Pent House Controls
    @FXML
    private Button btnPentHouseMinus;

    @FXML
    private Button btnPentHousePlus;

    @FXML
    private Label lblPentHouseCount;

    // Room counts
    private int singleRoomCount = 0;
    private int doubleRoomCount = 0;
    private int deluxeRoomCount = 0;
    private int pentHouseCount = 0;

    // Guest assignments to rooms
    private final Map<RoomType, Integer> guestsPerRoomType = new HashMap<>();

    // Booking data from previous screens
    private int guestCount = 1;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    // Services
    private RoomService roomService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Get services from ServiceLocator
        roomService = ServiceLocator.getService(RoomService.class);

        // Apply styles
        applyStyles();

        // Adjust window size immediately
        adjustStageSize();

        // Initialize room counts to zero
        updateRoomCounts();

        // Initialize guest assignments
        guestsPerRoomType.put(RoomType.SINGLE, 0);
        guestsPerRoomType.put(RoomType.DOUBLE, 0);
        guestsPerRoomType.put(RoomType.DELUXE, 0);
        guestsPerRoomType.put(RoomType.PENT_HOUSE, 0);

        LoggingManager.logSystemInfo("RoomSelectionController initialized");
    }

    /**
     * Initialize with booking data from previous screens
     * @param guestCount Number of guests
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     */
    public void initBookingData(int guestCount, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guestCount = guestCount;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;

        // Update guest summary
        lblGuestSummary.setText("Total guests: " + guestCount);

        // Make an initial room suggestion
        suggestRooms();

        LoggingManager.logSystemInfo("Room selection initialized with " + guestCount +
                " guests, check-in: " + checkInDate + ", check-out: " + checkOutDate);
    }

    /**
     * Make an initial room suggestion based on guest count
     */
    private void suggestRooms() {
        // Reset all counts first
        singleRoomCount = 0;
        doubleRoomCount = 0;
        deluxeRoomCount = 0;
        pentHouseCount = 0;

        // Reset guest assignments
        guestsPerRoomType.put(RoomType.SINGLE, 0);
        guestsPerRoomType.put(RoomType.DOUBLE, 0);
        guestsPerRoomType.put(RoomType.DELUXE, 0);
        guestsPerRoomType.put(RoomType.PENT_HOUSE, 0);

        // Determine room allocation based on guest count
        int remainingGuests = guestCount;

        // Try to use double rooms first for larger groups (more efficient)
        while (remainingGuests >= 3) {
            doubleRoomCount++;
            int guestsInRoom = Math.min(remainingGuests, RoomType.DOUBLE.getMaxOccupancy());
            guestsPerRoomType.put(RoomType.DOUBLE, guestsPerRoomType.get(RoomType.DOUBLE) + guestsInRoom);
            remainingGuests -= guestsInRoom;
        }

        // Use single rooms for remaining guests (1-2)
        if (remainingGuests > 0) {
            singleRoomCount++;
            guestsPerRoomType.put(RoomType.SINGLE, remainingGuests);
            remainingGuests = 0;
        }

        // Update the UI
        updateRoomCounts();
        updateSummary();
        validateRoomCapacity();
    }

    /**
     * Apply styles to ensure text elements are properly colored
     */
    private void applyStyles() {
        // Apply text styles to labels
        lblGuestSummary.setStyle("-fx-text-fill: white;");
        lblRoomSummary.setStyle("-fx-text-fill: white;");
        lblValidationMessage.setStyle("-fx-text-fill: #cf6679; -fx-font-weight: bold;");

        lblSingleCount.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        lblDoubleCount.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        lblDeluxeCount.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        lblPentHouseCount.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Apply styles to all labels when scene is available
        lblGuestSummary.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getRoot().lookupAll(".label").forEach(node -> {
                    if (node instanceof Label label &&
                            !label.getStyleClass().contains("label-header") &&
                            !label.equals(lblValidationMessage)) {
                        label.setStyle("-fx-text-fill: white;");
                    }
                });

                // Apply special styling to room selection boxes
                newScene.getRoot().lookupAll(".room-selection-box").forEach(box ->
                        box.setStyle("-fx-background-color: rgba(123, 31, 162, 0.3); -fx-background-radius: 5;"));
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize() {
        Platform.runLater(() -> {
            if (lblGuestSummary.getScene() != null && lblGuestSummary.getScene().getWindow() != null) {
                Stage stage = (Stage) lblGuestSummary.getScene().getWindow();

                // Match dimensions with GuestCount screen
                double stageWidth = ScreenSizeManager.calculateStageWidth(950);
                double stageHeight = ScreenSizeManager.calculateStageHeight(768);

                // Get center position
                double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

                // Set the stage's size and position
                stage.setWidth(stageWidth);
                stage.setHeight(stageHeight);
                stage.setX(centerPos[0]);
                stage.setY(centerPos[1]);

                // Make sure it's not maximized
                stage.setMaximized(false);

                LoggingManager.logSystemInfo("RoomSelectionScreen size matched to GuestCountScreen: " + stageWidth + "x" + stageHeight);
            }
        });

        // Also add a listener for when the scene becomes available
        lblGuestSummary.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                Platform.runLater(() -> {
                    Stage stage = (Stage) newScene.getWindow();

                    // Match dimensions with GuestCount screen
                    double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
                    double stageHeight = ScreenSizeManager.calculateStageHeight(768);

                    // Get center position
                    double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

                    // Set the stage's size and position
                    stage.setWidth(stageWidth);
                    stage.setHeight(stageHeight);
                    stage.setX(centerPos[0]);
                    stage.setY(centerPos[1]);

                    // Make sure it's not maximized
                    stage.setMaximized(false);
                });
            }
        });
    }

    // Handler methods for room selection buttons
    @FXML
    private void handleSingleMinus() {
        if (singleRoomCount > 0) {
            // Adjust guest assignments before changing room count
            adjustGuestAssignments(RoomType.SINGLE, -1);
            singleRoomCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleSinglePlus() {
        // Adjust guest assignments before changing room count
        if (adjustGuestAssignments(RoomType.SINGLE, 1)) {
            singleRoomCount++;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleDoubleMinus() {
        if (doubleRoomCount > 0) {
            // Adjust guest assignments before changing room count
            adjustGuestAssignments(RoomType.DOUBLE, -1);
            doubleRoomCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleDoublePlus() {
        // Adjust guest assignments before changing room count
        if (adjustGuestAssignments(RoomType.DOUBLE, 1)) {
            doubleRoomCount++;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleDeluxeMinus() {
        if (deluxeRoomCount > 0) {
            // Adjust guest assignments before changing room count
            adjustGuestAssignments(RoomType.DELUXE, -1);
            deluxeRoomCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleDeluxePlus() {
        // Adjust guest assignments before changing room count
        if (adjustGuestAssignments(RoomType.DELUXE, 1)) {
            deluxeRoomCount++;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handlePentHouseMinus() {
        if (pentHouseCount > 0) {
            // Adjust guest assignments before changing room count
            adjustGuestAssignments(RoomType.PENT_HOUSE, -1);
            pentHouseCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handlePentHousePlus() {
        // Adjust guest assignments before changing room count
        if (adjustGuestAssignments(RoomType.PENT_HOUSE, 1)) {
            pentHouseCount++;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    /**
     * Adjust guest assignments when adding or removing rooms
     *
     * @param roomType The room type being adjusted
     * @param change +1 for adding a room, -1 for removing a room
     * @return true if adjustment succeeded, false if it failed
     */
    private boolean adjustGuestAssignments(RoomType roomType, int change) {
        if (change > 0) {
            // Adding a room - assign guests if possible
            return assignGuestsToNewRoom(roomType);
        } else {
            // Removing a room - redistribute guests
            return redistributeGuests(roomType);
        }
    }

    /**
     * Assign guests to a new room being added
     *
     * @param roomType The type of room being added
     * @return true if guests were assigned, false if there are no unassigned guests
     */
    private boolean assignGuestsToNewRoom(RoomType roomType) {
        // Calculate how many guests are currently assigned
        int assignedGuests = getTotalAssignedGuests();

        // If all guests are already assigned, no need to add more rooms
        if (assignedGuests >= guestCount) {
            return false;
        }

        // Calculate how many guests to assign to this new room
        int maxCapacity = roomType.getMaxOccupancy();
        int guestsToAssign = Math.min(guestCount - assignedGuests, maxCapacity);

        // Update guest assignments
        guestsPerRoomType.put(roomType, guestsPerRoomType.get(roomType) + guestsToAssign);

        return true;
    }

    /**
     * Redistribute guests when removing a room
     *
     * @param roomType The type of room being removed
     * @return true if guests were successfully redistributed
     */
    private boolean redistributeGuests(RoomType roomType) {
        // Get number of guests in this room type
        int guestsInRoomType = guestsPerRoomType.get(roomType);

        // If this room type has guests, we need to redistribute them
        if (guestsInRoomType > 0) {
            // Calculate average guests per room of this type
            int roomCount = getRoomCount(roomType);
            int avgGuestsPerRoom = roomCount > 0 ? guestsInRoomType / roomCount : 0;

            // Redistribute guests - for simplicity, just remove them from this room type
            // and recalculate assignments for all room types
            guestsPerRoomType.put(roomType, Math.max(0, guestsInRoomType - avgGuestsPerRoom));

            // You could redistribute guests to other room types here if needed
        }

        return true;
    }

    /**
     * Get the count of a specific room type
     */
    private int getRoomCount(RoomType roomType) {
        return switch (roomType) {
            case SINGLE -> singleRoomCount;
            case DOUBLE -> doubleRoomCount;
            case DELUXE -> deluxeRoomCount;
            case PENT_HOUSE -> pentHouseCount;
        };
    }

    /**
     * Get the total number of guests assigned to rooms
     */
    private int getTotalAssignedGuests() {
        return guestsPerRoomType.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Update room count displays
     */
    private void updateRoomCounts() {
        Platform.runLater(() -> {
            lblSingleCount.setText(String.valueOf(singleRoomCount));
            lblDoubleCount.setText(String.valueOf(doubleRoomCount));
            lblDeluxeCount.setText(String.valueOf(deluxeRoomCount));
            lblPentHouseCount.setText(String.valueOf(pentHouseCount));

            // Disable minus buttons if count is zero
            btnSingleMinus.setDisable(singleRoomCount <= 0);
            btnDoubleMinus.setDisable(doubleRoomCount <= 0);
            btnDeluxeMinus.setDisable(deluxeRoomCount <= 0);
            btnPentHouseMinus.setDisable(pentHouseCount <= 0);
        });
    }

    /**
     * Update the summary information
     */
    private void updateSummary() {
        int totalRooms = singleRoomCount + doubleRoomCount + deluxeRoomCount + pentHouseCount;
        int capacityCount = calculateTotalCapacity();
        int assignedGuests = getTotalAssignedGuests();

        lblRoomSummary.setText(String.format("Selected: %d room%s for %d/%d guest%s",
                totalRooms, totalRooms != 1 ? "s" : "",
                assignedGuests, guestCount,
                guestCount != 1 ? "s" : ""));
    }

    /**
     * Calculate the total capacity of selected rooms
     * @return Total capacity
     */
    private int calculateTotalCapacity() {
        return (singleRoomCount * RoomType.SINGLE.getMaxOccupancy()) +
                (doubleRoomCount * RoomType.DOUBLE.getMaxOccupancy()) +
                (deluxeRoomCount * RoomType.DELUXE.getMaxOccupancy()) +
                (pentHouseCount * RoomType.PENT_HOUSE.getMaxOccupancy());
    }

    /**
     * Validate if the selected rooms can accommodate all guests
     */
    private void validateRoomCapacity() {
        int assignedGuests = getTotalAssignedGuests();
        boolean isValid = assignedGuests >= guestCount;

        lblValidationMessage.setVisible(!isValid);
        btnNext.setDisable(!isValid);

        if (!isValid) {
            lblValidationMessage.setText("More rooms are needed to accommodate all " + guestCount + " guests");
        }
    }

    @FXML
    private void handleNextButton() {
        try {
            // Validate room selection
            validateRoomSelection();

            // Create a map of room types to counts and guest assignments
            Map<RoomType, Integer> roomTypeMap = new HashMap<>();
            if (singleRoomCount > 0) roomTypeMap.put(RoomType.SINGLE, singleRoomCount);
            if (doubleRoomCount > 0) roomTypeMap.put(RoomType.DOUBLE, doubleRoomCount);
            if (deluxeRoomCount > 0) roomTypeMap.put(RoomType.DELUXE, deluxeRoomCount);
            if (pentHouseCount > 0) roomTypeMap.put(RoomType.PENT_HOUSE, pentHouseCount);

            // Calculate total price per night
            BigDecimal totalPricePerNight = BigDecimal.ZERO;
            for (Map.Entry<RoomType, Integer> entry : roomTypeMap.entrySet()) {
                RoomType roomType = entry.getKey();
                int roomCount = entry.getValue();
                BigDecimal roomPrice = BigDecimal.valueOf(roomType.getBasePrice());
                totalPricePerNight = totalPricePerNight.add(roomPrice.multiply(BigDecimal.valueOf(roomCount)));
            }

            // Create an instance of ReservationRoom and set the price per night
            ReservationRoom reservationRoom = new ReservationRoom();
            reservationRoom.setPricePerNight(totalPricePerNight);

            // Prepare booking data
            BookingData bookingData = new BookingData();
            bookingData.setGuestCount(guestCount);
            bookingData.setCheckInDate(checkInDate);
            bookingData.setCheckOutDate(checkOutDate);
            bookingData.setSingleRoomCount(singleRoomCount);
            bookingData.setDoubleRoomCount(doubleRoomCount);
            bookingData.setDeluxeRoomCount(deluxeRoomCount);
            bookingData.setPentHouseCount(pentHouseCount);

            // Store guest assignments per room type
            bookingData.setGuestsPerRoomType(new HashMap<>(guestsPerRoomType));

            // Load the guest details screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_GUEST_DETAILS));
            Parent guestDetailsRoot = loader.load();

            // Get the controller and pass the booking data
            GuestDetailsController guestDetailsController = loader.getController();
            guestDetailsController.initBookingData(bookingData);

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene guestDetailsScene = new Scene(guestDetailsRoot);
            guestDetailsScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            guestDetailsScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the scene
            stage.setScene(guestDetailsScene);

            // Size the stage properly using ScreenSizeManager
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);

            // Calculate center position
            double[] stagePosition = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Apply dimensions and position
            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(stagePosition[0]);
            stage.setY(stagePosition[1]);

            LoggingManager.logSystemInfo("Navigated to guest details screen with " +
                    singleRoomCount + " single rooms, " + doubleRoomCount + " double rooms, " +
                    deluxeRoomCount + " deluxe rooms, " + pentHouseCount + " pent houses");

        } catch (ValidationException e) {
            DialogService.showWarning(getStage(), "Validation Error", e.getMessage());
        } catch (IOException e) {
            LoggingManager.logException("Error navigating to guest details screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error loading guest details screen: " + e.getMessage(), e);
        }
    }


    /**
     * Validate the room selection
     * @throws ValidationException if validation fails
     */
    private void validateRoomSelection() throws ValidationException {
        // Check if any rooms are selected
        int totalRooms = singleRoomCount + doubleRoomCount + deluxeRoomCount + pentHouseCount;
        if (totalRooms == 0) {
            throw new ValidationException("Please select at least one room.");
        }

        // Check if all guests are assigned to rooms
        int assignedGuests = getTotalAssignedGuests();
        if (assignedGuests < guestCount) {
            throw new ValidationException("The selected rooms cannot accommodate all " + guestCount + " guests. Please select more rooms.");
        }

        // Check room availability
        try {
            // Check if enough rooms of each type are available
            validateRoomAvailability(RoomType.SINGLE, singleRoomCount);
            validateRoomAvailability(RoomType.DOUBLE, doubleRoomCount);
            validateRoomAvailability(RoomType.DELUXE, deluxeRoomCount);
            validateRoomAvailability(RoomType.PENT_HOUSE, pentHouseCount);

        } catch (DatabaseException e) {
            LoggingManager.logException("Error checking room availability", e);
            throw new ValidationException("Unable to verify room availability: " + e.getMessage());
        }
    }

    /**
     * Validate availability for a specific room type
     */
    private void validateRoomAvailability(RoomType roomType, int count) throws ValidationException, DatabaseException {
        if (count > 0) {
            int availableRooms = roomService.countAvailableRooms(roomType, checkInDate, checkOutDate);
            if (availableRooms < count) {
                throw new ValidationException("Only " + availableRooms + " " + roomType.getDisplayName() + "(s) available. " +
                        "You requested " + count + ". Please adjust your selection.");
            }
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            // Load the date selection screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_DATE_SELECTION));
            Parent dateSelectionRoot = loader.load();

            // Get the controller and pass the guest count
            DateSelectionController dateSelectionController = loader.getController();
            dateSelectionController.initGuestCount(guestCount);

            /// Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene dateSelectionScene = new Scene(dateSelectionRoot);
            dateSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            dateSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the new scene
            stage.setScene(dateSelectionScene);

            // Size the stage properly
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            LoggingManager.logSystemInfo("Returned to date selection screen from room selection screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to date selection screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error returning to date selection screen: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleRulesButton() {
        RulesDialogUtility.showRulesDialog(btnRules);
    }

    @Override
    protected Stage getStage() {
        if (btnNext != null && btnNext.getScene() != null) {
            return (Stage) btnNext.getScene().getWindow();
        }
        return null;
    }
}