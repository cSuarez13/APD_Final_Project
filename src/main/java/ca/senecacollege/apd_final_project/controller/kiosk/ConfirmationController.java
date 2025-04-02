package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.ResourceBundle;

public class ConfirmationController extends BaseController {

    public Button btnPrint;
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
    private Button btnDone;

    private int reservationId;
    private Reservation reservation;
    private Guest guest;
    private Room room;

    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        reservationService = ServiceLocator.getService(ReservationService.class);
        guestService = ServiceLocator.getService(GuestService.class);
        roomService = ServiceLocator.getService(RoomService.class);

        // Apply styles to ensure text is visible
        applyStyles();

        // Adjust window size
        adjustStageSize();

        // Call parent initialize
        super.initialize(url, resourceBundle);
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
                    if (node instanceof Label && !node.getStyleClass().contains("label-header")
                            && !node.getStyleClass().contains("label-total")) {
                        node.setStyle("-fx-text-fill: white;");
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

                // Calculate dimensions - use at most 90% of screen height to ensure full content is visible
                // and maintain aspect ratio
                double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
                double stageHeight = ScreenSizeManager.calculateStageHeight(750); // Increased height

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

                logSystemActivity("ConfirmationScreen size adjusted to " + stageWidth + "x" + stageHeight);
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

            logSystemActivity("Confirmation screen loaded with reservation ID: " + reservationId);

        } catch (Exception e) {
            logSystemActivity("Error loading reservation details: " + e.getMessage());
            DialogService.showError(getStage(), "Confirmation Error",
                    "Error loading reservation details: " + e.getMessage(), e);
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
    private void handlePrintButton() {
        try {
            logSystemActivity("Print confirmation requested for reservation ID: " + reservationId);

            DialogService.showInformation(getStage(), "Print Confirmation",
                    "Your booking confirmation has been sent to the printer.\n" +
                            "Please collect it from the front desk.");

        } catch (Exception e) {
            DialogService.showError(getStage(), "Print Error",
                    "Unable to print confirmation: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleDoneButton() {
        try {
            // Load the welcome screen to restart the process
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent welcomeRoot = loader.load();

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene welcomeScene = new Scene(welcomeRoot);
            welcomeScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            welcomeScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

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

            logSystemActivity("Returned to welcome screen after completing reservation");

        } catch (IOException e) {
            DialogService.showError(getStage(), "Navigation Error",
                    "Error returning to welcome screen: " + e.getMessage(), e);
        }
    }

    /**
     * Get the current stage
     */
    @Override
    protected Stage getStage() {
        if (btnDone != null && btnDone.getScene() != null) {
            return (Stage) btnDone.getScene().getWindow();
        }
        return null;
    }

    /**
     * Log a system activity
     */
    private void logSystemActivity(String activity) {
        logAdminActivity(activity);
    }
}