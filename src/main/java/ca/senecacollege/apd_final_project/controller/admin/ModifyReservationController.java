package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ReservationException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.*;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
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
    private TableView<Room> tblRooms;

    @FXML
    private Button btnAddRoom;

    @FXML
    private Button btnRemoveRoom;

    @FXML
    private ComboBox<Room> cmbAvailableRooms;

    @FXML
    private Spinner<Integer> spnGuestsInRoom;

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

    @FXML
    private VBox roomDetailsContainer;

    // Services
    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    // Current data
    private Reservation originalReservation;
    private List<Room> originalRooms;
    private List<ReservationRoom> originalReservationRooms;
    private Guest currentGuest;

    // New room assignments for the modified reservation
    private final Map<Integer, Integer> roomAssignments = new HashMap<>();

    // ObservableList for the table
    private final ObservableList<Room> selectedRooms = FXCollections.observableArrayList();

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
        SpinnerValueFactory<Integer> guestValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        spnNumberOfGuests.setValueFactory(guestValueFactory);

        // Set up guests in room spinner
        SpinnerValueFactory<Integer> roomGuestsValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1);
        spnGuestsInRoom.setValueFactory(roomGuestsValueFactory);

        // Set up room combo box
        setupRoomComboBox();

        // Setup room table
        setupRoomTable();

        // Disable add/remove room buttons initially
        btnAddRoom.setDisable(true);
        btnRemoveRoom.setDisable(true);

        // Add listener to enable/disable add room button based on combo box selection
        cmbAvailableRooms.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnAddRoom.setDisable(newVal == null);
        });

        // Add listener to enable/disable remove room button based on table selection
        tblRooms.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnRemoveRoom.setDisable(newVal == null);
        });

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

            // Load all rooms for this reservation
            originalRooms = reservationService.getRoomsForReservation(reservation.getReservationID());
            originalReservationRooms = reservationService.getReservationRooms(reservation.getReservationID());

            // Set the room assignments based on current reservation
            for (ReservationRoom rr : originalReservationRooms) {
                roomAssignments.put(rr.getRoomID(), rr.getGuestsInRoom());
            }

            // Update the room table
            selectedRooms.clear();
            selectedRooms.addAll(originalRooms);

            // If there's a primary room, display it in the original room field
            if (!originalRooms.isEmpty()) {
                Room primaryRoom = originalRooms.get(0);
                lblCurrentRoom.setText(primaryRoom.getRoomType().getDisplayName() +
                        " (Room #" + primaryRoom.getRoomID() + ")");
            } else {
                lblCurrentRoom.setText("No rooms assigned");
            }

            // Set values for editable fields
            dpCheckInDate.setValue(reservation.getCheckInDate());
            dpCheckOutDate.setValue(reservation.getCheckOutDate());
            spnNumberOfGuests.getValueFactory().setValue(reservation.getNumberOfGuests());

            // Load available rooms
            loadAvailableRooms();

            LoggingManager.logSystemInfo("Loaded reservation #" + reservation.getReservationID() +
                    " with " + originalRooms.size() + " rooms for modification");

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
        cmbAvailableRooms.setConverter(new StringConverter<Room>() {
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
        cmbAvailableRooms.setCellFactory(param -> new ListCell<Room>() {
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
     * Set up the room table
     */
    private void setupRoomTable() {
        // Create columns
        TableColumn<Room, String> roomIdCol = new TableColumn<>("Room #");
        roomIdCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getRoomID())));

        TableColumn<Room, String> roomTypeCol = new TableColumn<>("Type");
        roomTypeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRoomType().getDisplayName()));

        TableColumn<Room, String> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getRoomType().getMaxOccupancy())));

        TableColumn<Room, String> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("$%.2f", data.getValue().getPrice())));

        TableColumn<Room, String> guestsCol = new TableColumn<>("Guests");
        guestsCol.setCellValueFactory(data -> {
            int roomId = data.getValue().getRoomID();
            int guests = roomAssignments.getOrDefault(roomId, 0);
            return new SimpleStringProperty(String.valueOf(guests));
        });

        // Add columns to the table
        tblRooms.getColumns().addAll(roomIdCol, roomTypeCol, capacityCol, priceCol, guestsCol);

        // Set the items
        tblRooms.setItems(selectedRooms);
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
                        // Skip if room is not available and not already in the reservation
                        if (!room.isAvailable() && !isRoomInReservation(room.getRoomID())) {
                            return false;
                        }

                        try {
                            // For other rooms, check availability for the selected dates
                            if (isRoomInReservation(room.getRoomID())) {
                                // Current room is always available for this reservation
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

            // Filter out rooms that are already selected
            availableRooms = availableRooms.stream()
                    .filter(room -> !isRoomAlreadySelected(room.getRoomID()))
                    .collect(Collectors.toList());

            // Update the combo box
            cmbAvailableRooms.setItems(FXCollections.observableArrayList(availableRooms));

        } catch (Exception e) {
            LoggingManager.logException("Error loading available rooms", e);
            showError("Error loading available rooms: " + e.getMessage());
        }
    }

    /**
     * Check if a room is already part of the original reservation
     */
    private boolean isRoomInReservation(int roomId) {
        return originalRooms.stream().anyMatch(r -> r.getRoomID() == roomId);
    }

    /**
     * Check if a room is already in the selected rooms list
     */
    private boolean isRoomAlreadySelected(int roomId) {
        return selectedRooms.stream().anyMatch(r -> r.getRoomID() == roomId);
    }

    @FXML
    private void handleAddRoom() {
        Room selectedRoom = cmbAvailableRooms.getValue();
        if (selectedRoom == null) {
            return;
        }

        try {
            // Check if the selected room can accommodate the number of guests
            int guestsInRoom = spnGuestsInRoom.getValue();
            int maxCapacity = selectedRoom.getRoomType().getMaxOccupancy();

            if (guestsInRoom > maxCapacity) {
                showError("This room can only accommodate " + maxCapacity + " guests");
                return;
            }

            // Add the room to the selected rooms list
            selectedRooms.add(selectedRoom);

            // Add the guest assignment
            roomAssignments.put(selectedRoom.getRoomID(), guestsInRoom);

            // Update total guest count
            updateTotalGuestCount();

            // Refresh the available rooms list
            loadAvailableRooms();

            // Clear the selection
            cmbAvailableRooms.getSelectionModel().clearSelection();

        } catch (Exception e) {
            LoggingManager.logException("Error adding room", e);
            showError("Error adding room: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveRoom() {
        Room selectedRoom = tblRooms.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            return;
        }

        try {
            // Check if this is the only room (can't remove all rooms)
            if (selectedRooms.size() <= 1) {
                showError("Cannot remove the only room from a reservation");
                return;
            }

            // Remove the room from the list
            selectedRooms.remove(selectedRoom);

            // Remove the guest assignment
            roomAssignments.remove(selectedRoom.getRoomID());

            // Update total guest count
            updateTotalGuestCount();

            // Refresh the available rooms list
            loadAvailableRooms();

        } catch (Exception e) {
            LoggingManager.logException("Error removing room", e);
            showError("Error removing room: " + e.getMessage());
        }
    }

    /**
     * Update the total guest count based on room assignments
     */
    private void updateTotalGuestCount() {
        int totalGuests = roomAssignments.values().stream().mapToInt(Integer::intValue).sum();
        spnNumberOfGuests.getValueFactory().setValue(totalGuests);
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
            handleRoomChanges();

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

        // Check if there are selected rooms
        if (selectedRooms.isEmpty()) {
            throw new ValidationException("Please select at least one room.");
        }

        // Validate room assignments
        int totalAssignedGuests = roomAssignments.values().stream().mapToInt(Integer::intValue).sum();
        if (totalAssignedGuests != spnNumberOfGuests.getValue()) {
            throw new ValidationException(
                    "Total guests in rooms (" + totalAssignedGuests +
                            ") must match the number of guests in the reservation (" +
                            spnNumberOfGuests.getValue() + ")."
            );
        }

        // Check if any room is over capacity
        for (Room room : selectedRooms) {
            int guestsInRoom = roomAssignments.getOrDefault(room.getRoomID(), 0);
            int maxCapacity = room.getRoomType().getMaxOccupancy();

            if (guestsInRoom > maxCapacity) {
                throw new ValidationException(
                        "Room #" + room.getRoomID() + " can only accommodate " +
                                maxCapacity + " guests but has " + guestsInRoom + " assigned."
                );
            }
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

        // Check if rooms have changed
        if (selectedRooms.size() != originalRooms.size()) {
            return true;
        }

        // Check if the same rooms are used
        Set<Integer> originalRoomIds = originalRooms.stream()
                .map(Room::getRoomID)
                .collect(Collectors.toSet());

        Set<Integer> selectedRoomIds = selectedRooms.stream()
                .map(Room::getRoomID)
                .collect(Collectors.toSet());

        if (!originalRoomIds.equals(selectedRoomIds)) {
            return true;
        }

        // Check if guest assignments have changed
        for (ReservationRoom rr : originalReservationRooms) {
            int originalGuests = rr.getGuestsInRoom();
            int newGuests = roomAssignments.getOrDefault(rr.getRoomID(), 0);

            if (originalGuests != newGuests) {
                return true;
            }
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

        // Set the primary roomID for backward compatibility
        if (!selectedRooms.isEmpty()) {
            modifiedReservation.setRoomID(selectedRooms.get(0).getRoomID());
        } else {
            modifiedReservation.setRoomID(originalReservation.getRoomID());
        }

        // Set modified values
        modifiedReservation.setCheckInDate(dpCheckInDate.getValue());
        modifiedReservation.setCheckOutDate(dpCheckOutDate.getValue());
        modifiedReservation.setNumberOfGuests(spnNumberOfGuests.getValue());

        return modifiedReservation;
    }

    /**
     * Handle room changes
     *
     * @throws DatabaseException    If there's an error updating room availability
     * @throws ReservationException If there's an error updating the reservation
     */
    private void handleRoomChanges() throws DatabaseException, ReservationException {
        // Get the reservation ID
        int reservationId = originalReservation.getReservationID();

        // Delete all existing room assignments
        reservationService.reservationRoomDAO.deleteByReservationId(reservationId);

        // Make all previously assigned rooms available again
        for (Room room : originalRooms) {
            roomService.setRoomAvailability(room.getRoomID(), true);
        }

        // Create new room assignments
        for (Room room : selectedRooms) {
            int roomId = room.getRoomID();
            int guestsInRoom = roomAssignments.getOrDefault(roomId, 0);

            // Create reservation-room relationship
            ReservationRoom rr = new ReservationRoom();
            rr.setReservationID(reservationId);
            rr.setRoomID(roomId);
            rr.setGuestsInRoom(guestsInRoom);
            rr.setPricePerNight(room.getPrice());

            reservationService.reservationRoomDAO.save(rr);

            // Make the room unavailable
            roomService.setRoomAvailability(roomId, false);
        }

        LoggingManager.logSystemInfo(
                "Updated room assignments for reservation #" + reservationId +
                        " with " + selectedRooms.size() + " rooms"
        );
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