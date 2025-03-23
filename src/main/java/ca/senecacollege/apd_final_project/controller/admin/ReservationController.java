package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML
    private ComboBox<String> cmbReservationFilter;

    @FXML
    private Button btnCancelReservation;

    @FXML
    private TableView<Reservation> tblAllReservations;

    private Admin currentAdmin;
    private ReservationService reservationService;
    private GuestService guestService;
    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        reservationService = new ReservationService();
        guestService = new GuestService();

        // Initialize reservation filter combo box
        cmbReservationFilter.setItems(FXCollections.observableArrayList(
                "All", "Active", "Confirmed", "Checked In", "Checked Out", "Cancelled"));
        cmbReservationFilter.getSelectionModel().selectFirst();

        // Set up table
        TableUtils.setupReservationsTable(tblAllReservations, allReservations, guestService);
        TableUtils.configureTableColumnWidth(tblAllReservations);

        // Add listener for filter changes
        cmbReservationFilter.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> refreshReservations());

        LoggingManager.logSystemInfo("ReservationController initialized");
    }

    /**
     * Initialize controller with admin data
     */
    public void initData(Admin admin) {
        this.currentAdmin = admin;
        refreshReservations();
        LoggingManager.logSystemInfo("ReservationController initialized with admin: " + admin.getUsername());
    }

    /**
     * Refresh reservations based on filter
     */
    private void refreshReservations() {
        try {
            String filter = cmbReservationFilter.getValue();
            List<Reservation> reservations;

            if ("All".equals(filter)) {
                reservations = reservationService.getActiveReservations(); // Replace with getAll when implemented
            } else if ("Active".equals(filter)) {
                reservations = reservationService.getActiveReservations();
            } else if ("Confirmed".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CONFIRMED);
            } else if ("Checked In".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_IN);
            } else if ("Checked Out".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_OUT);
            } else if ("Cancelled".equals(filter)) {
                reservations = reservationService.getReservationsByStatus(ReservationStatus.CANCELLED);
            } else {
                reservations = reservationService.getActiveReservations(); // Default
            }

            allReservations.clear();
            allReservations.addAll(reservations);

        } catch (Exception e) {
            LoggingManager.logException("Error refreshing reservations", e);
            showAlert(Alert.AlertType.ERROR, "Reservations Error",
                    "Error refreshing reservations: " + e.getMessage());
        }
    }

    /**
     * Handle cancel reservation button click
     */
    @FXML
    private void handleCancelReservation(ActionEvent event) {
        Reservation selectedReservation = tblAllReservations.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a reservation to cancel.");
            return;
        }

        if (selectedReservation.getStatus().equals(ReservationStatus.CHECKED_IN) ||
                selectedReservation.getStatus().equals(ReservationStatus.CHECKED_OUT) ||
                selectedReservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            showAlert(Alert.AlertType.WARNING, "Cannot Cancel",
                    "This reservation cannot be cancelled. Current status: " +
                            selectedReservation.getStatus());
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Cancellation");
        confirmDialog.setHeaderText("Cancel Reservation #" + selectedReservation.getReservationID());
        confirmDialog.setContentText("Are you sure you want to cancel this reservation?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationService.cancelReservation(selectedReservation.getReservationID());
                    LoggingManager.logAdminActivity(currentAdmin.getUsername(),
                            "Cancelled reservation #" + selectedReservation.getReservationID());

                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Reservation cancelled successfully.");

                    refreshReservations();

                } catch (Exception e) {
                    LoggingManager.logException("Error cancelling reservation", e);
                    showAlert(Alert.AlertType.ERROR, "Cancellation Error",
                            "Error cancelling reservation: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Helper method to show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

        alert.showAndWait();
    }
}