package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ReservationException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.service.DialogService;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.service.RoomService;
import ca.senecacollege.apd_final_project.service.ServiceLocator;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ModifyReservationController extends BaseController {

    @FXML
    private Label lblReservationId;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblGuestId;

    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblCurrentRoom;

    @FXML
    private ComboBox<Room> cmbNewRoom;

    @FXML
    private DatePicker dpCheckInDate;

    @FXML
    private DatePicker dpCheckOutDate;

    @FXML
    private Spinner<Integer> spnNumberOfGuests;

    @FXML
    private Label lblError;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    // Services
    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    // Current data
    private Reservation originalReservation;
    private Room currentRoom;
    private Guest currentGuest;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Get services from ServiceLocator
        reservationService = ServiceLocator.getService(ReservationService.class);
        guestService = ServiceLocator.getService(GuestService.class);
        roomService = ServiceLocator.getService(RoomService.class);

        // Set up date pickers
        setupDatePickers();

        // Set up number of guests spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        spnNumberOfGuests.setValueFactory(valueFactory);

        // Set up room combo box
        setupRoomComboBox();

        // Hide error message initially
        hideError();

        LoggingManager.logSystemInfo("ModifyReservationController initialized");
    }

    /**
     * Initialize controller with admin data
     *
     * @param admin The admin user
     */
    @Override
    public void initData(Admin admin) {
        super.initData(admin);
        LoggingManager.logSystemInfo("ModifyReservationController initialized with admin: " + admin.getUsername());
    }

    /**
     * Initialize the controller with a reservation
     *
     * @param reservation The reservation to modify
     */
    public void initReservation(Reservation reservation) {
        try {
            this.originalReservation = reservation;

            // Display reservation information
            lblReservationId.setText(String.valueOf(reservation.getReservationID()));
            lblStatus.setText(reservation.getStatusDisplayName());

            // Load and display guest information
            currentGuest = guestService.getGuestById(reservation.getGuestID());
            if (currentGuest != null) {
                lblGuestId.setText(String.valueOf(currentGuest.getGuestID()));
                lblGuestName.setText(currentGuest.getName());
            }

            // Load and display room information
            currentRoom = roomService.getRoomById(reservation.getRoomID());
            if (currentRoom != null) {
                lblCurrentRoom.setText(currentRoom.getRoomType().getDisplayName() +
                        " (Room #" + currentRoom.getRoomID() + ")");
            }

            // Set values for editable fields
            dpCheckInDate.setValue(reservation.getCheckInDate());
            dpCheckOutDate.setValue(reservation.getCheckOutDate());
            spnNumberOfGuests.getValueFactory().setValue(reservation.getNumberOfGuests());

            // Load available rooms
            loadAvailableRooms();

            LoggingManager.logSystemInfo("Loaded reservation #" + reservation.getReservationID() + " for modification");

        } catch (DatabaseException e) {
            LoggingManager.logException("Error loading reservation details", e);
            showError("Error loading reservation details: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error initializing reservation", e);
            showError("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Set up date pickers with constraints
     */
    private void setupDatePickers() {
        // Ensure check-in date is not before today
        dpCheckInDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Ensure check-out date is after check-in date
        dpCheckOutDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = dpCheckInDate.getValue();
                setDisable(empty || (checkInDate != null && date.isBefore(checkInDate)) ||
                        date.isBefore(LocalDate.now()));
            }
        });

        // Add listeners to update constraints when dates change
        dpCheckInDate.valueProperty().addListener((obs, oldValue, newValue) -> {
            // If check-out date is now invalid, update it
            if (dpCheckOutDate.getValue() != null && dpCheckOutDate.getValue().isBefore(newValue)) {
                dpCheckOutDate.setValue(newValue.plusDays(1));
            }

            // Reload available rooms since date changed
            loadAvailableRooms();
        });

        dpCheckOutDate.valueProperty().addListener((obs, oldValue, newValue) -> {
            // Reload available rooms since date changed
            loadAvailableRooms();
        });
    }

    /**
     * Set up room combo box with custom cell factory
     */
    private void setupRoomComboBox() {
        // Set up string converter to display room information
        cmbNewRoom.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                if (room == null) {
                    return null;
                }
                return room.getRoomType().getDisplayName() + " (Room #" + room.getRoomID() +
                        ", $" + room.getPrice() + "/night)";
            }

            @Override
            public Room fromString(String string) {
                // Not needed for our use case
                return null;
            }
        });

        // Set up cell factory for dropdown items
        cmbNewRoom.setCellFactory(param -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);

                if (empty || room == null) {
                    setText(null);
                } else {
                    setText(room.getRoomType().getDisplayName() + " (Room #" + room.getRoomID() +
                            ", $" + room.getPrice() + "/night)");
                }
            }
        });
    }

    /**
     * Load available rooms based on selected dates
     */
    private void loadAvailableRooms() {
        // Only load rooms if dates are selected
        if (dpCheckInDate.getValue() == null || dpCheckOutDate.getValue() == null) {
            return;
        }

        try {
            // Get all rooms
            List<Room> allRooms = roomService.getAllRooms();

            // Filter available rooms
            List<Room> availableRooms = allRooms.stream()
                    .filter(room -> {
                        // Skip if room is not available
                        if (!room.isAvailable() && room.getRoomID() != originalReservation.getRoomID()) {
                            return false;
                        }

                        try {
                            // For other rooms, check availability for the selected dates
                            if (room.getRoomID() == originalReservation.getRoomID()) {
                                // Current room is always available
                                return true;
                            } else {
                                // Check if the room is available for the selected dates
                                return roomService.isRoomAvailable(room.getRoomID(),
                                        dpCheckInDate.getValue(), dpCheckOutDate.getValue(),
                                        originalReservation.getReservationID());
                            }
                        } catch (Exception e) {
                            LoggingManager.logException("Error checking room availability", e);
                            return false;
                        }
                    })
                    .sorted((r1, r2) -> {
                        // Sort by room type first, then by room ID
                        int typeCompare = r1.getRoomType().compareTo(r2.getRoomType());
                        if (typeCompare != 0) {
                            return typeCompare;
                        } else {
                            return Integer.compare(r1.getRoomID(), r2.getRoomID());
                        }
                    })
                    .collect(Collectors.toList());

            // Update the combo box
            cmbNewRoom.setItems(FXCollections.observableArrayList(availableRooms));

            // Select the current room
            cmbNewRoom.getItems().stream()
                    .filter(room -> room.getRoomID() == originalReservation.getRoomID())
                    .findFirst()
                    .ifPresent(room -> cmbNewRoom.setValue(room));

        } catch (Exception e) {
            LoggingManager.logException("Error loading available rooms", e);
            showError("Error loading available rooms: " + e.getMessage());
        }
    }

    /**
     * Handle save button click
     */
    @FXML
    private void handleSaveButton() {
        // Hide previous error message
        hideError();

        try {
            // Validate input
            validateInput();

            // Check if there are any changes
            if (!isModified()) {
                DialogService.showInformation(
                        getStage(),
                        "No Changes",
                        "No changes have been made to the reservation."
                );
                return;
            }

            // Create a modified reservation
            Reservation modifiedReservation = createModifiedReservation();

            // Save changes
            reservationService.updateReservation(modifiedReservation);

            // Handle room changes
            handleRoomChange(modifiedReservation);

            // Show success message
            DialogService.showInformation(
                    getStage(),
                    "Reservation Updated",
                    "Reservation #" + modifiedReservation.getReservationID() + " has been updated successfully."
            );

            // Log the action
            logAdminActivity("Modified reservation #" + modifiedReservation.getReservationID());

            // Close the dialog
            closeWindow();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (DatabaseException e) {
            LoggingManager.logException("Database error updating reservation", e);
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Error updating reservation", e);
            showError("Error updating reservation: " + e.getMessage());
        }
    }

    /**
     * Validate the input values
     *
     * @throws ValidationException if validation fails
     */
    private void validateInput() throws ValidationException {
        // Check if dates are selected
        if (dpCheckInDate.getValue() == null) {
            throw new ValidationException("Please select a check-in date.");
        }

        if (dpCheckOutDate.getValue() == null) {
            throw new ValidationException("Please select a check-out date.");
        }

        // Validate date range
        ValidationUtils.validateDateRange(
                dpCheckInDate.getValue(),
                dpCheckOutDate.getValue(),
                "Check-in date",
                "Check-out date"
        );

        // Check if check-in date is in the future
        if (ValidationUtils.isValidFutureDate(dpCheckInDate.getValue())) {
            throw new ValidationException("Check-in date must be today or a future date.");
        }

        // Check if a room is selected
        if (cmbNewRoom.getValue() == null) {
            throw new ValidationException("Please select a room.");
        }

        // Validate number of guests against room capacity
        RoomType roomType = cmbNewRoom.getValue().getRoomType();
        int maxOccupancy = roomType.getMaxOccupancy();
        int numberOfGuests = spnNumberOfGuests.getValue();

        if (numberOfGuests > maxOccupancy) {
            throw new ValidationException(
                    "The selected room can accommodate a maximum of " + maxOccupancy + " guests. " +
                            "Please select a different room or reduce the number of guests."
            );
        }
    }

    /**
     * Check if the reservation has been modified
     *
     * @return true if any field has been modified
     */
    private boolean isModified() {
        // Check if check-in date has changed
        if (!dpCheckInDate.getValue().equals(originalReservation.getCheckInDate())) {
            return true;
        }

        // Check if check-out date has changed
        if (!dpCheckOutDate.getValue().equals(originalReservation.getCheckOutDate())) {
            return true;
        }

        // Check if number of guests has changed
        if (spnNumberOfGuests.getValue() != originalReservation.getNumberOfGuests()) {
            return true;
        }

        // Check if room has changed
        if (cmbNewRoom.getValue().getRoomID() != originalReservation.getRoomID()) {
            return true;
        }

        return false;
    }

    /**
     * Create a modified reservation based on input values
     *
     * @return The modified reservation
     */
    private Reservation createModifiedReservation() {
        // Create a new reservation with the same ID and status
        Reservation modifiedReservation = new Reservation();
        modifiedReservation.setReservationID(originalReservation.getReservationID());
        modifiedReservation.setGuestID(originalReservation.getGuestID());
        modifiedReservation.setStatus(originalReservation.getStatus());

        // Set modified values
        modifiedReservation.setRoomID(cmbNewRoom.getValue().getRoomID());
        modifiedReservation.setCheckInDate(dpCheckInDate.getValue());
        modifiedReservation.setCheckOutDate(dpCheckOutDate.getValue());
        modifiedReservation.setNumberOfGuests(spnNumberOfGuests.getValue());

        return modifiedReservation;
    }

    /**
     * Handle room changes
     *
     * @param modifiedReservation The modified reservation
     * @throws DatabaseException    If there's an error updating room availability
     * @throws ReservationException If there's an error updating the reservation
     */
    private void handleRoomChange(Reservation modifiedReservation)
            throws DatabaseException, ReservationException {
        // Check if room has changed
        if (modifiedReservation.getRoomID() != originalReservation.getRoomID()) {
            // Make the old room available again
            roomService.setRoomAvailability(originalReservation.getRoomID(), true);

            // Make the new room unavailable
            roomService.setRoomAvailability(modifiedReservation.getRoomID(), false);

            LoggingManager.logSystemInfo(
                    "Changed room for reservation #" + modifiedReservation.getReservationID() +
                            " from " + originalReservation.getRoomID() + " to " + modifiedReservation.getRoomID()
            );
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancelButton() {
        // Close the window without saving
        closeWindow();
    }

    @Override
    protected Stage getStage() {
        if (btnCancel != null && btnCancel.getScene() != null) {
            return (Stage) btnCancel.getScene().getWindow();
        }
        return null;
    }
}