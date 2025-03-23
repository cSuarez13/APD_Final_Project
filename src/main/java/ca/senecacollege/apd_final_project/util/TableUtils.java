package ca.senecacollege.apd_final_project.util;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.function.Function;

/**
 * Utility class for common TableView operations in the application
 */
public class TableUtils {

    /**
     * Configure table columns to resize properly
     * @param tableView The table to configure
     */
    public static void configureTableColumnWidth(TableView<?> tableView) {
        // Use UNCONSTRAINED_RESIZE_POLICY for better column width distribution
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Distribute column widths proportionally
        double tableWidth = tableView.getWidth();
        double totalColumnWidth = 0;

        for (TableColumn<?,?> column : tableView.getColumns()) {
            totalColumnWidth += column.getWidth();
        }

        if (totalColumnWidth > 0) {
            for (TableColumn<?,?> column : tableView.getColumns()) {
                // Set each column to proportional width
                double percentage = column.getWidth() / totalColumnWidth;
                column.setPrefWidth(tableWidth * percentage);
            }
        }

        // Add admin-table style class if not present
        if (!tableView.getStyleClass().contains("admin-table")) {
            tableView.getStyleClass().add("admin-table");
        }
    }

    /**
     * Set up a reservations table with standard columns
     * @param tableView The table view to set up
     * @param data The data to populate the table with
     * @param guestService The guest service to look up guest names
     */
    public static void setupReservationsTable(TableView<Reservation> tableView,
                                              ObservableList<Reservation> data,
                                              GuestService guestService) {
        // Clear existing columns
        tableView.getColumns().clear();

        // Define columns with detailed configuration
        TableColumn<Reservation, String> colReservationId = createColumn("ID",
                cellData -> String.valueOf(cellData.getValue().getReservationID()));
        colReservationId.setPrefWidth(80);
        colReservationId.setMinWidth(50);

        TableColumn<Reservation, String> colGuestName = createColumn("Guest",
                cellData -> {
                    try {
                        Guest guest = guestService.getGuestById(cellData.getValue().getGuestID());
                        return guest != null ? guest.getName() : "Unknown";
                    } catch (Exception e) {
                        return "Error";
                    }
                });
        colGuestName.setPrefWidth(200);
        colGuestName.setMinWidth(150);

        TableColumn<Reservation, String> colCheckIn = createColumn("Check-in",
                cellData -> cellData.getValue().getCheckInDate().toString());
        colCheckIn.setPrefWidth(120);
        colCheckIn.setMinWidth(100);

        TableColumn<Reservation, String> colCheckOut = createColumn("Check-out",
                cellData -> cellData.getValue().getCheckOutDate().toString());
        colCheckOut.setPrefWidth(120);
        colCheckOut.setMinWidth(100);

        TableColumn<Reservation, String> colStatus = createStatusColumn();
        colStatus.setPrefWidth(100);
        colStatus.setMinWidth(80);

        tableView.getColumns().addAll(colReservationId, colGuestName, colCheckIn, colCheckOut, colStatus);
        tableView.setItems(data);

        // Configure column widths to fill the table
        configureTableColumnWidth(tableView);
    }

    // Helper method to create standard columns
    private static TableColumn<Reservation, String> createColumn(String title,
                                                                 Function<TableColumn.CellDataFeatures<Reservation, String>, String> cellValueExtractor) {
        TableColumn<Reservation, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellValueExtractor.apply(cellData)));
        return column;
    }

    // Helper method to create status column with styling
    private static TableColumn<Reservation, String> createStatusColumn() {
        TableColumn<Reservation, String> colStatus = new TableColumn<>("Status");

        // Use a lambda to convert the ReservationStatus to its display name
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatusDisplayName()));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);
                getStyleClass().removeAll("status-confirmed", "status-checked-in",
                        "status-checked-out", "status-cancelled");

                switch (item) {
                    case "Confirmed":
                        getStyleClass().add("status-confirmed");
                        break;
                    case "Checked In":
                        getStyleClass().add("status-checked-in");
                        break;
                    case "Checked Out":
                        getStyleClass().add("status-checked-out");
                        break;
                    case "Cancelled":
                        getStyleClass().add("status-cancelled");
                        break;
                }
            }
        });

        return colStatus;
    }

    /**
     * Setup a guest search results table with standard columns
     * @param tableView The table view to setup
     * @param data The data to populate the table with
     */
    public static void setupGuestTable(TableView<Guest> tableView, ObservableList<Guest> data) {
        // Clear existing columns
        tableView.getColumns().clear();

        TableColumn<Guest, String> colGuestId = new TableColumn<>("ID");
        colGuestId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getGuestID())));
        colGuestId.setPrefWidth(80);
        colGuestId.setMinWidth(50);

        TableColumn<Guest, String> colGuestName = new TableColumn<>("Name");
        colGuestName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colGuestName.setPrefWidth(200);
        colGuestName.setMinWidth(150);

        TableColumn<Guest, String> colGuestPhone = new TableColumn<>("Phone");
        colGuestPhone.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
        colGuestPhone.setPrefWidth(150);
        colGuestPhone.setMinWidth(120);

        TableColumn<Guest, String> colGuestEmail = new TableColumn<>("Email");
        colGuestEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colGuestEmail.setPrefWidth(200);
        colGuestEmail.setMinWidth(150);

        tableView.getColumns().addAll(colGuestId, colGuestName, colGuestPhone, colGuestEmail);
        tableView.setItems(data);

        // Configure column widths to fill the table
        configureTableColumnWidth(tableView);
    }
}