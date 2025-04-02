package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.service.ServiceLocator;
import ca.senecacollege.apd_final_project.service.DialogService;
import ca.senecacollege.apd_final_project.util.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class BookingScreenController extends BaseController {

    public Button btnBack;
    @FXML
    private BorderPane mainPane;

    @FXML
    private Spinner<Integer> spnGuests;

    @FXML
    private DatePicker dpCheckIn;

    @FXML
    private DatePicker dpCheckOut;

    @FXML
    private ComboBox<RoomType> cmbRoomType;

    @FXML
    private Label lblSuggestion;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnRules;

    private RoomService roomService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        roomService = ServiceLocator.getService(RoomService.class);

        // Apply styles directly to the components
        mainPane.setStyle("-fx-background-color: #121212;");

        for (javafx.scene.Node node : mainPane.lookupAll(".label")) {
            if (node instanceof Label label) {
                label.setStyle("-fx-text-fill: white;");
            }
        }

        lblSuggestion.setStyle("-fx-text-fill: #b491c8; -fx-background-color: rgba(180, 145, 200, 0.2); -fx-background-radius: 5px; -fx-padding: 10px;");

        // Setup spinner for guest count
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        spnGuests.setValueFactory(valueFactory);

        TextField spinnerEditor = spnGuests.getEditor();
        spinnerEditor.setPadding(new Insets(0));
        spinnerEditor.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");

        // Add listener to guest count to update room suggestions
        spnGuests.valueProperty().addListener((obs, oldValue, newValue) -> updateRoomSuggestion());

        // Setup date pickers
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(1));

        dpCheckIn.getEditor().setPadding(new Insets(0));
        dpCheckOut.getEditor().setPadding(new Insets(0));

        dpCheckIn.getEditor().setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
        dpCheckOut.getEditor().setStyle("-fx-text-fill: black; -fx-font-size: 14px;");

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
                setDisable(empty || date.isBefore(dpCheckIn.getValue()));
            }
        });

        // Add listeners to date pickers to handle dates properly
        dpCheckIn.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (dpCheckOut.getValue().isBefore(newValue)) {
                dpCheckOut.setValue(newValue.plusDays(1));
            }
        });

        // Setup room type combo box with custom string converter
        cmbRoomType.setItems(FXCollections.observableArrayList(RoomType.values()));
        cmbRoomType.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomType roomType) {
                if (roomType != null) {
                    return roomType.getDisplayName() + " - $" + roomType.getBasePrice() +
                            " (Max " + roomType.getMaxOccupancy() + " guests)";
                }
                return null;
            }

            @Override
            public RoomType fromString(String string) {
                for (RoomType type : RoomType.values()) {
                    if (string.startsWith(type.getDisplayName())) {
                        return type;
                    }
                }
                return null;
            }
        });

        // Set combo box's font size
        cmbRoomType.setStyle("-fx-font-size: 14px;");

        // Make initial room suggestion
        updateRoomSuggestion();

        // Call parent initialize
        super.initialize(url, resourceBundle);
    }

    private void updateRoomSuggestion() {
        int guestCount = spnGuests.getValue();
        List<RoomType> recommendedRooms = new ArrayList<>();

        StringBuilder suggestionBuilder = new StringBuilder("Based on your party size, we recommend ");

        if (guestCount <= Constants.MAX_GUESTS_SINGLE_ROOM) {
            // Single room is sufficient
            recommendedRooms.add(RoomType.SINGLE);
            suggestionBuilder.append("a Single Room.");
        } else if (guestCount <= Constants.MAX_GUESTS_DOUBLE_ROOM) {
            // Double room is sufficient
            recommendedRooms.add(RoomType.DOUBLE);
            suggestionBuilder.append("a Double Room.");
        } else {
            // Multiple rooms needed
            int doubleRooms = guestCount / Constants.MAX_GUESTS_DOUBLE_ROOM;
            int remainingGuests = guestCount % Constants.MAX_GUESTS_DOUBLE_ROOM;

            suggestionBuilder.append(doubleRooms)
                    .append(" Double Room")
                    .append(doubleRooms > 1 ? "s" : "");

            if (remainingGuests > 0) {
                suggestionBuilder.append(" and ");
            }

            if (remainingGuests > 0) {
                String roomType = (remainingGuests <= Constants.MAX_GUESTS_SINGLE_ROOM ? "Single" : "Double");
                suggestionBuilder.append("1 ").append(roomType).append(" Room");
            }

            suggestionBuilder.append(".");
        }

        // Update the suggestion label with the complete text
        String suggestion = suggestionBuilder.toString();
        lblSuggestion.setText(suggestion);

        lblSuggestion.setStyle("-fx-text-fill: #b491c8; -fx-background-color: rgba(180, 145, 200, 0.2); -fx-background-radius: 5px; -fx-padding: 10px;");

        // Set first recommended room type as default if nothing is selected
        if (cmbRoomType.getValue() == null && !recommendedRooms.isEmpty()) {
            cmbRoomType.setValue(recommendedRooms.get(0));
        }
    }

    @FXML
    private void handleNextButton() {
        // Validate inputs
        if (validateFields()) {
            return;
        }

        try {
            // Load the guest details screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_GUEST_DETAILS));
            Parent guestDetailsRoot = loader.load();

            // Get the controller and pass the booking data
            GuestDetailsController guestDetailsController = loader.getController();
            guestDetailsController.initBookingData(
                    spnGuests.getValue(),
                    dpCheckIn.getValue(),
                    dpCheckOut.getValue(),
                    cmbRoomType.getValue()
            );

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

            logSystemActivity("Navigated to guest details screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating to guest details screen", e);
            DialogService.showError(getStage(), "Navigation Error", "Error loading guest details screen: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            // Load the kiosk welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent welcomeRoot = loader.load();

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene welcomeScene = new Scene(welcomeRoot);
            welcomeScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            welcomeScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the scene
            stage.setScene(welcomeScene);

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

            logSystemActivity("Navigated back to welcome screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to welcome screen", e);
            DialogService.showError(getStage(), "Navigation Error", "Error returning to welcome screen: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleRulesButton() {
        RulesDialogUtility.showRulesDialog(btnRules);
    }

    @Override
    protected boolean validateFields() {
        try {
            // Validate guest count
            if (spnGuests.getValue() <= 0) {
                throw new ValidationException("Please enter a valid number of guests.");
            }

            // Validate check-in date
            if (dpCheckIn.getValue() == null) {
                throw new ValidationException("Please select a check-in date.");
            }

            // Check if check-in date is today or in the future
            if (ValidationUtils.isValidFutureDate(dpCheckIn.getValue())) {
                throw new ValidationException("Check-in date must be today or a future date.");
            }

            // Validate check-out date
            if (dpCheckOut.getValue() == null) {
                throw new ValidationException("Please select a check-out date.");
            }

            // Check if check-out date is after check-in date
            if (ValidationUtils.isValidDateRange(dpCheckIn.getValue(), dpCheckOut.getValue())) {
                throw new ValidationException("Check-out date must be after check-in date.");
            }

            // Validate room type selection
            if (cmbRoomType.getValue() == null) {
                throw new ValidationException("Please select a room type.");
            }

            // Check if guest count is valid for the selected room type
            RoomType selectedRoomType = cmbRoomType.getValue();
            int guestCount = spnGuests.getValue();

            if (selectedRoomType == RoomType.SINGLE && guestCount > Constants.MAX_GUESTS_SINGLE_ROOM) {
                throw new ValidationException("Single room can accommodate maximum " +
                        Constants.MAX_GUESTS_SINGLE_ROOM + " guests.");
            } else if (selectedRoomType == RoomType.DOUBLE && guestCount > Constants.MAX_GUESTS_DOUBLE_ROOM) {
                throw new ValidationException("Double room can accommodate maximum " +
                        Constants.MAX_GUESTS_DOUBLE_ROOM + " guests.");
            } else if ((selectedRoomType == RoomType.DELUXE || selectedRoomType == RoomType.PENT_HOUSE) &&
                    guestCount > Constants.MAX_GUESTS_DELUXE_ROOM) {
                throw new ValidationException(selectedRoomType.getDisplayName() + " can accommodate maximum " +
                        Constants.MAX_GUESTS_DELUXE_ROOM + " guests.");
            }

            // Check room availability
            if (!roomService.checkRoomAvailability(selectedRoomType, dpCheckIn.getValue(), dpCheckOut.getValue())) {
                throw new ValidationException("Sorry, no " + selectedRoomType.getDisplayName() +
                        " is available for the selected dates.");
            }

            // All validations passed
            return false;

        } catch (ValidationException e) {
            // Show validation error using DialogService
            DialogService.showWarning(getStage(), "Validation Error", e.getMessage());
            return true;
        }
    }

    /**
     * Get the current stage
     */
    @Override
    protected Stage getStage() {
        if (btnNext != null && btnNext.getScene() != null) {
            return (Stage) btnNext.getScene().getWindow();
        }
        return null;
    }

    /**
     * Log a system activity
     */
    private void logSystemActivity(String activity) {
        LoggingManager.logSystemInfo(activity);
    }
}