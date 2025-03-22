package ca.senecacollege.apd_final_project.util;

import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.GuestService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
     * Setup a reservations table with standard columns
     * @param tableView The table view to setup
     * @param data The data to populate the table with
     * @param guestService The guest service to look up guest names
     */
    public static void setupReservationsTable(TableView<Reservation> tableView,
                                              ObservableList<Reservation> data,
                                              GuestService guestService) {
        // Clear existing columns
        tableView.getColumns().clear();

        TableColumn<Reservation, String> colReservationId = new TableColumn<>("ID");
        colReservationId.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getReservationID())));

        TableColumn<Reservation, String> colGuestName = new TableColumn<>("Guest");
        colGuestName.setCellValueFactory(cellData -> {
            try {
                Guest guest = guestService.getGuestById(cellData.getValue().getGuestID());
                return new SimpleStringProperty(guest.getName());
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown");
            }
        });

        TableColumn<Reservation, String> colCheckIn = new TableColumn<>("Check-in");
        colCheckIn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCheckInDate().toString()));

        TableColumn<Reservation, String> colCheckOut = new TableColumn<>("Check-out");
        colCheckOut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCheckOutDate().toString()));

        TableColumn<Reservation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Add style class based on status
        colStatus.setCellFactory(column -> new javafx.scene.control.TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Apply appropriate style class based on status
                    if (item.equals(Reservation.STATUS_CONFIRMED)) {
                        getStyleClass().add("status-confirmed");
                    } else if (item.equals(Reservation.STATUS_CHECKED_IN)) {
                        getStyleClass().add("status-checked-in");
                    } else if (item.equals(Reservation.STATUS_CHECKED_OUT)) {
                        getStyleClass().add("status-checked-out");
                    } else if (item.equals(Reservation.STATUS_CANCELLED)) {
                        getStyleClass().add("status-cancelled");
                    }
                }
            }
        });

        tableView.getColumns().addAll(colReservationId, colGuestName, colCheckIn, colCheckOut, colStatus);
        tableView.setItems(data);
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