package ca.senecacollege.apd_final_project.util;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Utility class for common TableView operations in the application
 */
public class TableUtils {
    // Shared date formatter for consistent date display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        // Make columns fill the entire table width
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Define columns
        TableColumn<Reservation, String> colReservationId = createColumn("ID",
                cellData -> String.valueOf(cellData.getValue().getReservationID()));

        TableColumn<Reservation, String> colGuestName = createColumn("Guest",
                cellData -> {
                    try {
                        Guest guest = guestService.getGuestById(cellData.getValue().getGuestID());
                        return guest != null ? guest.getName() : "Unknown";
                    } catch (Exception e) {
                        return "Error";
                    }
                });

        TableColumn<Reservation, String> colCheckIn = createColumn("Check-in",
                cellData -> cellData.getValue().getCheckInDate().format(DATE_FORMATTER));

        TableColumn<Reservation, String> colCheckOut = createColumn("Check-out",
                cellData -> cellData.getValue().getCheckOutDate().format(DATE_FORMATTER));

        TableColumn<Reservation, String> colStatus = createStatusColumn();

        tableView.getColumns().addAll(colReservationId, colGuestName, colCheckIn, colCheckOut, colStatus);
        tableView.setItems(data);
    }

    /**
     * Set up a guest search results table with standard columns
     * @param tableView The table view to set up
     * @param data The data to populate the table with
     */
    public static void setupGuestTable(TableView<Guest> tableView, ObservableList<Guest> data) {
        // Clear existing columns
        tableView.getColumns().clear();

        // Make columns fill the entire table width
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Guest, String> colGuestId = new TableColumn<>("ID");
        colGuestId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getGuestID())));

        TableColumn<Guest, String> colGuestName = new TableColumn<>("Name");
        colGuestName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Guest, String> colGuestPhone = new TableColumn<>("Phone");
        colGuestPhone.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());

        TableColumn<Guest, String> colGuestEmail = new TableColumn<>("Email");
        colGuestEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        tableView.getColumns().addAll(colGuestId, colGuestName, colGuestPhone, colGuestEmail);
        tableView.setItems(data);
    }


    /**
     * Create a standard column with string value
     */
    private static TableColumn<Reservation, String> createColumn(String title,
                                                                 Function<TableColumn.CellDataFeatures<Reservation, String>, String> cellValueExtractor) {
        TableColumn<Reservation, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellValueExtractor.apply(cellData)));
        return column;
    }

    /**
     * Create a status column with custom styling
     */
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
}