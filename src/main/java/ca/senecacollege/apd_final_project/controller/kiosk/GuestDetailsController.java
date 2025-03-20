package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class GuestDetailsController implements Initializable {

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
    private Button btnBack;

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
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Hide error label initially
        lblError.setVisible(false);

        // Apply proper text styling to ensure visibility
        applyStyles();

        LoggingManager.logSystemInfo("GuestDetailsController initialized");
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
                        ((Label) node).setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                    }
                });

                // Now that we have a scene, we can adjust the stage size safely
                adjustStageSize(newScene);
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize(Scene scene) {
        try {
            // Get stage from the scene
            Stage stage = (Stage) scene.getWindow();
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
    private void handleNextButton(ActionEvent event) {
        // Validate inputs
        if (!validateInputs()) {
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
            reservation.setStatus(Reservation.STATUS_PENDING);

            // Save reservation and get reservation ID
            int reservationId = reservationService.createReservation(reservation, roomType);

            // Load the confirmation screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_CONFIRMATION));
            Parent confirmationRoot = loader.load();

            // Get the controller and pass the reservation data
            ConfirmationController confirmationController = loader.getController();
            confirmationController.initReservationData(reservationId);

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create new scene
            Scene confirmationScene = new Scene(confirmationRoot);
            confirmationScene.getStylesheets().add(getClass().getResource(Constants.CSS_MAIN).toExternalForm());
            confirmationScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

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

            // Use ErrorPopupManager instead of directly showing error on lblError
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "GUEST-001", "Error processing guest details");
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            // Load the booking screen again
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_BOOKING));
            Parent bookingRoot = loader.load();

            // Get the controller
            BookingScreenController bookingController = loader.getController();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create new scene
            Scene bookingScene = new Scene(bookingRoot);
            bookingScene.getStylesheets().add(getClass().getResource(Constants.CSS_MAIN).toExternalForm());
            bookingScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

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

            // Use ErrorPopupManager
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "NAV-002", "Error returning to booking screen");
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        RulesDialogUtility.showRulesDialog(btnRules);
    }

    private boolean validateInputs() {
        // Get the current stage for error popups
        Stage stage = (Stage) btnNext.getScene().getWindow();

        // Validate name
        if (!ValidationUtils.isNotNullOrEmpty(txtName.getText())) {
            ErrorPopupManager.showValidationErrorPopup(stage, "Name", "Please enter your name");
            return false;
        }

        // Validate phone
        if (!ValidationUtils.isNotNullOrEmpty(txtPhone.getText())) {
            ErrorPopupManager.showValidationErrorPopup(stage, "Phone", "Please enter your phone number");
            return false;
        }

        if (!ValidationUtils.isValidPhoneNumber(txtPhone.getText())) {
            ErrorPopupManager.showValidationErrorPopup(stage, "Phone", "Please enter a valid phone number");
            return false;
        }

        // Validate email
        if (!ValidationUtils.isNotNullOrEmpty(txtEmail.getText())) {
            ErrorPopupManager.showValidationErrorPopup(stage, "Email", "Please enter your email");
            return false;
        }

        if (!ValidationUtils.isValidEmail(txtEmail.getText())) {
            ErrorPopupManager.showValidationErrorPopup(stage, "Email", "Please enter a valid email address");
            return false;
        }

        // Validate address
        if (!ValidationUtils.isNotNullOrEmpty(txtAddress.getText())) {
            ErrorPopupManager.showValidationErrorPopup(stage, "Address", "Please enter your address");
            return false;
        }

        // All validations passed
        return true;
    }

    // Helper method that's no longer needed as we use ErrorPopupManager now
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}