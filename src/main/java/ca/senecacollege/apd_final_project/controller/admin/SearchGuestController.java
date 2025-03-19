package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;

public class SearchGuestController implements Initializable {

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

    @FXML
    private Label lblError;

    private GuestService guestService;
    private ReservationService reservationService;

    private ObservableList<Guest> guestList = FXCollections.observableArrayList();
    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Setup search by options
        cmbSearchBy.setItems(FXCollections.observableArrayList("Name", "Phone", "Email"));
        cmbSearchBy.getSelectionModel().selectFirst();

        // Setup guest table columns
        setupGuestTable();

        // Setup reservation table columns
        setupReservationTable();

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

        // Hide error message initially
        lblError.setVisible(false);

        LoggingManager.logSystemInfo("SearchGuestController initialized");
    }

    /**
     * Setup the guest table columns
     */
    private void setupGuestTable() {
        // Create columns
        TableColumn<Guest, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("guestID"));

        TableColumn<Guest, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Guest, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Guest, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Configure column widths
        idCol.setPrefWidth(50);
        nameCol.setPrefWidth(200);
        phoneCol.setPrefWidth(150);
        emailCol.setPrefWidth(200);

        // Add columns to table
        tblGuests.getColumns().addAll(idCol, nameCol, phoneCol, emailCol);

        // Set items list
        tblGuests.setItems(guestList);
    }

    /**
     * Setup the reservation table columns
     */
    private void setupReservationTable() {
        // Create columns
        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationID"));

        TableColumn<Reservation, String> checkInCol = new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCheckInDate().format(dateFormatter)));

        TableColumn<Reservation, String> checkOutCol = new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCheckOutDate().format(dateFormatter)));

        TableColumn<Reservation, Integer> guestsCol = new TableColumn<>("Guests");
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Configure column widths
        idCol.setPrefWidth(50);
        checkInCol.setPrefWidth(100);
        checkOutCol.setPrefWidth(100);
        guestsCol.setPrefWidth(70);
        statusCol.setPrefWidth(100);

        // Add columns to table
        tblReservations.getColumns().addAll(idCol, checkInCol, checkOutCol, guestsCol, statusCol);

        // Set items list
        tblReservations.setItems(reservationList);
    }

    /**
     * Handle search button action
     */
    @FXML
    private void handleSearchAction(ActionEvent event) {
        // Clear previous results and error message
        guestList.clear();
        reservationList.clear();
        lblError.setVisible(false);

        String searchTerm = txtSearchTerm.getText().trim();

        if (searchTerm.isEmpty()) {
            showError("Please enter a search term");
            return;
        }

        try {
            List<Guest> results = null;
            String searchBy = cmbSearchBy.getValue();

            // Search based on selected criteria
            if (searchBy.equals("Name")) {
                results = guestService.searchGuestsByName(searchTerm);
            } else if (searchBy.equals("Phone")) {
                results = guestService.searchGuestsByPhone(searchTerm);
            } else if (searchBy.equals("Email")) {
                results = guestService.searchGuestsByEmail(searchTerm);
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

    /**
     * Load reservations for the selected guest
     *
     * @param guest The selected guest
     */
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

    /**
     * Update the states of the action buttons based on selections
     */
    private void updateButtonStates() {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();
        Reservation selectedReservation = tblReservations.getSelectionModel().getSelectedItem();

        // Enable/disable view details button for guest
        btnViewDetails.setDisable(selectedGuest == null);

        // Enable/disable new reservation button for guest
        btnNewReservation.setDisable(selectedGuest == null);

        // Only enable check-in button if reservation is in confirmed status
        btnCheckIn.setDisable(selectedReservation == null ||
                !selectedReservation.getStatus().equals(Reservation.STATUS_CONFIRMED));

        // Only enable check-out button if reservation is in checked-in status
        btnCheckOut.setDisable(selectedReservation == null ||
                !selectedReservation.getStatus().equals(Reservation.STATUS_CHECKED_IN));
    }

    /**
     * Handle view guest details button action
     */
    @FXML
    private void handleViewDetailsAction(ActionEvent event) {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();

        if (selectedGuest == null) return;

        try {
            // Create a dialog to show guest details
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Guest Details");
            dialog.setHeaderText("Details for " + selectedGuest.getName());

            // Add guest information
            VBox content = new VBox(10);
            content.getChildren().addAll(
                    new Label("Guest ID: " + selectedGuest.getGuestID()),
                    new Label("Name: " + selectedGuest.getName()),
                    new Label("Phone: " + selectedGuest.getPhoneNumber()),
                    new Label("Email: " + selectedGuest.getEmail()),
                    new Label("Address: " + selectedGuest.getAddress())
            );

            // Add feedback if available
            if (selectedGuest.getFeedback() != null && !selectedGuest.getFeedback().isEmpty()) {
                content.getChildren().add(new Separator());
                content.getChildren().add(new Label("Feedback:"));
                TextArea feedbackArea = new TextArea(selectedGuest.getFeedback());
                feedbackArea.setEditable(false);
                feedbackArea.setPrefRowCount(4);
                content.getChildren().add(feedbackArea);
            }

            dialog.getDialogPane().setContent(content);

            // Add close button
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Apply CSS to the dialog
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            // Show the dialog
            dialog.showAndWait();

        } catch (Exception e) {
            LoggingManager.logException("Error showing guest details", e);
            showError("Error showing guest details: " + e.getMessage());
        }
    }

    /**
     * Handle new reservation button action
     */
    @FXML
    private void handleNewReservationAction(ActionEvent event) {
        Guest selectedGuest = tblGuests.getSelectionModel().getSelectedItem();

        if (selectedGuest == null) return;

        try {
            // This would normally open a new reservation screen
            // For the demo, just show a message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("New Reservation");
            alert.setHeaderText("Create New Reservation");
            alert.setContentText("This would open a new reservation form for " + selectedGuest.getName() +
                    ". Implementation of the new reservation form is outside the scope of this controller.");

            // Apply CSS to the dialog
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

            alert.showAndWait();

        } catch (Exception e) {
            LoggingManager.logException("Error creating new reservation", e);
            showError("Error creating new reservation: " + e.getMessage());
        }
    }

    /**
     * Handle check-in button action
     */
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
            controller.initData(selectedReservation.getReservationID());

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

    /**
     * Handle check-out button action
     */
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

            // Here we'd normally initialize the controller with the reservation ID
            // We simulate the initialization by setting the reservation ID in the text field
            // This is not ideal but it's a simple way to demo the interaction
            TextField txtReservationId = (TextField) root.lookup("#txtReservationId");
            if (txtReservationId != null) {
                txtReservationId.setText(String.valueOf(selectedReservation.getReservationID()));

                // Trigger the search button
                Button btnSearch = (Button) root.lookup("#btnSearch");
                if (btnSearch != null) {
                    btnSearch.fire();
                }
            }

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

    /**
     * Handle cancel button action
     */
    @FXML
    private void handleCancelAction(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Show an error message
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}