package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML
    private Label lblTodayCheckIns;

    @FXML
    private Label lblTodayCheckOuts;

    @FXML
    private Label lblActiveReservations;

    @FXML
    private TableView<Reservation> tblTodayCheckIns;

    @FXML
    private TableView<Reservation> tblTodayCheckOuts;

    private GuestService guestService;
    private ReservationService reservationService;

    private ObservableList<Reservation> todayCheckIns = FXCollections.observableArrayList();
    private ObservableList<Reservation> todayCheckOuts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Setup tables
        setupDashboardTables();

        // Refresh dashboard data
        refreshDashboard();

        LoggingManager.logSystemInfo("DashboardContentController initialized");
    }

    /**
     * Setup the dashboard tables
     */
    private void setupDashboardTables() {
        // Setup tables for dashboard
        TableUtils.setupReservationsTable(tblTodayCheckIns, todayCheckIns, guestService);
        TableUtils.setupReservationsTable(tblTodayCheckOuts, todayCheckOuts, guestService);

        // Configure table column widths
        TableUtils.configureTableColumnWidth(tblTodayCheckIns);
        TableUtils.configureTableColumnWidth(tblTodayCheckOuts);
    }

    /**
     * Refresh the dashboard data
     */
    public void refreshDashboard() {
        try {
            // Load today's check-ins
            List<Reservation> checkIns = reservationService.getTodayCheckIns();
            todayCheckIns.clear();
            todayCheckIns.addAll(checkIns);
            lblTodayCheckIns.setText(String.valueOf(checkIns.size()));

            // Load today's check-outs
            List<Reservation> checkOuts = reservationService.getTodayCheckOuts();
            todayCheckOuts.clear();
            todayCheckOuts.addAll(checkOuts);
            lblTodayCheckOuts.setText(String.valueOf(checkOuts.size()));

            // Load active reservations count
            List<Reservation> active = reservationService.getActiveReservations();
            lblActiveReservations.setText(String.valueOf(active.size()));

        } catch (Exception e) {
            LoggingManager.logException("Error refreshing dashboard", e);
        }
    }
}