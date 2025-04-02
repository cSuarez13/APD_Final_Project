package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.ErrorPopupManager;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.util.List;
import java.util.ResourceBundle;

public class SearchGuestController extends BaseController {

    @FXML
    private ComboBox<String> cmbSearchBy;

    @FXML
    private TextField txtSearchTerm;

    @FXML
    private Button btnSearch;

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

    @FXML
    private Button btnCancel;

    private GuestService guestService;
    private ReservationService reservationService;

    private ObservableList<Guest> guestList = FXCollections.observableArrayList();
    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Initialize services
        guestService = new GuestService();
        reservationService = new ReservationService();

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
                updateButtonStates();
            }
        });

        // Add listener to reservation table selection to update button states
        tblReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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
    private void handleSearchAction(ActionEvent event) {
        // Clear previous results
        guestList.clear();
        reservationList.clear();

        String searchTerm = txtSearchTerm.getText().trim();

        if (searchTerm.isEmpty()) {
            showError("Please enter a search term");
            return;
        }

        try {
            List<Guest> results = null;
            String searchBy = cmbSearchBy.getValue();

            // Search based on selected criteria
            switch (searchBy) {
                case "Name":
                    results = guestService.searchGuestsByName(searchTerm);
                    break;
                case "Phone":
                    results = guestService.searchGuestsByPhone(searchTerm);
                    break;
                case "Email":
                    results = guestService.searchGuestsByEmail(searchTerm);
                    break;
            }

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
                tblReservations.getSelectionModel().selectFirst();
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

        // Enable/disable buttons based on selections
        btnViewDetails.setDisable(selectedGuest == null);
        btnNewReservation.setDisable(selectedGuest == null);
        btnCheckIn.setDisable(selectedReservation == null ||
                !selectedReservation.getStatus().equals(ReservationStatus.CONFIRMED));
        btnCheckOut.setDisable(selectedReservation == null ||
                !selectedReservation.getStatus().equals(ReservationStatus.CHECKED_IN));
    }

    @FXML
    private void handleNewReservationAction(ActionEvent event) {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();

        if (selectedGuest == null) return;

        try {
            // Open reservation screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_BOOKING));
            Parent root = loader.load();

            // Create and configure the stage
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Reservation for " + selectedGuest.getName());
            stage.setScene(new Scene(root));

            // Apply CSS
            root.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            // Set a larger stage size
            stage.setWidth(800);
            stage.setHeight(600);

            // Show the dialog
            stage.showAndWait();

            // Log the action
            logAdminActivity("Opened new reservation for guest: " + selectedGuest.getName());

        } catch (IOException e) {
            LoggingManager.logException("Error opening new reservation screen", e);
            showError("Error creating new reservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewDetailsAction(ActionEvent event) {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();
        if (selectedGuest == null) return;

        try {
            // Create guest details dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Guest Details");
            dialog.setHeaderText("Details for " + selectedGuest.getName());

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setPrefWidth(400);
            dialogPane.setPrefHeight(400);

            // Construct guest details content
            VBox content = createGuestDetailsContent(selectedGuest);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            dialog.showAndWait();

        } catch (Exception e) {
            LoggingManager.logException("Error showing guest details", e);
            showError("Error showing guest details: " + e.getMessage());
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
    private void handleCheckInAction(ActionEvent event) {
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
            root.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

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
    private void handleCheckOutAction(ActionEvent event) {
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
            root.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

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
    private void handleCancelAction(ActionEvent event) {
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