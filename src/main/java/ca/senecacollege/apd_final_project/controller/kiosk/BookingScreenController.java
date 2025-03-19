package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Insets;

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
    private TextFlow suggestionTextFlow;

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

        // Initialize suggestion TextFlow (for highlighting)
        suggestionTextFlow = new TextFlow();
        suggestionTextFlow.getStyleClass().add("suggestion-textflow");

        // Make initial room suggestion
        updateRoomSuggestion();

        LoggingManager.logSystemInfo("BookingScreenController initialized");
    }

    private void updateRoomSuggestion() {
        int guestCount = spnGuests.getValue();
        List<RoomType> recommendedRooms = new ArrayList<>();

        StringBuilder suggestion = new StringBuilder("Based on your party size, we recommend ");

        if (guestCount <= Constants.MAX_GUESTS_SINGLE_ROOM) {
            // Single room is sufficient
            recommendedRooms.add(RoomType.SINGLE);
            suggestion.append("a Single Room.");
        } else if (guestCount <= Constants.MAX_GUESTS_DOUBLE_ROOM) {
            // Double room is sufficient
            recommendedRooms.add(RoomType.DOUBLE);
            suggestion.append("a Double Room.");
        } else {
            // Multiple rooms needed
            int doubleRooms = guestCount / Constants.MAX_GUESTS_DOUBLE_ROOM;
            int remainingGuests = guestCount % Constants.MAX_GUESTS_DOUBLE_ROOM;

            if (doubleRooms > 0) {
                suggestion.append(doubleRooms)
                        .append(" Double Room")
                        .append(doubleRooms > 1 ? "s" : "");

                if (remainingGuests > 0) {
                    suggestion.append(" and ");
                }
            }

            if (remainingGuests > 0) {
                String roomType = (remainingGuests <= Constants.MAX_GUESTS_SINGLE_ROOM ? "Single" : "Double");
                suggestion.append("1 ").append(roomType).append(" Room");
            }

            suggestion.append(".");
        }

        // Update the suggestion label with the full, plain text message
        lblSuggestion.setText(suggestion.toString());

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
            showError("Error loading next screen: " + e.getMessage());

            // More detailed error information for debugging
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            LoggingManager.logSystemInfo("Opening kiosk interface");

            // Load the kiosk welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent kioskRoot = loader.load();

            // Create a new stage for the kiosk
            Stage kioskStage = new Stage();
            Scene kioskScene = new Scene(kioskRoot);

            // Apply the kiosk CSS
            kioskScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Configure and show the kiosk stage
            kioskStage.setTitle("Hotel ABC Kiosk");
            kioskStage.setScene(kioskScene);
            kioskStage.setMaximized(true); // Full screen for kiosk mode
            kioskStage.show();

        } catch (IOException e) {
            LoggingManager.logException("Error opening kiosk interface", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        // Create an improved rules and regulations dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Hotel Rules & Regulations");

        // Create a custom dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().addAll("root", "rules-dialog");

        // Create a VBox for content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Add header
        Label header = new Label("Please Read Our Rules & Regulations");
        header.getStyleClass().add("header");

        // Add scrollable text area for rules content
        TextArea rulesText = new TextArea(Constants.RULES_REGULATIONS);
        rulesText.setEditable(false);
        rulesText.setWrapText(true);
        rulesText.setPrefHeight(400);
        rulesText.setPrefWidth(600);
        rulesText.getStyleClass().add("content");

        // Add components to content
        content.getChildren().addAll(header, rulesText);

        // Set the content
        dialogPane.setContent(content);

        // Add the close button
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        // Show the dialog
        dialog.showAndWait();
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

        // Add fade-in animation for error message
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), lblError);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}