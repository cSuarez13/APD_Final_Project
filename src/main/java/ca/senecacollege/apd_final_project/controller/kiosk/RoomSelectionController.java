package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
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
import java.net.URL;
import java.time.LocalDate;
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

        // Adjust window size
        adjustStageSize();

        // Initialize room counts to zero
        updateRoomCounts();

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

        // Simple algorithm for room suggestions
        if (guestCount <= Constants.MAX_GUESTS_SINGLE_ROOM) {
            // Single room is sufficient for 1-2 guests
            singleRoomCount = 1;
        } else if (guestCount <= Constants.MAX_GUESTS_DOUBLE_ROOM) {
            // Double room is sufficient for 3-4 guests
            doubleRoomCount = 1;
        } else {
            // For more guests, use a combination of rooms
            int remainingGuests = guestCount;

            // First, allocate as many double rooms as needed for groups of 4
            doubleRoomCount = remainingGuests / Constants.MAX_GUESTS_DOUBLE_ROOM;
            remainingGuests %= Constants.MAX_GUESTS_DOUBLE_ROOM;

            // Then allocate a single room if needed for 1-2 remaining guests
            if (remainingGuests > 0) {
                singleRoomCount = 1;
            }
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
        lblGuestSummary.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                Stage stage = (Stage) newScene.getWindow();

                // Calculate dimensions
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

                LoggingManager.logSystemInfo("RoomSelectionScreen size adjusted to " + stageWidth + "x" + stageHeight);
            }
        });
    }

    // Handler methods for room selection buttons
    @FXML
    private void handleSingleMinus() {
        if (singleRoomCount > 0) {
            singleRoomCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleSinglePlus() {
        singleRoomCount++;
        updateRoomCounts();
        updateSummary();
        validateRoomCapacity();
    }

    @FXML
    private void handleDoubleMinus() {
        if (doubleRoomCount > 0) {
            doubleRoomCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleDoublePlus() {
        doubleRoomCount++;
        updateRoomCounts();
        updateSummary();
        validateRoomCapacity();
    }

    @FXML
    private void handleDeluxeMinus() {
        if (deluxeRoomCount > 0) {
            deluxeRoomCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handleDeluxePlus() {
        deluxeRoomCount++;
        updateRoomCounts();
        updateSummary();
        validateRoomCapacity();
    }

    @FXML
    private void handlePentHouseMinus() {
        if (pentHouseCount > 0) {
            pentHouseCount--;
            updateRoomCounts();
            updateSummary();
            validateRoomCapacity();
        }
    }

    @FXML
    private void handlePentHousePlus() {
        pentHouseCount++;
        updateRoomCounts();
        updateSummary();
        validateRoomCapacity();
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

        lblRoomSummary.setText(String.format("Selected: %d room%s for up to %d guest%s",
                totalRooms, totalRooms != 1 ? "s" : "",
                capacityCount, capacityCount != 1 ? "s" : ""));
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
        int capacity = calculateTotalCapacity();
        boolean isValid = capacity >= guestCount;

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

            // Load the guest details screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_GUEST_DETAILS));
            Parent guestDetailsRoot = loader.load();

            // Get the controller and pass the booking data
            GuestDetailsController guestDetailsController = loader.getController();

            // Create a new BookingData object to hold all the information
            BookingData bookingData = new BookingData();
            bookingData.setGuestCount(guestCount);
            bookingData.setCheckInDate(checkInDate);
            bookingData.setCheckOutDate(checkOutDate);
            bookingData.setSingleRoomCount(singleRoomCount);
            bookingData.setDoubleRoomCount(doubleRoomCount);
            bookingData.setDeluxeRoomCount(deluxeRoomCount);
            bookingData.setPentHouseCount(pentHouseCount);

            guestDetailsController.initBookingData(bookingData);

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene guestDetailsScene = new Scene(guestDetailsRoot);
            guestDetailsScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            guestDetailsScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the new scene
            stage.setScene(guestDetailsScene);

            // Size the stage properly
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

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

        // Check if the selected rooms can accommodate all guests
        int capacity = calculateTotalCapacity();
        if (capacity < guestCount) {
            throw new ValidationException("The selected rooms cannot accommodate all " + guestCount + " guests. Please select more rooms.");
        }

        // Check room availability by attempting to find available rooms of each type
        try {
            // We need to check if enough rooms of each type are available
            if (singleRoomCount > 0) {
                int availableSingleRooms = roomService.countAvailableRooms(RoomType.SINGLE, checkInDate, checkOutDate);
                if (availableSingleRooms < singleRoomCount) {
                    throw new ValidationException("Only " + availableSingleRooms + " Single Room(s) available. " +
                            "You requested " + singleRoomCount + ". Please adjust your selection.");
                }
            }

            if (doubleRoomCount > 0) {
                int availableDoubleRooms = roomService.countAvailableRooms(RoomType.DOUBLE, checkInDate, checkOutDate);
                if (availableDoubleRooms < doubleRoomCount) {
                    throw new ValidationException("Only " + availableDoubleRooms + " Double Room(s) available. " +
                            "You requested " + doubleRoomCount + ". Please adjust your selection.");
                }
            }

            if (deluxeRoomCount > 0) {
                int availableDeluxeRooms = roomService.countAvailableRooms(RoomType.DELUXE, checkInDate, checkOutDate);
                if (availableDeluxeRooms < deluxeRoomCount) {
                    throw new ValidationException("Only " + availableDeluxeRooms + " Deluxe Room(s) available. " +
                            "You requested " + deluxeRoomCount + ". Please adjust your selection.");
                }
            }

            if (pentHouseCount > 0) {
                int availablePentHouses = roomService.countAvailableRooms(RoomType.PENT_HOUSE, checkInDate, checkOutDate);
                if (availablePentHouses < pentHouseCount) {
                    throw new ValidationException("Only " + availablePentHouses + " Pent House(s) available. " +
                            "You requested " + pentHouseCount + ". Please adjust your selection.");
                }
            }
        } catch (DatabaseException e) {
            LoggingManager.logException("Error checking room availability", e);
            throw new ValidationException("Unable to verify room availability: " + e.getMessage());
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

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene dateSelectionScene = new Scene(dateSelectionRoot);
            dateSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            dateSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the scene
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