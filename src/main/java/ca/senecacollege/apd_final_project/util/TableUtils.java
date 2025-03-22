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
        // Use CONSTRAINED_RESIZE_POLICY to make columns fill available space evenly
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set all columns to have equal percentage widths
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            column.prefWidthProperty().bind(
                    tableView.widthProperty().divide(tableView.getColumns().size()).subtract(2)
            );
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
                cellData -> cellData.getValue().getCheckInDate().toString());

        TableColumn<Reservation, String> colCheckOut = createColumn("Check-out",
                cellData -> cellData.getValue().getCheckOutDate().toString());

        TableColumn<Reservation, String> colStatus = createStatusColumn();

        tableView.getColumns().addAll(colReservationId, colGuestName, colCheckIn, colCheckOut, colStatus);
        tableView.setItems(data);
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
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

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
                    case Reservation.STATUS_CONFIRMED:
                        getStyleClass().add("status-confirmed");
                        break;
                    case Reservation.STATUS_CHECKED_IN:
                        getStyleClass().add("status-checked-in");
                        break;
                    case Reservation.STATUS_CHECKED_OUT:
                        getStyleClass().add("status-checked-out");
                        break;
                    case Reservation.STATUS_CANCELLED:
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

        TableColumn<Guest, String> colGuestName = new TableColumn<>("Name");
        colGuestName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Guest, String> colGuestPhone = new TableColumn<>("Phone");
        colGuestPhone.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());

        TableColumn<Guest, String> colGuestEmail = new TableColumn<>("Email");
        colGuestEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        tableView.getColumns().addAll(colGuestId, colGuestName, colGuestPhone, colGuestEmail);
        tableView.setItems(data);
    }
}