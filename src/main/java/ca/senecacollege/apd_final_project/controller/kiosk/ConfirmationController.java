package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ConfirmationController implements Initializable {

    @FXML
    private Label lblReservationId;

    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblRoomInfo;

    @FXML
    private Label lblCheckIn;

    @FXML
    private Label lblCheckOut;

    @FXML
    private Label lblNights;

    @FXML
    private Label lblGuests;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblTax;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnPrint;

    @FXML
    private Button btnDone;

    private int reservationId;
    private Reservation reservation;
    private Guest guest;
    private Room room;

    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reservationService = new ReservationService();
        guestService = new GuestService();
        roomService = new RoomService();

        // Apply styles to ensure text is visible
        applyStyles();

        // Adjust window size
        adjustStageSize();

        LoggingManager.logSystemInfo("ConfirmationController initialized");
    }

    /**
     * Apply styles to ensure text elements are properly colored
     */
    private void applyStyles() {
        // Apply white text color to all labels
        lblReservationId.setStyle("-fx-text-fill: white;");
        lblGuestName.setStyle("-fx-text-fill: white;");
        lblRoomInfo.setStyle("-fx-text-fill: white;");
        lblCheckIn.setStyle("-fx-text-fill: white;");
        lblCheckOut.setStyle("-fx-text-fill: white;");
        lblNights.setStyle("-fx-text-fill: white;");
        lblGuests.setStyle("-fx-text-fill: white;");
        lblSubtotal.setStyle("-fx-text-fill: white;");
        lblTax.setStyle("-fx-text-fill: white;");
        lblTotal.setStyle("-fx-text-fill: #b491c8; -fx-font-weight: bold;"); // Highlight the total

        // Apply styles to scene labels once the scene is available
        lblReservationId.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Find all labels in the scene and ensure white text
                newScene.getRoot().lookupAll(".label").forEach(node -> {
                    if (node instanceof Label && !((Label) node).getStyleClass().contains("label-header")
                            && !((Label) node).getStyleClass().contains("label-total")) {
                        ((Label) node).setStyle("-fx-text-fill: white;");
                    }
                });
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize() {
        // Add listener to wait for scene to be available
        lblReservationId.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();

                // Get screen dimensions
                Rectangle2D screenBounds = ScreenSizeManager.getPrimaryScreenBounds();

                // Calculate dimensions - use at most 90% of screen height to ensure full content is visible
                // and maintain aspect ratio
                double maxHeight = screenBounds.getHeight() * 0.9;
                double maxWidth = screenBounds.getWidth() * 0.9;

                // Choose smaller dimension to ensure fitting on screen
                double aspectRatio = 1024.0 / 650.0; // Adjusted aspect ratio for even more height
                double stageHeight = Math.min(maxHeight, 750); // Increased height
                double stageWidth = Math.min(maxWidth, stageHeight * aspectRatio);

                // Ensure minimum size for content
                stageWidth = Math.max(stageWidth, 800);
                stageHeight = Math.max(stageHeight, 600);

                // Get center position
                double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

                // Set the stage's size and position
                stage.setWidth(stageWidth);
                stage.setHeight(stageHeight);
                stage.setX(centerPos[0]);
                stage.setY(centerPos[1]);

                // Make sure it's not maximized
                stage.setMaximized(false);

                LoggingManager.logSystemInfo("ConfirmationScreen size adjusted to " + stageWidth + "x" + stageHeight);
            }
        });
    }

    public void initReservationData(int reservationId) {
        this.reservationId = reservationId;

        try {
            // Load reservation details
            reservation = reservationService.getReservationById(reservationId);

            // Load guest details
            guest = guestService.getGuestById(reservation.getGuestID());

            // Load room details
            room = roomService.getRoomById(reservation.getRoomID());

            // Update UI with reservation details
            updateConfirmationDetails();

            LoggingManager.logSystemInfo("Confirmation screen loaded with reservation ID: " + reservationId);

        } catch (Exception e) {
            LoggingManager.logException("Error loading reservation details", e);

            // Use ErrorPopupManager instead of direct error handling
            Stage stage = (Stage) lblReservationId.getScene().getWindow();
            if (stage != null) {
                ErrorPopupManager.showSystemErrorPopup(stage, "CONF-001",
                        "Error loading reservation details: " + e.getMessage());
            }
        }
    }

    private void updateConfirmationDetails() {
        // Format the reservation details for display
        lblReservationId.setText("Reservation #" + reservationId);
        lblGuestName.setText(guest.getName());
        lblRoomInfo.setText(room.getRoomType().getDisplayName() + " (Room #" + room.getRoomID() + ")");
        lblCheckIn.setText(reservation.getCheckInDate().format(dateFormatter));
        lblCheckOut.setText(reservation.getCheckOutDate().format(dateFormatter));

        // Calculate number of nights
        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        lblNights.setText(String.valueOf(nights));

        // Show number of guests
        lblGuests.setText(String.valueOf(reservation.getNumberOfGuests()));

        // Calculate and show pricing
        double subtotal = room.getPrice() * nights;
        double tax = subtotal * Constants.TAX_RATE;
        double total = subtotal + tax;

        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblTax.setText(String.format("$%.2f", tax));
        lblTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handlePrintButton(ActionEvent event) {
        // Get the current stage for error popup
        Stage stage = (Stage) btnPrint.getScene().getWindow();

        try {
            // In a real system, this would send the confirmation to a printer
            LoggingManager.logSystemInfo("Print confirmation requested for reservation ID: " + reservationId);

            // Apply CSS to the dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Print Confirmation");
            alert.setHeaderText("Printing Confirmation");
            alert.setContentText("Your booking confirmation has been sent to the printer.\n" +
                    "Please collect it from the front desk.");

            // Apply CSS to the dialog
            javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());
            dialogPane.getStyleClass().add("root");

            // Explicitly style the header area and text to match dark theme
            Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
            if (headerLabel != null) {
                headerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }

            // Set the dialog background to dark
            dialogPane.setStyle("-fx-background-color: #2a2a2a;");

            // Make sure the header panel background is also dark
            javafx.scene.layout.Region headerPanel = (javafx.scene.layout.Region) dialogPane.lookup(".header-panel");
            if (headerPanel != null) {
                headerPanel.setStyle("-fx-background-color: #2a2a2a;");
            }

            // Set content text to white
            Label contentLabel = (Label) dialogPane.lookup(".content.label");
            if (contentLabel != null) {
                contentLabel.setStyle("-fx-text-fill: white;");
            }

            // Customize the buttons
            Button okButton = (Button) dialogPane.lookupButton(javafx.scene.control.ButtonType.OK);
            if (okButton != null) {
                okButton.setStyle("-fx-background-color: #7b1fa2; -fx-text-fill: white;");
            }

            alert.showAndWait();
        } catch (Exception e) {
            LoggingManager.logException("Error printing confirmation", e);
            ErrorPopupManager.showSystemErrorPopup(stage, "PRINT-001",
                    "Unable to print confirmation: " + e.getMessage());
        }
    }

    @FXML
    private void handleDoneButton(ActionEvent event) {
        try {
            // Load the welcome screen to restart the process
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent welcomeRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) btnDone.getScene().getWindow();

            // Create new scene
            Scene welcomeScene = new Scene(welcomeRoot);
            welcomeScene.getStylesheets().add(getClass().getResource(Constants.CSS_MAIN).toExternalForm());
            welcomeScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the scene dimensions using ScreenSizeManager
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);

            // Get center position
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            // Apply size and position
            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            // Set the new scene
            stage.setScene(welcomeScene);

            LoggingManager.logSystemInfo("Returned to welcome screen after completing reservation");

        } catch (IOException e) {
            LoggingManager.logException("Error returning to welcome screen", e);

            // Use ErrorPopupManager
            Stage stage = (Stage) btnDone.getScene().getWindow();
            ErrorPopupManager.showSystemErrorPopup(stage, "NAV-003",
                    "Error returning to welcome screen: " + e.getMessage());
        }
    }
}