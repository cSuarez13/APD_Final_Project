package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
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

        // Make sure the current stage has proper size and position
        adjustStageSize();
    }

    /**
     * Apply styles to ensure text is visible
     */
    private void applyStyles() {
        // Set explicit styling for text fields to ensure text is visible
        String textFieldStyle = "-fx-text-fill: black; -fx-font-size: 14px;";
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
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize() {
        // Get the current scene and stage, but wait until they're available
        // This will be called when the scene exists
        txtName.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
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
            Stage stage = (Stage) btnNext.getScene().getWindow();

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
            Stage stage = (Stage) btnNext.getScene().getWindow();
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
            Stage stage = (Stage) btnBack.getScene().getWindow();

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
            Stage stage = (Stage) btnBack.getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "NAV-002", "Error returning to booking screen");
        }
    }

    @FXML
    private void handleRulesButton(ActionEvent event) {
        // Show rules and regulations dialog
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Rules & Regulations");
        alert.setHeaderText("Please Read Our Rules & Regulations");

        // Create text area for rules content
        TextArea textArea = new TextArea(Constants.RULES_REGULATIONS);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-fill: white;");

        // Use ScreenSizeManager to set appropriate dialog size
        Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();
        double dialogWidth = Math.min(800, screenBounds.getWidth() * 0.7);
        double dialogHeight = Math.min(500, screenBounds.getHeight() * 0.6);

        textArea.setPrefWidth(dialogWidth);
        textArea.setPrefHeight(dialogHeight);

        alert.getDialogPane().setContent(textArea);

        // Apply CSS to the dialog
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
        dialogPane.getStyleClass().add("root");
        dialogPane.setStyle("-fx-background-color: #2a2a2a;");

        // Explicitly set header text color
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-text-fill: #b491c8; -fx-font-weight: bold;");
        }

        alert.showAndWait();
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