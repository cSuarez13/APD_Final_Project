package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardContentController extends BaseController {

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

    private final ObservableList<Reservation> todayCheckIns = FXCollections.observableArrayList();
    private final ObservableList<Reservation> todayCheckOuts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Initialize services
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Setup tables
        setupDashboardTables();

        // Refresh dashboard data
        refreshDashboard();

        LoggingManager.logSystemInfo("DashboardContentController initialized");
    }

    @Override
    public void initData(Admin admin) {
        super.initData(admin);
    }

    /**
     * Set up the dashboard tables
     */
    private void setupDashboardTables() {
        // Setup tables for dashboard
        tblTodayCheckIns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblTodayCheckOuts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableUtils.setupReservationsTable(tblTodayCheckIns, todayCheckIns, guestService);
        TableUtils.setupReservationsTable(tblTodayCheckOuts, todayCheckOuts, guestService);
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

        } catch (DatabaseException e) {
            LoggingManager.logException("Error refreshing dashboard", e);
            showError("Error loading dashboard data: " + e.getMessage());
        }
    }

    @Override
    protected Stage getStage() {
        return tblTodayCheckIns != null && tblTodayCheckIns.getScene() != null
                ? (Stage) tblTodayCheckIns.getScene().getWindow()
                : null;
    }
}