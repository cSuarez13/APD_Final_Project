package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.service.DialogService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.RulesDialogUtility;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.ResourceBundle;

public class DateSelectionController extends BaseController {

    public BorderPane mainPane;
    @FXML
    private Button btnBack;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnRules;

    @FXML
    private DatePicker dpCheckIn;

    @FXML
    private DatePicker dpCheckOut;

    @FXML
    private Label lblStayInfo;

    private int guestCount = 1; // Will be set from previous screen

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Setup date pickers
        setupDatePickers();

        // Apply styles
        applyStyles();

        // Adjust window size
        adjustStageSize();

        LoggingManager.logSystemInfo("DateSelectionController initialized");
    }

    /**
     * Initialize with guest count from previous screen
     * @param guestCount Number of guests
     */
    public void initGuestCount(int guestCount) {
        this.guestCount = guestCount;
        LoggingManager.logSystemInfo("Date selection initialized with " + guestCount + " guests");
    }

    /**
     * Setup date pickers with constraints and listeners
     */
    private void setupDatePickers() {
        // Set initial dates
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(1));

        // Update stay information display
        updateStayInfo();

        // Ensure check-in date is not before today
        dpCheckIn.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Ensure check-out date is after check-in date
        dpCheckOut.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = dpCheckIn.getValue();
                setDisable(empty || (checkInDate != null && date.isBefore(checkInDate)));
            }
        });

        // Add listeners to update stay information when dates change
        dpCheckIn.valueProperty().addListener((obs, oldValue, newValue) -> {
            // If check-out date is now invalid, update it
            if (dpCheckOut.getValue() != null && dpCheckOut.getValue().isBefore(newValue)) {
                dpCheckOut.setValue(newValue.plusDays(1));
            }
            updateStayInfo();
        });

        dpCheckOut.valueProperty().addListener((obs, oldValue, newValue) -> updateStayInfo());
    }

    /**
     * Update the stay information display
     */
    private void updateStayInfo() {
        if (dpCheckIn.getValue() != null && dpCheckOut.getValue() != null) {
            long nights = ChronoUnit.DAYS.between(dpCheckIn.getValue(), dpCheckOut.getValue());
            lblStayInfo.setText(String.format("Your stay: %d night%s", nights, nights != 1 ? "s" : ""));
        } else {
            lblStayInfo.setText("Your stay: 0 nights");
        }
    }

    /**
     * Apply styles to ensure text elements are properly colored
     */
    private void applyStyles() {
        lblStayInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Style date picker text fields
        dpCheckIn.getEditor().setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
        dpCheckOut.getEditor().setStyle("-fx-text-fill: black; -fx-font-size: 14px;");

        lblStayInfo.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getRoot().lookupAll(".label").forEach(node -> {
                    if (node instanceof Label label &&
                            !label.getStyleClass().contains("label-header") &&
                            !label.equals(lblStayInfo)) {
                        label.setStyle("-fx-text-fill: white;");
                    }
                });
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize() {
        lblStayInfo.sceneProperty().addListener((obs, oldScene, newScene) -> {
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

                LoggingManager.logSystemInfo("DateSelectionScreen size adjusted to " + stageWidth + "x" + stageHeight);
            }
        });
    }

    @FXML
    private void handleNextButton() {
        try {
            // Validate dates
            validateDates();

            // Load the room selection screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_ROOM_SELECTION));
            Parent roomSelectionRoot = loader.load();

            // Get the controller and pass the booking data
            RoomSelectionController roomSelectionController = loader.getController();
            roomSelectionController.initBookingData(guestCount, dpCheckIn.getValue(), dpCheckOut.getValue());

            // Get the current stage
            Stage stage = getStage();

            // Create new scene with compact size for room selection
            Scene roomSelectionScene = new Scene(roomSelectionRoot);
            roomSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            roomSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the new scene
            stage.setScene(roomSelectionScene);

            // Set optimal size for room selection screen
            double stageWidth = 950;
            double stageHeight = 768;
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);
            stage.setMaximized(false);

            LoggingManager.logSystemInfo("Navigated to room selection screen with " + guestCount +
                    " guests, check-in: " + dpCheckIn.getValue() + ", check-out: " + dpCheckOut.getValue());

        } catch (ValidationException e) {
            DialogService.showWarning(getStage(), "Validation Error", e.getMessage());
        } catch (IOException e) {
            LoggingManager.logException("Error navigating to room selection screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error loading room selection screen: " + e.getMessage(), e);
        }
    }

    /**
     * Validate the selected dates
     * @throws ValidationException if validation fails
     */
    private void validateDates() throws ValidationException {
        LocalDate checkInDate = dpCheckIn.getValue();
        LocalDate checkOutDate = dpCheckOut.getValue();

        // Validate check-in date
        if (checkInDate == null) {
            throw new ValidationException("Please select a check-in date.");
        }

        // Check if check-in date is today or in the future
        if (ValidationUtils.isValidFutureDate(checkInDate)) {
            throw new ValidationException("Check-in date must be today or a future date.");
        }

        // Validate check-out date
        if (checkOutDate == null) {
            throw new ValidationException("Please select a check-out date.");
        }

        // Check if check-out date is after check-in date
        if (ValidationUtils.isValidDateRange(checkInDate, checkOutDate)) {
            throw new ValidationException("Check-out date must be after check-in date.");
        }

        // Check if the stay is within a reasonable range (e.g., 30 days)
        long nightCount = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nightCount > 30) {
            throw new ValidationException("Stays longer than 30 nights require special booking. Please contact the front desk.");
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            // Load the guest count screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_GUEST_COUNT));
            Parent guestCountRoot = loader.load();

            // Get the current stage
            Stage stage = getStage();

            // Calculate dimensions using ScreenSizeManager - use 95% of screen height
            Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();
            double aspectRatio = 1024.0 / 768.0; // Original aspect ratio
            double targetHeight = screenBounds.getHeight() * 0.95;
            double targetWidth = targetHeight * aspectRatio;

            // Create new scene with calculated dimensions
            Scene guestCountScene = new Scene(guestCountRoot, targetWidth, targetHeight);

            guestCountScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            guestCountScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            double[] centerPos = ScreenSizeManager.centerStageOnScreen(targetWidth, targetHeight);

            // Apply position and scene
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);
            stage.setWidth(targetWidth);
            stage.setHeight(targetHeight);
            stage.setScene(guestCountScene);
            stage.setMaximized(false);
            stage.show();

            LoggingManager.logSystemInfo("Returned to guest count screen from date selection screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to guest count screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error returning to guest count screen: " + e.getMessage(), e);
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