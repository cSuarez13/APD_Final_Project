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
import java.time.LocalDate;
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
    private Label lblError;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnRules;

    // Booking data from previous screen
    private int numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private RoomType roomType;

    // Services
    private GuestService guestService;
    private ReservationService reservationService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        guestService = ServiceLocator.getService(GuestService.class);
        reservationService = ServiceLocator.getService(ReservationService.class);

        // Hide error label initially
        hideError();

        // Apply proper text styling to ensure visibility
        applyStyles();

        // Call parent initialize
        super.initialize(url, resourceBundle);
    }

    /**
     * Apply styles to ensure text is visible
     */
    private void applyStyles() {
        // Set explicit styling for text fields to ensure text is visible
        String textFieldStyle = "-fx-text-fill: white; -fx-font-size: 16px;";
        txtName.setStyle(textFieldStyle);
        txtPhone.setStyle(textFieldStyle);
        txtEmail.setStyle(textFieldStyle);
        txtAddress.setStyle(textFieldStyle);

        // Make sure labels have white text
        lblError.setStyle("-fx-text-fill: #cf6679; -fx-font-size: 14px;");

        // Set all field labels to white text explicitly when scene is available
        txtName.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Find all labels in the GridPane and set their text fill to white
                txtName.getParent().getParent().lookupAll(".label").forEach(node -> {
                    if (node instanceof Label && !(node.equals(lblError))) {
                        node.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                    }
                });

                // Now that we have a scene, we can adjust the stage size safely
                adjustStageSize();
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize() {
        try {
            // Get stage
            Stage stage = getStage();
            if (stage != null) {
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
        } catch (Exception e) {
            LoggingManager.logException("Error adjusting stage size", e);
        }
    }

    public void initBookingData(int numberOfGuests, LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
        this.numberOfGuests = numberOfGuests;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomType = roomType;

        LoggingManager.logSystemInfo("Guest details screen initialized with booking data");
    }

    @FXML
    private void handleNextButton() {
        // Validate inputs
        if (validateFields()) {
            return;
        }

        try {
            // Create guest object
            Guest guest = new Guest();
            guest.setName(txtName.getText().trim());
            guest.setPhoneNumber(txtPhone.getText().trim());
            guest.setEmail(txtEmail.getText().trim());
            guest.setAddress(txtAddress.getText().trim());

            // Save guest to database and get guest ID
            int guestId = guestService.saveGuest(guest);

            // Create reservation
            Reservation reservation = new Reservation();
            reservation.setGuestID(guestId);
            reservation.setCheckInDate(checkInDate);
            reservation.setCheckOutDate(checkOutDate);
            reservation.setNumberOfGuests(numberOfGuests);

            // Call createReservation method instead of manually setting status
            // This avoids issues with the setStatus method signature
            reservation.createReservation(
                    guestId,
                    0, // roomID will be assigned in the service
                    checkInDate,
                    checkOutDate,
                    numberOfGuests
            );

            // Save reservation and get reservation ID
            int reservationId = reservationService.createReservation(reservation, roomType);

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

        } catch (Exception e) {
            LoggingManager.logException("Error processing guest details", e);
            DialogService.showError(getStage(), "Processing Error", "Error processing guest details: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            // Load the booking screen again
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_BOOKING));
            Parent bookingRoot = loader.load();

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene bookingScene = new Scene(bookingRoot);
            bookingScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            bookingScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the new scene
            stage.setScene(bookingScene);

            // Use ScreenSizeManager for proper sizing and positioning
            double stageWidth = ScreenSizeManager.calculateStageWidth(900);
            double stageHeight = ScreenSizeManager.calculateStageHeight(700);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            LoggingManager.logSystemInfo("Navigated back to booking screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to booking screen", e);
            DialogService.showError(getStage(), "Navigation Error", "Error returning to booking screen: " + e.getMessage(), e);
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
            if (!ValidationUtils.isNotNullOrEmpty(txtName.getText())) {
                throw new ValidationException("Please enter your name");
            }

            // Validate phone
            if (!ValidationUtils.isNotNullOrEmpty(txtPhone.getText())) {
                throw new ValidationException("Please enter your phone number");
            }
            if (!ValidationUtils.isValidPhoneNumber(txtPhone.getText())) {
                throw new ValidationException("Please enter a valid phone number");
            }

            // Validate email
            if (!ValidationUtils.isNotNullOrEmpty(txtEmail.getText())) {
                throw new ValidationException("Please enter your email");
            }
            if (!ValidationUtils.isValidEmail(txtEmail.getText())) {
                throw new ValidationException("Please enter a valid email address");
            }

            // Validate address
            if (!ValidationUtils.isNotNullOrEmpty(txtAddress.getText())) {
                throw new ValidationException("Please enter your address");
            }

            // All validations passed
            return false;

        } catch (ValidationException e) {
            DialogService.showWarning(getStage(), "Validation Error", e.getMessage());
            return true;
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
}