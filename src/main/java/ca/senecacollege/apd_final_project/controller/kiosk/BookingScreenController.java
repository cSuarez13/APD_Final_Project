package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BookingScreenController implements Initializable {

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
    private Label lblError;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnRules;

    private RoomService roomService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roomService = new RoomService();

        // Setup spinner for guest count
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        spnGuests.setValueFactory(valueFactory);

        // Add listener to guest count to update room suggestions
        spnGuests.valueProperty().addListener((obs, oldValue, newValue) -> updateRoomSuggestion());

        // Setup date pickers
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(1));

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

        // Setup room type combo box
        cmbRoomType.setItems(FXCollections.observableArrayList(RoomType.values()));

        // Hide error label initially
        lblError.setVisible(false);

        // Make initial room suggestion
        updateRoomSuggestion();

        LoggingManager.logSystemInfo("BookingScreenController initialized");
    }

    private void updateRoomSuggestion() {
        int guestCount = spnGuests.getValue();
        List<RoomType> recommendedRooms = new ArrayList<>();
        String suggestionText = "";

        if (guestCount <= Constants.MAX_GUESTS_SINGLE_ROOM) {
            // Single room is sufficient
            recommendedRooms.add(RoomType.SINGLE);
            suggestionText = "Based on your party size, we recommend a Single Room.";
        } else if (guestCount <= Constants.MAX_GUESTS_DOUBLE_ROOM) {
            // Double room is sufficient
            recommendedRooms.add(RoomType.DOUBLE);
            suggestionText = "Based on your party size, we recommend a Double Room.";
        } else {
            // Multiple rooms needed
            int doubleRooms = guestCount / Constants.MAX_GUESTS_DOUBLE_ROOM;
            int remainingGuests = guestCount % Constants.MAX_GUESTS_DOUBLE_ROOM;

            suggestionText = "Based on your party size, we recommend ";

            if (doubleRooms > 0) {
                suggestionText += doubleRooms + " Double Room" + (doubleRooms > 1 ? "s" : "");

                if (remainingGuests > 0) {
                    suggestionText += " and ";
                }
            }

            if (remainingGuests > 0) {
                suggestionText += "1 " + (remainingGuests <= Constants.MAX_GUESTS_SINGLE_ROOM ?
                        "Single" : "Double") + " Room";
            }

            suggestionText += ".";
        }

        // Update suggestion label
        lblSuggestion.setText(suggestionText);

        // Set first recommended room type as default if nothing is selected
        if (cmbRoomType.getValue() == null && !recommendedRooms.isEmpty()) {
            cmbRoomType.setValue(recommendedRooms.get(0));
        }
    }

    @FXML
    private void handleNextButton(ActionEvent event) {
        // Validate inputs
        if (!validateInputs()) {
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
            Stage stage = (Stage) btnNext.getScene().getWindow();

            // Create new scene
            Scene guestDetailsScene = new Scene(guestDetailsRoot);
            guestDetailsScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the new scene
            stage.setScene(guestDetailsScene);

            LoggingManager.logSystemInfo("Navigated to guest details screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating to guest details screen", e);
            showError("System error. Please try again or contact front desk.");
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            // Load the welcome screen again
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent welcomeRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) btnBack.getScene().getWindow();

            // Create new scene
            Scene welcomeScene = new Scene(welcomeRoot);
            welcomeScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the new scene
            stage.setScene(welcomeScene);

            LoggingManager.logSystemInfo("Navigated back to welcome screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to welcome screen", e);
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        // Show rules and regulations dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Rules & Regulations");
        alert.setHeaderText("Please Read Our Rules & Regulations");
        alert.setContentText(Constants.RULES_REGULATIONS);

        // Apply CSS to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().add("root");

        alert.showAndWait();
    }

    private boolean validateInputs() {
        // Validate guest count
        if (spnGuests.getValue() <= 0) {
            showError("Please enter a valid number of guests.");
            return false;
        }

        // Validate check-in date
        if (dpCheckIn.getValue() == null) {
            showError("Please select a check-in date.");
            return false;
        }

        if (!ValidationUtils.isValidFutureDate(dpCheckIn.getValue())) {
            showError("Check-in date must be today or a future date.");
            return false;
        }

        // Validate check-out date
        if (dpCheckOut.getValue() == null) {
            showError("Please select a check-out date.");
            return false;
        }

        if (!ValidationUtils.isValidDateRange(dpCheckIn.getValue(), dpCheckOut.getValue())) {
            showError("Check-out date must be after check-in date.");
            return false;
        }

        // Validate room type selection
        if (cmbRoomType.getValue() == null) {
            showError("Please select a room type.");
            return false;
        }

        // Check if guest count is valid for the selected room type
        RoomType selectedRoomType = cmbRoomType.getValue();
        int guestCount = spnGuests.getValue();

        if (selectedRoomType == RoomType.SINGLE && guestCount > Constants.MAX_GUESTS_SINGLE_ROOM) {
            showError("Single room can accommodate maximum " + Constants.MAX_GUESTS_SINGLE_ROOM + " guests.");
            return false;
        } else if (selectedRoomType == RoomType.DOUBLE && guestCount > Constants.MAX_GUESTS_DOUBLE_ROOM) {
            showError("Double room can accommodate maximum " + Constants.MAX_GUESTS_DOUBLE_ROOM + " guests.");
            return false;
        } else if ((selectedRoomType == RoomType.DELUXE || selectedRoomType == RoomType.PENT_HOUSE) &&
                guestCount > Constants.MAX_GUESTS_DELUXE_ROOM) {
            showError(selectedRoomType.getDisplayName() + " can accommodate maximum " +
                    Constants.MAX_GUESTS_DELUXE_ROOM + " guests.");
            return false;
        }

        // Check room availability
        if (!roomService.checkRoomAvailability(selectedRoomType, dpCheckIn.getValue(), dpCheckOut.getValue())) {
            showError("Sorry, no " + selectedRoomType.getDisplayName() + " is available for the selected dates.");
            return false;
        }

        // All validations passed
        return true;
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}