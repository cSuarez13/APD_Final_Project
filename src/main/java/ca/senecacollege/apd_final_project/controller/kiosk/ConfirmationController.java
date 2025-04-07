package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.controller.kiosk.BookingData;
import ca.senecacollege.apd_final_project.model.*;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
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

    // Container for room details
    @FXML
    private VBox roomDetailsContainer;
    @FXML
    private GridPane roomGrid;

    private int reservationId;
    private Reservation reservation;
    private Guest guest;
    private List<Room> rooms;
    private List<ReservationRoom> reservationRooms;
    private BookingData bookingData;

    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;
    private BillingService billingService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        reservationService = ServiceLocator.getService(ReservationService.class);
        guestService = ServiceLocator.getService(GuestService.class);
        roomService = ServiceLocator.getService(RoomService.class);
        billingService = ServiceLocator.getService(BillingService.class);

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

        lblReservationId.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
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
                // Wait for the window to be available
                newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                    if (newWindow instanceof Stage stage) {

                        // Calculate dimensions based on screen dimensions
                        double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
                        double stageHeight = ScreenSizeManager.calculateStageHeight(800); // Increased height for multiple rooms

                        // Ensure minimum size for content
                        stageWidth = Math.max(stageWidth, 800);
                        stageHeight = Math.max(stageHeight, 700);

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
        });
    }

    public void initReservationData(int reservationId, BookingData bookingData)
    {
        this.reservationId = reservationId;
        this.bookingData = bookingData;
        try {
            // Load reservation details
            reservation = reservationService.getReservationById(reservationId);

            // Load guest details
            guest = guestService.getGuestById(reservation.getGuestID());

            // Load all rooms for this reservation
            rooms = reservationService.getRoomsForReservation(reservationId);

            // Load reservation-room relationships for guest assignments
            reservationRooms = reservationService.getReservationRooms(reservationId);

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

        // Room info is now handled in displayRoomDetails()
        displayRoomDetails();

        lblCheckIn.setText(reservation.getCheckInDate().format(dateFormatter));
        lblCheckOut.setText(reservation.getCheckOutDate().format(dateFormatter));

        // Calculate number of nights
        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        lblNights.setText(String.valueOf(nights));

        // Show number of guests
        lblGuests.setText(String.valueOf(reservation.getNumberOfGuests()));

        // Calculate and show pricing
        calculateBill();
    }

    /**
     * Display details for all rooms in the reservation
     */
    private void displayRoomDetails() {
        if (rooms.isEmpty()) {
            lblRoomInfo.setText("No rooms assigned");
            return;
        }

        System.out.println("Rooms:");
        for (Room r : rooms) {
            System.out.println("Room ID: " + r.getRoomID() + ", Type: " + r.getRoomType().getDisplayName());
        }

        // Group rooms by room type to count duplicates and display properly
        Map<RoomType, List<Room>> roomsByType = rooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomType));

        // Create a summary for the main room info label
        StringBuilder roomSummary = new StringBuilder();
        List<String> roomDetailsList = new ArrayList<>();

        boolean firstRoom = true;
        for (Map.Entry<RoomType, List<Room>> entry : roomsByType.entrySet()) {
            RoomType roomType = entry.getKey();
            List<Room> roomsOfType = entry.getValue();
            int count = roomsOfType.size();

            // Add to summary
            if (!firstRoom) {
                roomSummary.append(", ");
            }

            if (count == 1) {
                Room room = roomsOfType.get(0);
                roomSummary.append(roomType.getDisplayName())
                        .append(" (Room #")
                        .append(room.getRoomID())
                        .append(")");
            } else {
                roomSummary.append(count)
                        .append(" ")
                        .append(roomType.getDisplayName())
                        .append("s");

                // Add individual room numbers
                for (Room room : roomsOfType) {
                    roomDetailsList.add(roomType.getDisplayName() + " (Room #" + room.getRoomID() + ")");
                }
            }

            firstRoom = false;
        }

        // Set the main room info label
        lblRoomInfo.setText(roomSummary.toString());

        // Create detailed rows for each room if we have a detailed view container
        if (roomDetailsContainer != null && !roomDetailsList.isEmpty()) {
            roomDetailsContainer.getChildren().clear();

            Label titleLabel = new Label("Room Details:");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;");
            titleLabel.setPadding(new Insets(10, 0, 5, 0));
            roomDetailsContainer.getChildren().add(titleLabel);

            // Create a grid for room details
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(5);
            grid.setPadding(new Insets(5, 0, 0, 15));

            // Headers
            Label roomHeader = new Label("Room #");
            Label typeHeader = new Label("Type");
            Label guestsHeader = new Label("Guests");
            Label priceHeader = new Label("Price/Night");

            roomHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            typeHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            guestsHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            priceHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

            grid.add(roomHeader, 0, 0);
            grid.add(typeHeader, 1, 0);
            grid.add(guestsHeader, 2, 0);
            grid.add(priceHeader, 3, 0);

            // Add each room
            int row = 1;
            for (Room room : rooms) {
                Label roomNumber = new Label(String.valueOf(room.getRoomID()));
                Label roomType = new Label(room.getRoomType().getDisplayName());

                // Find guest count for this room
                int guestCount = 0;
                for (ReservationRoom rr : reservationRooms) {
                    if (rr.getRoomID() == room.getRoomID()) {
                        guestCount = rr.getGuestsInRoom();
                        break;
                    }
                }

                Label guests = new Label(String.valueOf(guestCount));
                Label price = new Label(String.format("$%.2f", room.getPrice()));

                roomNumber.setStyle("-fx-text-fill: white;");
                roomType.setStyle("-fx-text-fill: white;");
                guests.setStyle("-fx-text-fill: white;");
                price.setStyle("-fx-text-fill: white;");

                grid.add(roomNumber, 0, row);
                grid.add(roomType, 1, row);
                grid.add(guests, 2, row);
                grid.add(price, 3, row);

                row++;
            }

            roomDetailsContainer.getChildren().add(grid);
        }
    }

    /**
     * Calculate the subtotal manually based on room prices
     */
    private double calculateSubtotal() {
        if (rooms.isEmpty()) {
            return 0;
        }

        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());

        double subtotal = 0;
        for (Room room : rooms) {
            subtotal += room.getPrice() * nights;
        }

        return subtotal;
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
            // Load the welcome screen
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
     * Calculate the bill and update the UI
     */
    private void calculateBill() {
        if (reservation == null || rooms.isEmpty()) {
            return;
        }

        try {
            // Use the BillingService to calculate the bill
            var bill = billingService.calculateBill(reservationId);

            // Update UI with calculated values
            double subtotal = bill.getAmount();
            double tax = bill.getTax();
            double total = bill.getTotalAmount();

            lblSubtotal.setText(String.format("$%.2f", subtotal));
            lblTax.setText(String.format("$%.2f", tax));
            lblTotal.setText(String.format("$%.2f", total));
        } catch (Exception e) {
            LoggingManager.logException("Error calculating bill", e);

            // Fallback calculation if billing service fails
            double subtotal = calculateSubtotal();
            double tax = subtotal * Constants.TAX_RATE;
            double total = subtotal + tax;

            lblSubtotal.setText(String.format("$%.2f", subtotal));
            lblTax.setText(String.format("$%.2f", tax));
            lblTotal.setText(String.format("$%.2f", total));
        }
    }

    /**
     * Log a system activity
     */
    private void logSystemActivity(String activity) {
        logAdminActivity(activity);
    }
}