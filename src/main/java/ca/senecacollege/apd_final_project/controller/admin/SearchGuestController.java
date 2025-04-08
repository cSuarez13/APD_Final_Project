package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.service.ServiceLocator;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class SearchGuestController extends BaseController {

    public Button btnSearch;
    public Button btnCancel;
    @FXML
    private ComboBox<String> cmbSearchBy;

    @FXML
    private TextField txtSearchTerm;

    @FXML
    private TableView<Guest> tblGuests;

    @FXML
    private TableView<Reservation> tblReservations;

    @FXML
    private Button btnViewDetails;

    @FXML
    private Button btnNewReservation;

    @FXML
    private Button btnCheckIn;

    @FXML
    private Button btnCheckOut;

    private GuestService guestService;
    private ReservationService reservationService;
    private RoomService roomService;

    private final ObservableList<Guest> guestList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Initialize services
        guestService = ServiceLocator.getService(GuestService.class);
        reservationService = ServiceLocator.getService(ReservationService.class);
        roomService = ServiceLocator.getService(RoomService.class);

        // Setup search by options
        cmbSearchBy.setItems(FXCollections.observableArrayList("Name", "Phone", "Email"));
        cmbSearchBy.getSelectionModel().selectFirst();

        // Setup guest table
        TableUtils.setupGuestTable(tblGuests, guestList);

        // Setup reservation table
        TableUtils.setupReservationsTable(tblReservations, reservationList, guestService);

        // Add listener to guest table selection to load related reservations
        tblGuests.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadReservationsForGuest(newSelection);
                // Clear reservation selection when a guest is selected
                tblReservations.getSelectionModel().clearSelection();
                updateButtonStates();
            }
        });

        // Add listener to reservation table selection to update button states
        tblReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonStates();

            // If a reservation is selected, make sure its guest is also selected
            if (newSelection != null) {
                for (Guest guest : tblGuests.getItems()) {
                    if (guest.getGuestID() == newSelection.getGuestID()) {
                        // Select the guest without triggering the clear selection logic
                        tblGuests.getSelectionModel().select(guest);
                        break;
                    }
                }
            }
        });

        // Add mouse click listener to guest table to handle unselecting reservation
        tblGuests.setOnMouseClicked(event -> {
            // Clear reservation selection when guest table is clicked
            tblReservations.getSelectionModel().clearSelection();
            updateButtonStates();
        });

        // Initially disable action buttons
        btnViewDetails.setDisable(true);
        btnNewReservation.setDisable(true);
        btnCheckIn.setDisable(true);
        btnCheckOut.setDisable(true);

        LoggingManager.logSystemInfo("SearchGuestController initialized");
    }

    @Override
    public void initData(Admin admin) {
        super.initData(admin);
        LoggingManager.logSystemInfo("SearchGuestController initialized with admin: " + admin.getUsername());
    }

    @FXML
    private void handleSearchAction() {
        // Clear previous results
        guestList.clear();
        reservationList.clear();

        String searchTerm = txtSearchTerm.getText().trim();

        if (searchTerm.isEmpty()) {
            showError("Please enter a search term");
            return;
        }

        try {
            List<Guest> results;
            String searchBy = cmbSearchBy.getValue();

            // Search based on selected criteria
            results = switch (searchBy) {
                case "Name" -> guestService.searchGuestsByName(searchTerm);
                case "Phone" -> guestService.searchGuestsByPhone(searchTerm);
                case "Email" -> guestService.searchGuestsByEmail(searchTerm);
                default -> null;
            };

            // Update the guest list
            if (results != null && !results.isEmpty()) {
                guestList.addAll(results);
                tblGuests.getSelectionModel().selectFirst();
            } else {
                showError("No guests found matching your search criteria");
            }

            // Log the search
            LoggingManager.logSystemInfo("Guest search performed: " + searchBy + " = " + searchTerm +
                    ", found " + (results != null ? results.size() : 0) + " results");

        } catch (DatabaseException e) {
            LoggingManager.logException("Error searching for guests", e);
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error during guest search", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void loadReservationsForGuest(Guest guest) {
        if (guest == null) return;

        try {
            // Clear previous reservation list
            reservationList.clear();

            // Fetch reservations for this guest
            List<Reservation> reservations = reservationService.getReservationsByGuest(guest.getGuestID());

            if (reservations != null && !reservations.isEmpty()) {
                reservationList.addAll(reservations);
                // Don't auto-select any reservation
                // Let the user explicitly choose which one to view
            }

        } catch (DatabaseException e) {
            LoggingManager.logException("Error loading reservations for guest ID " + guest.getGuestID(), e);
            showError("Error loading reservations: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error loading reservations", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void updateButtonStates() {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();
        Reservation selectedReservation = tblReservations.getSelectionModel().getSelectedItem();

        // Enable View Details button if either guest or reservation is selected
        // The handleViewDetailsAction method will determine which to show
        btnViewDetails.setDisable(selectedGuest == null && selectedReservation == null);

        // Enable New Reservation only when a guest is selected
        btnNewReservation.setDisable(selectedGuest == null);

        // Enable Check-in only for reservations with CONFIRMED status
        btnCheckIn.setDisable(selectedReservation == null ||
                !selectedReservation.getStatus().equals(ReservationStatus.CONFIRMED));

        // Enable Check-out only for reservations with CHECKED_IN status
        btnCheckOut.setDisable(selectedReservation == null ||
                !selectedReservation.getStatus().equals(ReservationStatus.CHECKED_IN));

        // Update View Details button text based on selection
        if (selectedReservation != null) {
            btnViewDetails.setText("View Reservation Details");
        } else if (selectedGuest != null) {
            btnViewDetails.setText("View Guest Details");
        } else {
            btnViewDetails.setText("View Details");
        }
    }

    @FXML
    private void handleNewReservationAction() {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();

        if (selectedGuest == null) return;

        try {
            // Load the new reservation screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_NEW_RESERVATION));
            Parent root = loader.load();

            // Get the controller and pass the guest data
            NewReservationController controller = loader.getController();
            controller.initData(currentAdmin);
            controller.initGuest(selectedGuest.getGuestID());

            // Create and configure the stage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Reservation for " + selectedGuest.getName());
            stage.setScene(new Scene(root));

            // Apply CSS
            root.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());

            // Show the dialog
            stage.showAndWait();

            // Refresh reservations after dialog closes
            loadReservationsForGuest(selectedGuest);

            // Log the action
            logAdminActivity("Created new reservation for guest: " + selectedGuest.getName());

        } catch (IOException e) {
            LoggingManager.logException("Error opening new reservation screen", e);
            showError("Error creating new reservation: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error", e);
            showError("Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewDetailsAction() {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();
        Reservation selectedReservation = tblReservations.getSelectionModel().getSelectedItem();

        // Determine which details to show based on what's selected
        if (selectedReservation != null) {
            // Reservation is selected, show reservation details
            showReservationDetails(selectedReservation);
        } else if (selectedGuest != null) {
            // Only guest is selected, show guest details
            showGuestDetails(selectedGuest);
        } else {
            // Nothing selected, show error
            showError("Please select a guest or reservation to view details");
        }
    }

    /**
     * Show guest details in a dialog
     * @param guest The guest to show details for
     */
    private void showGuestDetails(Guest guest) {
        try {
            // Create guest details dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Guest Details");
            dialog.setHeaderText("Details for " + guest.getName());

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setPrefWidth(400);
            dialogPane.setPrefHeight(400);

            // Construct guest details content
            VBox content = createGuestDetailsContent(guest);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());

            dialog.showAndWait();

        } catch (Exception e) {
            LoggingManager.logException("Error showing guest details", e);
            showError("Error showing guest details: " + e.getMessage());
        }
    }

    /**
     * Show reservation details in a dialog
     * @param reservation The reservation to show details for
     */
    private void showReservationDetails(Reservation reservation) {
        try {
            // Create reservation details dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Reservation Details");
            dialog.setHeaderText("Reservation #" + reservation.getReservationID());

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setPrefWidth(500);
            dialogPane.setPrefHeight(500);

            // Construct reservation details content
            VBox content = createReservationDetailsContent(reservation);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());

            dialog.showAndWait();

        } catch (Exception e) {
            LoggingManager.logException("Error showing reservation details", e);
            showError("Error showing reservation details: " + e.getMessage());
        }
    }

    private VBox createGuestDetailsContent(Guest guest) {
        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String labelStyle = "-fx-font-size: 18px;";
        String[] details = {
                "Guest ID: " + guest.getGuestID(),
                "Name: " + guest.getName(),
                "Phone: " + guest.getPhoneNumber(),
                "Email: " + guest.getEmail(),
                "Address: " + guest.getAddress()
        };

        for (String detail : details) {
            Label label = new Label(detail);
            label.setStyle(labelStyle);
            content.getChildren().add(label);
        }

        // Add feedback if available
        if (guest.getFeedback() != null && !guest.getFeedback().isEmpty()) {
            content.getChildren().addAll(
                    new Separator(),
                    createFeedbackSection(guest.getFeedback())
            );
        }

        return content;
    }

    private VBox createReservationDetailsContent(Reservation reservation) {
        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String labelStyle = "-fx-font-size: 16px;";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        try {
            // Get additional information
            Guest guest = guestService.getGuestById(reservation.getGuestID());
            Room room = roomService.getRoomById(reservation.getRoomID());

            // Basic reservation details
            Label idLabel = new Label("Reservation ID: " + reservation.getReservationID());
            Label statusLabel = new Label("Status: " + reservation.getStatusDisplayName());
            Label guestLabel = new Label("Guest: " + (guest != null ? guest.getName() : "Unknown") +
                    " (ID: " + reservation.getGuestID() + ")");

            Label roomLabel = new Label("Room: " + (room != null ?
                    room.getRoomType().getDisplayName() + " (Room #" + room.getRoomID() + ")" :
                    "Room #" + reservation.getRoomID()));

            Label datesLabel = new Label("Dates: " +
                    reservation.getCheckInDate().format(dateFormatter) + " to " +
                    reservation.getCheckOutDate().format(dateFormatter));

            Label nightsLabel = new Label("Number of nights: " + reservation.calculateNumberOfNights());
            Label guestsLabel = new Label("Number of guests: " + reservation.getNumberOfGuests());

            // Apply styling
            idLabel.setStyle(labelStyle);
            statusLabel.setStyle(labelStyle);
            guestLabel.setStyle(labelStyle);
            roomLabel.setStyle(labelStyle);
            datesLabel.setStyle(labelStyle);
            nightsLabel.setStyle(labelStyle);
            guestsLabel.setStyle(labelStyle);

            // Set status label color based on status
            if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                statusLabel.setStyle(labelStyle + "-fx-text-fill: #2e7d32;"); // Green for checked in
            } else if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                statusLabel.setStyle(labelStyle + "-fx-text-fill: #1976d2;"); // Blue for confirmed
            } else if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
                statusLabel.setStyle(labelStyle + "-fx-text-fill: #9e9e9e;"); // Gray for checked out
            } else if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                statusLabel.setStyle(labelStyle + "-fx-text-fill: #d32f2f;"); // Red for cancelled
            }

            // Special note for active reservations
            Label noteLabel = null;
            if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                noteLabel = new Label("Note: Guest is currently checked in");
                noteLabel.setStyle(labelStyle + "-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            }

            // Add all components to the content
            content.getChildren().addAll(
                    idLabel,
                    statusLabel,
                    guestLabel,
                    roomLabel,
                    datesLabel,
                    nightsLabel,
                    guestsLabel
            );

            if (noteLabel != null) {
                content.getChildren().add(new Separator());
                content.getChildren().add(noteLabel);
            }

            // If there's pricing info available, we could add it here

        } catch (Exception e) {
            LoggingManager.logException("Error creating reservation details", e);
            Label errorLabel = new Label("Error loading some reservation details: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            content.getChildren().add(errorLabel);
        }

        return content;
    }

    private VBox createFeedbackSection(String feedbackText) {
        VBox feedbackBox = new VBox(10);

        Label feedbackLabel = new Label("Feedback:");
        feedbackLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextArea feedbackArea = new TextArea(feedbackText);
        feedbackArea.setEditable(false);
        feedbackArea.setPrefRowCount(8);
        feedbackArea.setPrefHeight(120);
        feedbackArea.setPrefWidth(350);
        feedbackArea.setStyle("-fx-font-size: 16px; -fx-control-inner-background: #7b1fa2;");

        feedbackBox.getChildren().addAll(feedbackLabel, feedbackArea);
        return feedbackBox;
    }

    @FXML
    private void handleCheckInAction() {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();
        Reservation selectedReservation = tblReservations.getSelectionModel().getSelectedItem();

        if (selectedGuest == null || selectedReservation == null) return;

        try {
            // Open check-in dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_CHECKIN));
            Parent root = loader.load();

            // Get the controller
            CheckInController controller = loader.getController();
            controller.initData(currentAdmin);

            // Use setReservationId instead of searchReservation
            controller.setReservationId(selectedReservation.getReservationID());

            // Create and configure the stage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Check In");
            stage.setScene(new Scene(root));

            // Apply CSS
            root.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());

            // Show the dialog and wait for it to close
            stage.showAndWait();

            // Refresh reservation data
            loadReservationsForGuest(selectedGuest);

        } catch (IOException e) {
            LoggingManager.logException("Error loading check-in screen", e);
            showError("Error loading check-in screen: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error during check-in", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckOutAction() {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();
        Reservation selectedReservation = tblReservations.getSelectionModel().getSelectedItem();

        if (selectedGuest == null || selectedReservation == null) return;

        try {
            // Open check-out dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_CHECKOUT));
            Parent root = loader.load();

            // Get the controller
            CheckoutController controller = loader.getController();
            controller.initData(currentAdmin);

            // Set reservation ID and trigger search
            controller.setReservationId(selectedReservation.getReservationID());

            // Create and configure the stage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Check Out");
            stage.setScene(new Scene(root));

            // Apply CSS
            root.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_ADMIN)).toExternalForm());

            // Show the dialog and wait for it to close
            stage.showAndWait();

            // Refresh reservation data
            loadReservationsForGuest(selectedGuest);

        } catch (IOException e) {
            LoggingManager.logException("Error loading check-out screen", e);
            showError("Error loading check-out screen: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error during check-out", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelAction() {
        // Close the window
        closeWindow();
    }

    @Override
    protected Stage getStage() {
        return tblGuests != null && tblGuests.getScene() != null
                ? (Stage) tblGuests.getScene().getWindow()
                : null;
    }
}