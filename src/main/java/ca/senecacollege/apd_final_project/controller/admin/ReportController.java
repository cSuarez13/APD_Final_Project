package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Billing;
import ca.senecacollege.apd_final_project.model.Feedback;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.BillingService;
import ca.senecacollege.apd_final_project.service.FeedbackService;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReportController implements Initializable {

    @FXML
    private ComboBox<String> cmbReportType;

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private Button btnGenerateReport;

    @FXML
    private Button btnExportReport;

    @FXML
    private Button btnClose;

    @FXML
    private TabPane tabPaneReports;

    @FXML
    private Tab tabOccupancy;

    @FXML
    private Tab tabRevenue;

    @FXML
    private Tab tabFeedback;

    @FXML
    private BorderPane occupancyPane;

    @FXML
    private BorderPane revenuePane;

    @FXML
    private BorderPane feedbackPane;

    @FXML
    private Label lblError;

    @FXML
    private Label lblReportSummary;

    // Tables for each report type
    @FXML
    private TableView<RoomOccupancyData> tblOccupancy;

    @FXML
    private TableView<RevenueSummaryData> tblRevenue;

    @FXML
    private TableView<FeedbackSummaryData> tblFeedback;

    // Service classes
    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;
    private BillingService billingService;
    private FeedbackService feedbackService;

    // Current report data
    private List<Reservation> currentReservations;
    private List<Billing> currentBillings;
    private List<Feedback> currentFeedbacks;
    private String currentReportType;
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;

    // Data classes for tables
    public static class RoomOccupancyData {
        private final SimpleStringProperty roomType;
        private final SimpleIntegerProperty totalRooms;
        private final SimpleIntegerProperty occupiedRooms;
        private final SimpleDoubleProperty occupancyRate;

        public RoomOccupancyData(String roomType, int totalRooms, int occupiedRooms) {
            this.roomType = new SimpleStringProperty(roomType);
            this.totalRooms = new SimpleIntegerProperty(totalRooms);
            this.occupiedRooms = new SimpleIntegerProperty(occupiedRooms);
            double rate = totalRooms > 0 ? ((double) occupiedRooms / totalRooms) * 100 : 0;
            this.occupancyRate = new SimpleDoubleProperty(rate);
        }

        public String getRoomType() {
            return roomType.get();
        }

        public int getTotalRooms() {
            return totalRooms.get();
        }

        public int getOccupiedRooms() {
            return occupiedRooms.get();
        }

        public double getOccupancyRate() {
            return occupancyRate.get();
        }
    }

    public static class RevenueSummaryData {
        private final SimpleStringProperty date;
        private final SimpleDoubleProperty roomRevenue;
        private final SimpleDoubleProperty taxesCollected;
        private final SimpleDoubleProperty discountsGiven;
        private final SimpleDoubleProperty totalRevenue;

        public RevenueSummaryData(String date, double roomRevenue, double taxesCollected, double discountsGiven) {
            this.date = new SimpleStringProperty(date);
            this.roomRevenue = new SimpleDoubleProperty(roomRevenue);
            this.taxesCollected = new SimpleDoubleProperty(taxesCollected);
            this.discountsGiven = new SimpleDoubleProperty(discountsGiven);
            this.totalRevenue = new SimpleDoubleProperty(roomRevenue + taxesCollected - discountsGiven);
        }

        public String getDate() {
            return date.get();
        }

        public double getRoomRevenue() {
            return roomRevenue.get();
        }

        public double getTaxesCollected() {
            return taxesCollected.get();
        }

        public double getDiscountsGiven() {
            return discountsGiven.get();
        }

        public double getTotalRevenue() {
            return totalRevenue.get();
        }
    }

    public static class FeedbackSummaryData {
        private final SimpleStringProperty category;
        private final SimpleIntegerProperty count;
        private final SimpleDoubleProperty averageRating;

        public FeedbackSummaryData(String category, int count, double averageRating) {
            this.category = new SimpleStringProperty(category);
            this.count = new SimpleIntegerProperty(count);
            this.averageRating = new SimpleDoubleProperty(averageRating);
        }

        public String getCategory() {
            return category.get();
        }

        public int getCount() {
            return count.get();
        }

        public double getAverageRating() {
            return averageRating.get();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize service classes
        reservationService = new ReservationService();
        guestService = new GuestService();
        roomService = new RoomService();
        billingService = new BillingService();
        feedbackService = new FeedbackService();

        // Initialize report type combo box
        cmbReportType.setItems(FXCollections.observableArrayList(
                "Occupancy Report", "Revenue Report", "Guest Feedback Report"));
        cmbReportType.getSelectionModel().selectFirst();

        // Set default date range (last 30 days)
        dpEndDate.setValue(LocalDate.now());
        dpStartDate.setValue(LocalDate.now().minusDays(30));

        // Add listener to report type combo box to switch tabs
        cmbReportType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Occupancy Report")) {
                tabPaneReports.getSelectionModel().select(tabOccupancy);
            } else if (newValue.equals("Revenue Report")) {
                tabPaneReports.getSelectionModel().select(tabRevenue);
            } else if (newValue.equals("Guest Feedback Report")) {
                tabPaneReports.getSelectionModel().select(tabFeedback);
            }
        });

        // Initialize tables
        setupOccupancyTable();
        setupRevenueTable();
        setupFeedbackTable();

        // Initially hide the export button until a report is generated
        btnExportReport.setDisable(true);

        // Hide error message initially
        lblError.setVisible(false);

        LoggingManager.logSystemInfo("ReportController initialized");
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        // Clear previous data
        clearReportData();

        // Get report parameters
        currentReportType = cmbReportType.getValue();
        currentStartDate = dpStartDate.getValue();
        currentEndDate = dpEndDate.getValue();

        // Validate date range
        if (!validateDateRange()) {
            return;
        }

        try {
            // Fetch data based on report type
            if (currentReportType.equals("Occupancy Report")) {
                generateOccupancyReport();
            } else if (currentReportType.equals("Revenue Report")) {
                generateRevenueReport();
            } else if (currentReportType.equals("Guest Feedback Report")) {
                generateFeedbackReport();
            }

            // Enable export button
            btnExportReport.setDisable(false);

            // Log the report generation
            String adminUser = "Admin"; // In a real app, this would be the logged-in admin
            LoggingManager.logAdminActivity(adminUser,
                    "Generated " + currentReportType + " from " +
                            currentStartDate + " to " + currentEndDate);

        } catch (DatabaseException e) {
            LoggingManager.logException("Database error generating report", e);
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Error generating report", e);
            showError("An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportReport(ActionEvent event) {
        if (currentReportType == null || currentStartDate == null || currentEndDate == null) {
            showError("No report has been generated to export.");
            return;
        }

        try {
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            // Set default file name
            String fileName = currentReportType.replace(" ", "_") + "_" +
                    currentStartDate + "_to_" + currentEndDate + ".csv";
            fileChooser.setInitialFileName(fileName);

            // Show save dialog
            File file = fileChooser.showSaveDialog(btnExportReport.getScene().getWindow());

            if (file != null) {
                // Export the report based on type
                if (currentReportType.equals("Occupancy Report")) {
                    exportOccupancyReport(file);
                } else if (currentReportType.equals("Revenue Report")) {
                    exportRevenueReport(file);
                } else if (currentReportType.equals("Guest Feedback Report")) {
                    exportFeedbackReport(file);
                }

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("Report Exported");
                alert.setContentText("The report has been successfully exported to:\n" + file.getAbsolutePath());

                // Apply CSS to the dialog
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(getClass().getResource(Constants.CSS_ADMIN).toExternalForm());

                alert.showAndWait();

                // Log the export
                String adminUser = "Admin"; // In a real app, this would be the logged-in admin
                LoggingManager.logAdminActivity(adminUser,
                        "Exported " + currentReportType + " to " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            LoggingManager.logException("Error exporting report", e);
            showError("Error exporting report: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error during export", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleCloseButton(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    /**
     * Generate the occupancy report
     */
    private void generateOccupancyReport() throws DatabaseException {
        // Fetch reservations for the specified date range
        currentReservations = reservationService.getReservationsByDateRange(currentStartDate, currentEndDate);

        // Fetch all rooms
        List<Room> allRooms = roomService.getAllRooms();

        // Create a map to count rooms by type
        Map<RoomType, Integer> totalRoomsByType = new HashMap<>();
        Map<RoomType, Integer> occupiedRoomsByType = new HashMap<>();

        // Initialize counts
        for (RoomType type : RoomType.values()) {
            totalRoomsByType.put(type, 0);
            occupiedRoomsByType.put(type, 0);
        }

        // Count total rooms by type
        for (Room room : allRooms) {
            RoomType type = room.getRoomType();
            totalRoomsByType.put(type, totalRoomsByType.get(type) + 1);
        }

        // Count occupied rooms by type
        for (Reservation reservation : currentReservations) {
            if (reservation.getStatus().equals(Reservation.STATUS_CHECKED_IN) ||
                    reservation.getStatus().equals(Reservation.STATUS_CONFIRMED)) {

                // Get the room for this reservation
                Room room = roomService.getRoomById(reservation.getRoomID());
                RoomType type = room.getRoomType();

                // Increment the count for this room type
                occupiedRoomsByType.put(type, occupiedRoomsByType.get(type) + 1);
            }
        }

        // Create and populate the table
        ObservableList<RoomOccupancyData> occupancyData = FXCollections.observableArrayList();

        for (RoomType type : RoomType.values()) {
            int total = totalRoomsByType.get(type);
            int occupied = occupiedRoomsByType.get(type);

            occupancyData.add(new RoomOccupancyData(
                    type.getDisplayName(),
                    total,
                    occupied
            ));
        }

        // Add a row for the overall occupancy
        int totalRooms = allRooms.size();
        int totalOccupied = occupiedRoomsByType.values().stream().mapToInt(Integer::intValue).sum();
        occupancyData.add(new RoomOccupancyData("All Rooms", totalRooms, totalOccupied));

        // Set the table data
        tblOccupancy.setItems(occupancyData);

        // Create a pie chart for visualization
        PieChart occupancyChart = createOccupancyPieChart(occupancyData);
        VBox chartContainer = new VBox(occupancyChart);
        occupancyPane.setCenter(chartContainer);

        // Update summary label
        double overallOccupancy = totalRooms > 0 ? ((double) totalOccupied / totalRooms) * 100 : 0;
        lblReportSummary.setText(String.format(
                "Occupancy Report: %s to %s\nOverall Occupancy Rate: %.2f%% (%d out of %d rooms occupied)",
                currentStartDate, currentEndDate, overallOccupancy, totalOccupied, totalRooms));
    }

    /**
     * Generate the revenue report
     */
    private void generateRevenueReport() throws DatabaseException {
        // Fetch billings for the specified date range
        currentBillings = billingService.getBillingsByDateRange(currentStartDate, currentEndDate);

        // Group billings by date
        Map<LocalDate, List<Billing>> billingsByDate = currentBillings.stream()
                .collect(Collectors.groupingBy(b -> b.getBillingDateTime().toLocalDate()));

        // Create and populate the table
        ObservableList<RevenueSummaryData> revenueData = FXCollections.observableArrayList();

        double totalRoomRevenue = 0;
        double totalTaxes = 0;
        double totalDiscounts = 0;

        // For each date in the range
        LocalDate date = currentStartDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (!date.isAfter(currentEndDate)) {
            List<Billing> dayBillings = billingsByDate.getOrDefault(date, List.of());

            double roomRevenue = dayBillings.stream().mapToDouble(Billing::getAmount).sum();
            double taxesCollected = dayBillings.stream().mapToDouble(Billing::getTax).sum();
            double discountsGiven = dayBillings.stream().mapToDouble(Billing::getDiscount).sum();

            // Only add to the table if there was any activity on this day
            if (roomRevenue > 0 || taxesCollected > 0 || discountsGiven > 0) {
                revenueData.add(new RevenueSummaryData(
                        date.format(formatter),
                        roomRevenue,
                        taxesCollected,
                        discountsGiven
                ));

                totalRoomRevenue += roomRevenue;
                totalTaxes += taxesCollected;
                totalDiscounts += discountsGiven;
            }

            date = date.plusDays(1);
        }

        // Add a row for the totals
        revenueData.add(new RevenueSummaryData("TOTAL", totalRoomRevenue, totalTaxes, totalDiscounts));

        // Set the table data
        tblRevenue.setItems(revenueData);

        // Create a bar chart for visualization
        BarChart<String, Number> revenueChart = createRevenueBarChart(revenueData);
        VBox chartContainer = new VBox(revenueChart);
        revenuePane.setCenter(chartContainer);

        // Update summary label
        double totalRevenue = totalRoomRevenue + totalTaxes - totalDiscounts;
        lblReportSummary.setText(String.format(
                "Revenue Report: %s to %s\nTotal Revenue: $%.2f (Room Revenue: $%.2f, Taxes: $%.2f, Discounts: $%.2f)",
                currentStartDate, currentEndDate, totalRevenue, totalRoomRevenue, totalTaxes, totalDiscounts));
    }

    /**
     * Generate the feedback report
     */
    private void generateFeedbackReport() throws DatabaseException {
        // Fetch feedback for the specified date range
        currentFeedbacks = feedbackService.getFeedbackByDateRange(currentStartDate, currentEndDate);

        // Calculate average ratings
        double overallAverage = currentFeedbacks.stream()
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0);

        // Count feedback by rating
        Map<Integer, Long> feedbackByRating = currentFeedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));

        // Create and populate the table
        ObservableList<FeedbackSummaryData> feedbackData = FXCollections.observableArrayList();

        // Add a row for each rating level
        for (int i = 1; i <= 5; i++) {
            long count = feedbackByRating.getOrDefault(i, 0L);
            feedbackData.add(new FeedbackSummaryData(
                    i + " Star",
                    (int) count,
                    i
            ));
        }

        // Add a row for the overall average
        feedbackData.add(new FeedbackSummaryData(
                "Overall",
                currentFeedbacks.size(),
                overallAverage
        ));

        // Set the table data
        tblFeedback.setItems(feedbackData);

        // Create a pie chart for visualization
        PieChart feedbackChart = createFeedbackPieChart(feedbackData);
        VBox chartContainer = new VBox(feedbackChart);
        feedbackPane.setCenter(chartContainer);

        // Update summary label
        lblReportSummary.setText(String.format(
                "Guest Feedback Report: %s to %s\nTotal Feedback: %d, Average Rating: %.2f/5",
                currentStartDate, currentEndDate, currentFeedbacks.size(), overallAverage));
    }

    /**
     * Export the occupancy report to a CSV file
     */
    private void exportOccupancyReport(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println("Room Type,Total Rooms,Occupied Rooms,Occupancy Rate (%)");

            // Write data rows
            for (RoomOccupancyData data : tblOccupancy.getItems()) {
                writer.printf("%s,%d,%d,%.2f\n",
                        data.getRoomType(),
                        data.getTotalRooms(),
                        data.getOccupiedRooms(),
                        data.getOccupancyRate());
            }
        }
    }

    /**
     * Export the revenue report to a CSV file
     */
    private void exportRevenueReport(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println("Date,Room Revenue,Taxes Collected,Discounts Given,Total Revenue");

            // Write data rows
            for (RevenueSummaryData data : tblRevenue.getItems()) {
                writer.printf("%s,%.2f,%.2f,%.2f,%.2f\n",
                        data.getDate(),
                        data.getRoomRevenue(),
                        data.getTaxesCollected(),
                        data.getDiscountsGiven(),
                        data.getTotalRevenue());
            }
        }
    }

    /**
     * Export the feedback report to a CSV file
     */
    private void exportFeedbackReport(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println("Category,Count,Average Rating");

            // Write data rows
            for (FeedbackSummaryData data : tblFeedback.getItems()) {
                writer.printf("%s,%d,%.2f\n",
                        data.getCategory(),
                        data.getCount(),
                        data.getAverageRating());
            }

            // Write individual feedback comments
            writer.println("\nIndividual Feedback:");
            writer.println("Date,Guest,Rating,Comments");

            for (Feedback feedback : currentFeedbacks) {
                try {
                    Guest guest = guestService.getGuestById(feedback.getGuestID());
                    String guestName = guest != null ? guest.getName() : "Unknown";

                    writer.printf("%s,%s,%d,\"%s\"\n",
                            feedback.getSubmissionDateTime().toLocalDate(),
                            guestName,
                            feedback.getRating(),
                            feedback.getComments().replace("\"", "\"\""));  // Escape quotes for CSV
                } catch (Exception e) {
                    // Skip this feedback if there's an error
                    LoggingManager.logException("Error exporting feedback", e);
                }
            }
        }
    }

    /**
     * Create a pie chart for the occupancy report
     */
    private PieChart createOccupancyPieChart(ObservableList<RoomOccupancyData> data) {
        PieChart chart = new PieChart();
        chart.setTitle("Room Occupancy by Type");

        // Remove the "All Rooms" entry for the chart
        for (int i = 0; i < data.size() - 1; i++) {
            RoomOccupancyData roomData = data.get(i);

            // Only add to chart if there are rooms of this type
            if (roomData.getTotalRooms() > 0) {
                String name = roomData.getRoomType() + " (" +
                        roomData.getOccupiedRooms() + "/" + roomData.getTotalRooms() + ")";

                chart.getData().add(new PieChart.Data(name, roomData.getOccupancyRate()));
            }
        }

        return chart;
    }

    /**
     * Create a bar chart for the revenue report
     */
    private BarChart<String, Number> createRevenueBarChart(ObservableList<RevenueSummaryData> data) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        chart.setTitle("Revenue by Date");
        xAxis.setLabel("Date");
        yAxis.setLabel("Amount ($)");

        // Create series for room revenue, taxes, and discounts
        XYChart.Series<String, Number> roomRevenueSeries = new XYChart.Series<>();
        roomRevenueSeries.setName("Room Revenue");

        XYChart.Series<String, Number> taxesSeries = new XYChart.Series<>();
        taxesSeries.setName("Taxes");

        XYChart.Series<String, Number> discountsSeries = new XYChart.Series<>();
        discountsSeries.setName("Discounts");

        // Remove the "TOTAL" entry for the chart
        for (int i = 0; i < data.size() - 1; i++) {
            RevenueSummaryData revenueData = data.get(i);

            roomRevenueSeries.getData().add(new XYChart.Data<>(
                    revenueData.getDate(), revenueData.getRoomRevenue()));

            taxesSeries.getData().add(new XYChart.Data<>(
                    revenueData.getDate(), revenueData.getTaxesCollected()));

            discountsSeries.getData().add(new XYChart.Data<>(
                    revenueData.getDate(), revenueData.getDiscountsGiven()));
        }

        chart.getData().addAll(roomRevenueSeries, taxesSeries, discountsSeries);
        return chart;
    }

    /**
     * Create a pie chart for the feedback report
     */
    private PieChart createFeedbackPieChart(ObservableList<FeedbackSummaryData> data) {
        PieChart chart = new PieChart();
        chart.setTitle("Feedback Distribution by Rating");

        // Remove the "Overall" entry for the chart
        for (int i = 0; i < data.size() - 1; i++) {
            FeedbackSummaryData feedbackData = data.get(i);

            // Only add to chart if there is feedback with this rating
            if (feedbackData.getCount() > 0) {
                String name = feedbackData.getCategory() + " (" + feedbackData.getCount() + ")";

                chart.getData().add(new PieChart.Data(name, feedbackData.getCount()));
            }
        }

        return chart;
    }

    /**
     * Setup the occupancy table columns
     */
    private void setupOccupancyTable() {
        // Create columns
        TableColumn<RoomOccupancyData, String> roomTypeCol = new TableColumn<>("Room Type");
        roomTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomType()));

        TableColumn<RoomOccupancyData, Number> totalRoomsCol = new TableColumn<>("Total Rooms");
        totalRoomsCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTotalRooms()));

        TableColumn<RoomOccupancyData, Number> occupiedRoomsCol = new TableColumn<>("Occupied Rooms");
        occupiedRoomsCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getOccupiedRooms()));

        TableColumn<RoomOccupancyData, Number> occupancyRateCol = new TableColumn<>("Occupancy Rate (%)");
        occupancyRateCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getOccupancyRate()));
        occupancyRateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", item.doubleValue()));
                }
            }
        });

        // Add columns to table
        tblOccupancy.getColumns().addAll(roomTypeCol, totalRoomsCol, occupiedRoomsCol, occupancyRateCol);
    }

    /**
     * Setup the revenue table columns
     */
    private void setupRevenueTable() {
        // Create columns
        TableColumn<RevenueSummaryData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));

        TableColumn<RevenueSummaryData, Number> roomRevenueCol = new TableColumn<>("Room Revenue");
        roomRevenueCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getRoomRevenue()));
        roomRevenueCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        });

        TableColumn<RevenueSummaryData, Number> taxesCol = new TableColumn<>("Taxes Collected");
        taxesCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTaxesCollected()));
        taxesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        });

        TableColumn<RevenueSummaryData, Number> discountsCol = new TableColumn<>("Discounts Given");
        discountsCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getDiscountsGiven()));
        discountsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        });

        TableColumn<RevenueSummaryData, Number> totalCol = new TableColumn<>("Total Revenue");
        totalCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalRevenue()));
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        });

        // Add columns to table
        tblRevenue.getColumns().addAll(dateCol, roomRevenueCol, taxesCol, discountsCol, totalCol);
    }
}