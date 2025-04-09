package ca.senecacollege.apd_final_project.controller.admin;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ReservationException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.*;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    // Room Selection Fields - similar to NewReservationController
    @FXML
    private Spinner<Integer> spnSingleRooms;

    @FXML
    private Spinner<Integer> spnDoubleRooms;

    @FXML
    private Spinner<Integer> spnDeluxeRooms;

    @FXML
    private Spinner<Integer> spnPenthouseRooms;

    @FXML
    private ListView<RoomSelectionItem> lvSingleRooms;

    @FXML
    private ListView<RoomSelectionItem> lvDoubleRooms;

    @FXML
    private ListView<RoomSelectionItem> lvDeluxeRooms;

    @FXML
    private ListView<RoomSelectionItem> lvPenthouseRooms;

    @FXML
    private Label lblTotalRooms;

    @FXML
    private Label lblTotalCapacity;

    @FXML
    private Label lblGuestCount;

    // Services
    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    // Current data
    private Reservation originalReservation;
    private Guest currentGuest;
    private List<Room> originalRooms;
    private List<ReservationRoom> originalReservationRooms;

    // Available rooms by type (for selection)
    private List<Room> availableSingleRooms = new ArrayList<>();
    private List<Room> availableDoubleRooms = new ArrayList<>();
    private List<Room> availableDeluxeRooms = new ArrayList<>();
    private List<Room> availablePenthouseRooms = new ArrayList<>();

    // Observable lists for room selection lists
    private final ObservableList<RoomSelectionItem> singleRoomItems = FXCollections.observableArrayList();
    private final ObservableList<RoomSelectionItem> doubleRoomItems = FXCollections.observableArrayList();
    private final ObservableList<RoomSelectionItem> deluxeRoomItems = FXCollections.observableArrayList();
    private final ObservableList<RoomSelectionItem> penthouseRoomItems = FXCollections.observableArrayList();

    // Keep track of selected rooms
    private final Map<RoomType, Set<Integer>> selectedRoomIds = new HashMap<>();

    /**
     * Inner class to represent a room with checkbox for selection
     */
    private class RoomSelectionItem {
        private final Room room;
        private final CheckBox checkBox;

        public RoomSelectionItem(Room room) {
            this.room = room;
            this.checkBox = new CheckBox();

            // Listen for checkbox changes to update selection
            this.checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                RoomType type = room.getRoomType();
                Set<Integer> selectedIds = selectedRoomIds.computeIfAbsent(type, k -> new HashSet<>());

                if (newVal) {
                    // Selected - add to set
                    selectedIds.add(room.getRoomID());
                } else {
                    // Deselected - remove from set
                    selectedIds.remove(room.getRoomID());
                }

                // Validate room count against spinners
                validateRoomSelections();
            });
        }

        public Room getRoom() {
            return room;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public boolean isSelected() {
            return checkBox.isSelected();
        }

        public void setSelected(boolean selected) {
            checkBox.setSelected(selected);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Initialize services using ServiceLocator
        reservationService = ServiceLocator.getService(ReservationService.class);
        guestService = ServiceLocator.getService(GuestService.class);
        roomService = ServiceLocator.getService(RoomService.class);

        // Initialize the set of selected room IDs for each type
        selectedRoomIds.put(RoomType.SINGLE, new HashSet<>());
        selectedRoomIds.put(RoomType.DOUBLE, new HashSet<>());
        selectedRoomIds.put(RoomType.DELUXE, new HashSet<>());
        selectedRoomIds.put(RoomType.PENT_HOUSE, new HashSet<>());

        // Set up date pickers with constraints
        setupDatePickers();

        // Set up room spinners
        setupSpinners();

        // Set up room list views with custom cell factories
        setupRoomListViews();

        // Add change listeners to update summary info
        addChangeListeners();

        // Hide error message initially
        hideError();

        LoggingManager.logSystemInfo("ModifyReservationController initialized");
    }

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

            // Set values for editable fields
            dpCheckInDate.setValue(reservation.getCheckInDate());
            dpCheckOutDate.setValue(reservation.getCheckOutDate());
            spnNumberOfGuests.getValueFactory().setValue(reservation.getNumberOfGuests());

            // If there's a primary room, display it
            if (!originalRooms.isEmpty()) {
                Room primaryRoom = originalRooms.get(0);
                lblCurrentRoom.setText(primaryRoom.getRoomType().getDisplayName() +
                        " (Room #" + primaryRoom.getRoomID() + ")");
            } else {
                lblCurrentRoom.setText("No rooms assigned");
            }

            // Update room spinners based on current selection
            updateRoomSpinners();

            // Load available rooms based on dates
            loadAvailableRooms();

            // Pre-select the existing rooms
            preselectExistingRooms();

            // Update summary labels
            updateTotalCounts();

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
     * Update room spinners based on the original reservation's room selections
     */
    private void updateRoomSpinners() {
        // Count rooms by type in the original reservation
        int singleCount = 0;
        int doubleCount = 0;
        int deluxeCount = 0;
        int penthouseCount = 0;

        for (Room room : originalRooms) {
            switch (room.getRoomType()) {
                case SINGLE -> singleCount++;
                case DOUBLE -> doubleCount++;
                case DELUXE -> deluxeCount++;
                case PENT_HOUSE -> penthouseCount++;
            }
        }

        // Set spinner values
        spnSingleRooms.getValueFactory().setValue(singleCount);
        spnDoubleRooms.getValueFactory().setValue(doubleCount);
        spnDeluxeRooms.getValueFactory().setValue(deluxeCount);
        spnPenthouseRooms.getValueFactory().setValue(penthouseCount);
    }

    /**
     * Pre-select the rooms that are part of the original reservation
     */
    private void preselectExistingRooms() {
        // Create a set of original room IDs for quick lookup
        Set<Integer> originalRoomIds = new HashSet<>();
        for (Room room : originalRooms) {
            originalRoomIds.add(room.getRoomID());
        }

        // Pre-select rooms in each list view
        preselectRoomsInList(singleRoomItems, originalRoomIds);
        preselectRoomsInList(doubleRoomItems, originalRoomIds);
        preselectRoomsInList(deluxeRoomItems, originalRoomIds);
        preselectRoomsInList(penthouseRoomItems, originalRoomIds);

        // Update selected room IDs map
        for (Room room : originalRooms) {
            RoomType type = room.getRoomType();
            selectedRoomIds.get(type).add(room.getRoomID());
        }
    }

    /**
     * Pre-select rooms in a list view based on original room IDs
     */
    private void preselectRoomsInList(ObservableList<RoomSelectionItem> items, Set<Integer> originalRoomIds) {
        for (RoomSelectionItem item : items) {
            if (originalRoomIds.contains(item.getRoom().getRoomID())) {
                item.setSelected(true);
            }
        }
    }

    /**
     * Set up date pickers with constraints
     */
    private void setupDatePickers() {
        // Set min date to today
        dpCheckInDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Set min date for checkout to be after check-in date
        dpCheckOutDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = dpCheckInDate.getValue();
                setDisable(empty || (checkInDate != null && date.isBefore(checkInDate)));
            }
        });

        // Add listeners to update constraints when dates change
        dpCheckInDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            // If check-out date is now invalid, update it
            if (dpCheckOutDate.getValue() != null && dpCheckOutDate.getValue().isBefore(newVal)) {
                dpCheckOutDate.setValue(newVal.plusDays(1));
            }

            // Reload available rooms since date changed
            loadAvailableRooms();
        });

        dpCheckOutDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Reload available rooms since date changed
            loadAvailableRooms();
        });
    }

    /**
     * Set up the list views for room selection
     */
    private void setupRoomListViews() {
        // Set up custom cell factories for each list
        setupRoomListView(lvSingleRooms, singleRoomItems);
        setupRoomListView(lvDoubleRooms, doubleRoomItems);
        setupRoomListView(lvDeluxeRooms, deluxeRoomItems);
        setupRoomListView(lvPenthouseRooms, penthouseRoomItems);
    }

    /**
     * Set up a single room list view with a custom cell factory
     */
    private void setupRoomListView(ListView<RoomSelectionItem> listView, ObservableList<RoomSelectionItem> items) {
        // Set the items
        listView.setItems(items);

        // Set custom cell factory
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<RoomSelectionItem> call(ListView<RoomSelectionItem> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(RoomSelectionItem item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Room room = item.getRoom();
                            CheckBox checkBox = item.getCheckBox();

                            // Create HBox to hold the room info and checkbox
                            HBox hbox = new HBox(10);
                            hbox.setAlignment(Pos.CENTER_LEFT);

                            // Create label with room details
                            Label lblRoom = new Label(
                                    String.format("Room #%d - Floor: %d - $%.2f/night",
                                            room.getRoomID(),
                                            room.getFloor(),
                                            room.getPrice())
                            );

                            // Add components to HBox
                            hbox.getChildren().addAll(checkBox, lblRoom);

                            // Set the graphic
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    /**
     * Set up spinners and listeners for room selection and validation
     */
    private void setupSpinners() {
        // Set up room count spinners with min=0 and appropriate max values
        SpinnerValueFactory<Integer> singleFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
        spnSingleRooms.setValueFactory(singleFactory);

        SpinnerValueFactory<Integer> doubleFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
        spnDoubleRooms.setValueFactory(doubleFactory);

        SpinnerValueFactory<Integer> deluxeFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0);
        spnDeluxeRooms.setValueFactory(deluxeFactory);

        SpinnerValueFactory<Integer> penthouseFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2, 0);
        spnPenthouseRooms.setValueFactory(penthouseFactory);

        // Set up guest count spinner - default to 1 guest, max to 20
        SpinnerValueFactory<Integer> guestFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        spnNumberOfGuests.setValueFactory(guestFactory);
    }

    /**
     * Add change listeners to update validation when values change
     */
    private void addChangeListeners() {
        // Add change listeners to all spinners to update totals and validate selections
        spnSingleRooms.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalCounts();
            validateRoomSelections();
        });

        spnDoubleRooms.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalCounts();
            validateRoomSelections();
        });

        spnDeluxeRooms.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalCounts();
            validateRoomSelections();
        });

        spnPenthouseRooms.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalCounts();
            validateRoomSelections();
        });

        spnNumberOfGuests.valueProperty().addListener((obs, oldVal, newVal) -> validateGuestCapacity());
    }

    /**
     * Load available rooms based on current date selection
     */
    private void loadAvailableRooms() {
        if (dpCheckInDate.getValue() == null || dpCheckOutDate.getValue() == null) {
            return;
        }

        try {
            LocalDate checkInDate = dpCheckInDate.getValue();
            LocalDate checkOutDate = dpCheckOutDate.getValue();

            // Clear existing lists
            availableSingleRooms.clear();
            availableDoubleRooms.clear();
            availableDeluxeRooms.clear();
            availablePenthouseRooms.clear();

            // Clear existing items
            singleRoomItems.clear();
            doubleRoomItems.clear();
            deluxeRoomItems.clear();
            penthouseRoomItems.clear();

            // Load available rooms by type, excluding the current reservation
            availableSingleRooms = getAvailableRooms(RoomType.SINGLE, checkInDate, checkOutDate);
            availableDoubleRooms = getAvailableRooms(RoomType.DOUBLE, checkInDate, checkOutDate);
            availableDeluxeRooms = getAvailableRooms(RoomType.DELUXE, checkInDate, checkOutDate);
            availablePenthouseRooms = getAvailableRooms(RoomType.PENT_HOUSE, checkInDate, checkOutDate);

            LoggingManager.logSystemInfo("Loaded available rooms: " +
                    availableSingleRooms.size() + " single, " +
                    availableDoubleRooms.size() + " double, " +
                    availableDeluxeRooms.size() + " deluxe, " +
                    availablePenthouseRooms.size() + " penthouse");

            // Create and add items for each room
            for (Room room : availableSingleRooms) {
                singleRoomItems.add(new RoomSelectionItem(room));
            }

            for (Room room : availableDoubleRooms) {
                doubleRoomItems.add(new RoomSelectionItem(room));
            }

            for (Room room : availableDeluxeRooms) {
                deluxeRoomItems.add(new RoomSelectionItem(room));
            }

            for (Room room : availablePenthouseRooms) {
                penthouseRoomItems.add(new RoomSelectionItem(room));
            }

            // Add the original rooms if not already in the list
            addOriginalRoomsToLists();

            // Update spinner maximums based on available rooms
            updateSpinnerMaximums();

            // Validate the current selection
            validateGuestCapacity();

        } catch (DatabaseException e) {
            LoggingManager.logException("Error loading available rooms", e);
            showError("Database error loading available rooms: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error loading rooms", e);
            showError("Error loading available rooms: " + e.getMessage());
        }
    }

    /**
     * Get available rooms for a specific type, making sure to include rooms from the original reservation
     */
    private List<Room> getAvailableRooms(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) throws DatabaseException {
        // Get all available rooms for this type, excluding rooms from any reservation
        List<Room> availableRooms = roomService.getAllAvailableRoomsByType(roomType, checkInDate, checkOutDate);

        // Get original rooms of this type that should be included
        Set<Integer> originalRoomIds = new HashSet<>();
        for (Room room : originalRooms) {
            if (room.getRoomType() == roomType) {
                originalRoomIds.add(room.getRoomID());
            }
        }

        // Add original rooms of this type that aren't already in the available list
        for (int roomId : originalRoomIds) {
            boolean alreadyInList = availableRooms.stream().anyMatch(r -> r.getRoomID() == roomId);
            if (!alreadyInList) {
                try {
                    Room room = roomService.getRoomById(roomId);
                    if (room != null) {
                        availableRooms.add(room);
                    }
                } catch (DatabaseException e) {
                    LoggingManager.logException("Error getting room details", e);
                }
            }
        }

        return availableRooms;
    }

    /**
     * Make sure original rooms are included in the available room lists
     */
    private void addOriginalRoomsToLists() {
        if (originalRooms == null || originalRooms.isEmpty()) {
            return;
        }

        // Collect all room IDs already in the lists
        Set<Integer> singleIds = singleRoomItems.stream()
                .map(item -> item.getRoom().getRoomID())
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        Set<Integer> doubleIds = doubleRoomItems.stream()
                .map(item -> item.getRoom().getRoomID())
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        Set<Integer> deluxeIds = deluxeRoomItems.stream()
                .map(item -> item.getRoom().getRoomID())
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        Set<Integer> penthouseIds = penthouseRoomItems.stream()
                .map(item -> item.getRoom().getRoomID())
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        // Add any original rooms that aren't already in the lists
        for (Room room : originalRooms) {
            switch (room.getRoomType()) {
                case SINGLE -> {
                    if (!singleIds.contains(room.getRoomID())) {
                        singleRoomItems.add(new RoomSelectionItem(room));
                    }
                }
                case DOUBLE -> {
                    if (!doubleIds.contains(room.getRoomID())) {
                        doubleRoomItems.add(new RoomSelectionItem(room));
                    }
                }
                case DELUXE -> {
                    if (!deluxeIds.contains(room.getRoomID())) {
                        deluxeRoomItems.add(new RoomSelectionItem(room));
                    }
                }
                case PENT_HOUSE -> {
                    if (!penthouseIds.contains(room.getRoomID())) {
                        penthouseRoomItems.add(new RoomSelectionItem(room));
                    }
                }
            }
        }
    }

    /**
     * Update spinner maximum values based on available rooms
     */
    private void updateSpinnerMaximums() {
        // Update single rooms spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory singleFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spnSingleRooms.getValueFactory();
        singleFactory.setMax(Math.max(singleRoomItems.size(), singleFactory.getValue()));

        // Update double rooms spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory doubleFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spnDoubleRooms.getValueFactory();
        doubleFactory.setMax(Math.max(doubleRoomItems.size(), doubleFactory.getValue()));

        // Update deluxe rooms spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory deluxeFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spnDeluxeRooms.getValueFactory();
        deluxeFactory.setMax(Math.max(deluxeRoomItems.size(), deluxeFactory.getValue()));

        // Update penthouse rooms spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory penthouseFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spnPenthouseRooms.getValueFactory();
        penthouseFactory.setMax(Math.max(penthouseRoomItems.size(), penthouseFactory.getValue()));
    }

    /**
     * Update total counts and validate room selections
     */
    private void updateTotalCounts() {
        // Calculate total rooms
        int totalRooms = spnSingleRooms.getValue() +
                spnDoubleRooms.getValue() +
                spnDeluxeRooms.getValue() +
                spnPenthouseRooms.getValue();

        // Calculate total capacity
        int totalCapacity = (spnSingleRooms.getValue() * RoomType.SINGLE.getMaxOccupancy()) +
                (spnDoubleRooms.getValue() * RoomType.DOUBLE.getMaxOccupancy()) +
                (spnDeluxeRooms.getValue() * RoomType.DELUXE.getMaxOccupancy()) +
                (spnPenthouseRooms.getValue() * RoomType.PENT_HOUSE.getMaxOccupancy());

        // Update labels
        lblTotalRooms.setText(String.valueOf(totalRooms));
        lblTotalCapacity.setText(String.valueOf(totalCapacity));
        lblGuestCount.setText(String.valueOf(spnNumberOfGuests.getValue()));

        // Validate capacity
        validateGuestCapacity();
    }

    /**
     * This method is called when room selections are being validated.
     * It checks if the exact number of rooms specified in the spinners
     * have been selected for each room type.
     */
    private void validateRoomSelections() {
        // Get spinner values (target number of rooms for each type)
        int singleCount = spnSingleRooms.getValue();
        int doubleCount = spnDoubleRooms.getValue();
        int deluxeCount = spnDeluxeRooms.getValue();
        int penthouseCount = spnPenthouseRooms.getValue();

        // Get actual selected room counts
        int selectedSingleCount = selectedRoomIds.get(RoomType.SINGLE).size();
        int selectedDoubleCount = selectedRoomIds.get(RoomType.DOUBLE).size();
        int selectedDeluxeCount = selectedRoomIds.get(RoomType.DELUXE).size();
        int selectedPenthouseCount = selectedRoomIds.get(RoomType.PENT_HOUSE).size();

        // Check if the number of selected rooms matches the spinner values
        boolean roomSelectionValid =
                (singleCount == selectedSingleCount) &&
                        (doubleCount == selectedDoubleCount) &&
                        (deluxeCount == selectedDeluxeCount) &&
                        (penthouseCount == selectedPenthouseCount);

        // If room selection is not valid, show error message
        if (!roomSelectionValid) {
            StringBuilder errorMsg = new StringBuilder("Please select exactly: ");
            List<String> mismatches = new ArrayList<>();

            if (singleCount != selectedSingleCount) {
                mismatches.add(singleCount + " single rooms (selected: " + selectedSingleCount + ")");
            }
            if (doubleCount != selectedDoubleCount) {
                mismatches.add(doubleCount + " double rooms (selected: " + selectedDoubleCount + ")");
            }
            if (deluxeCount != selectedDeluxeCount) {
                mismatches.add(deluxeCount + " deluxe rooms (selected: " + selectedDeluxeCount + ")");
            }
            if (penthouseCount != selectedPenthouseCount) {
                mismatches.add(penthouseCount + " penthouse rooms (selected: " + selectedPenthouseCount + ")");
            }

            errorMsg.append(String.join(", ", mismatches));
            showError(errorMsg.toString());
            btnSave.setDisable(true);
        } else {
            checkAllValidations();
        }
    }

    /**
     * Check all validations and enable/disable save button accordingly
     */
    private void checkAllValidations() {
        try {
            // Check if guest is selected
            if (currentGuest == null) {
                showError("No guest selected for the reservation.");
                btnSave.setDisable(true);
                return;
            }

            // Check dates
            if (dpCheckInDate.getValue() == null) {
                showError("Please select a check-in date.");
                btnSave.setDisable(true);
                return;
            }

            if (dpCheckOutDate.getValue() == null) {
                showError("Please select a check-out date.");
                btnSave.setDisable(true);
                return;
            }

            // Validate date range
            ValidationUtils.validateDateRange(
                    dpCheckInDate.getValue(),
                    dpCheckOutDate.getValue(),
                    "Check-in date",
                    "Check-out date");

            // Check for future date - only for new check-in dates
            if (!dpCheckInDate.getValue().equals(originalReservation.getCheckInDate()) &&
                    ValidationUtils.isValidFutureDate(dpCheckInDate.getValue())) {
                showError("Check-in date must be today or in the future.");
                btnSave.setDisable(true);
                return;
            }

            // Check if any rooms are selected
            int totalRooms = Integer.parseInt(lblTotalRooms.getText());
            if (totalRooms == 0) {
                showError("Please select at least one room.");
                btnSave.setDisable(true);
                return;
            }

            // Check if selected rooms have enough capacity
            int capacity = Integer.parseInt(lblTotalCapacity.getText());
            int guests = spnNumberOfGuests.getValue();

            if (capacity < guests) {
                showError("Selected rooms cannot accommodate " + guests + " guests.");
                btnSave.setDisable(true);
                return;
            }

            hideError();
            btnSave.setDisable(false);

        } catch (ValidationException e) {
            showError(e.getMessage());
            btnSave.setDisable(true);
        } catch (Exception e) {
            showError("Validation error: " + e.getMessage());
            btnSave.setDisable(true);
        }
    }

    /**
     * Validate guest capacity against selected rooms
     */
    private void validateGuestCapacity() {
        int guests = spnNumberOfGuests.getValue();
        int totalRooms = spnSingleRooms.getValue() + spnDoubleRooms.getValue() +
                spnDeluxeRooms.getValue() + spnPenthouseRooms.getValue();

        // Calculate total capacity based on room types
        int capacity = (spnSingleRooms.getValue() * RoomType.SINGLE.getMaxOccupancy()) +
                (spnDoubleRooms.getValue() * RoomType.DOUBLE.getMaxOccupancy()) +
                (spnDeluxeRooms.getValue() * RoomType.DELUXE.getMaxOccupancy()) +
                (spnPenthouseRooms.getValue() * RoomType.PENT_HOUSE.getMaxOccupancy());

        // Update the labels
        lblTotalRooms.setText(String.valueOf(totalRooms));
        lblTotalCapacity.setText(String.valueOf(capacity));
        lblGuestCount.setText(String.valueOf(guests));

        // Apply validation rules
        if (totalRooms == 0) {
            showError("Please select at least one room.");
            btnSave.setDisable(true);
            return;
        }

        if (capacity < guests) {
            showError("Selected rooms cannot accommodate " + guests + " guests.\n" +
                    "Total capacity: " + capacity + ". You need room(s) for " + (guests - capacity) + " more guest(s).");
            btnSave.setDisable(true);
            return;
        }

        // Check if room selections match spinner values
        validateRoomSelections();
    }

    /**
     * Get the room items for a specific room type
     */
    private ObservableList<RoomSelectionItem> getRoomItemsByType(RoomType roomType) {
        return switch (roomType) {
            case SINGLE -> singleRoomItems;
            case DOUBLE -> doubleRoomItems;
            case DELUXE -> deluxeRoomItems;
            case PENT_HOUSE -> penthouseRoomItems;
        };
    }

    /**
     * Handle save button click
     */
    @FXML
    private void handleSaveButton() {
        if (validateFields()) {
            try {
                // Create a modified reservation based on the original one
                Reservation modifiedReservation = new Reservation();
                modifiedReservation.setReservationID(originalReservation.getReservationID());
                modifiedReservation.setGuestID(originalReservation.getGuestID());
                modifiedReservation.setStatus(originalReservation.getStatus());

                // Copy timestamps if they exist in the original reservation
                if (originalReservation.getCreatedAt() != null) {
                    modifiedReservation.setCreatedAt(originalReservation.getCreatedAt());
                } else {
                    modifiedReservation.setCreatedAt(LocalDateTime.now());
                }

                // Update the updatedAt timestamp
                modifiedReservation.setUpdatedAt(LocalDateTime.now());

                // Set the updated fields
                modifiedReservation.setCheckInDate(dpCheckInDate.getValue());
                modifiedReservation.setCheckOutDate(dpCheckOutDate.getValue());
                modifiedReservation.setNumberOfGuests(spnNumberOfGuests.getValue());

                // Update the reservation in the database
                reservationService.updateReservation(modifiedReservation);

                // Handle room changes - delete old assignments and create new ones
                handleRoomChanges(modifiedReservation.getReservationID());

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

            } catch (DatabaseException e) {
                LoggingManager.logException("Database error updating reservation", e);
                showError("Database error: " + e.getMessage());
            } catch (Exception e) {
                LoggingManager.logException("Error updating reservation", e);
                showError("Error updating reservation: " + e.getMessage());
            }
        }
    }

    /**
     * Handle room changes - delete old assignments and create new ones
     */
    private void handleRoomChanges(int reservationId) throws DatabaseException, ReservationException {
        // Delete all existing room assignments
        reservationService.reservationRoomDAO.deleteByReservationId(reservationId);

        // Make all previously assigned rooms available again
        for (Room room : originalRooms) {
            roomService.setRoomAvailability(room.getRoomID(), true);
        }

        // Get all the selected room IDs
        List<Integer> selectedSingleRoomIds = new ArrayList<>(selectedRoomIds.get(RoomType.SINGLE));
        List<Integer> selectedDoubleRoomIds = new ArrayList<>(selectedRoomIds.get(RoomType.DOUBLE));
        List<Integer> selectedDeluxeRoomIds = new ArrayList<>(selectedRoomIds.get(RoomType.DELUXE));
        List<Integer> selectedPenthouseRoomIds = new ArrayList<>(selectedRoomIds.get(RoomType.PENT_HOUSE));

        // Create new room assignments for all selected rooms
        createRoomAssignments(reservationId, RoomType.SINGLE, selectedSingleRoomIds);
        createRoomAssignments(reservationId, RoomType.DOUBLE, selectedDoubleRoomIds);
        createRoomAssignments(reservationId, RoomType.DELUXE, selectedDeluxeRoomIds);
        createRoomAssignments(reservationId, RoomType.PENT_HOUSE, selectedPenthouseRoomIds);
    }

    /**
     * Create room assignments for a specific room type
     */
    private void createRoomAssignments(int reservationId, RoomType roomType, List<Integer> roomIds)
            throws DatabaseException {
        if (roomIds.isEmpty()) {
            return;
        }

        int guestsRemaining = spnNumberOfGuests.getValue();
        int maxCapacity = roomType.getMaxOccupancy();
        int roomCount = roomIds.size();

        // Calculate guests per room
        int guestsPerRoom = Math.min(maxCapacity, guestsRemaining / roomCount);
        int extraGuests = guestsRemaining % roomCount;

        for (int i = 0; i < roomIds.size(); i++) {
            int roomId = roomIds.get(i);
            Room room = roomService.getRoomById(roomId);

            // Calculate guests for this specific room
            int guestsInRoom = guestsPerRoom;
            if (i == 0 && extraGuests > 0) {
                // Add any extra guests to the first room
                int possibleExtra = Math.min(extraGuests, maxCapacity - guestsPerRoom);
                guestsInRoom += possibleExtra;
                extraGuests -= possibleExtra;
            }

            // Make sure we don't exceed capacity
            guestsInRoom = Math.min(guestsInRoom, maxCapacity);

            // Create a reservation-room relationship
            ReservationRoom rr = new ReservationRoom();
            rr.setReservationID(reservationId);
            rr.setRoomID(roomId);
            rr.setGuestsInRoom(guestsInRoom);
            rr.setPricePerNight(room.getPrice());

            // Save the relationship
            reservationService.reservationRoomDAO.save(rr);

            // Make the room unavailable
            roomService.setRoomAvailability(roomId, false);
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
    protected boolean validateFields() {
        try {
            // Check if guest is selected
            if (currentGuest == null) {
                showError("No guest selected for the reservation.");
                return false;
            }

            // Check dates
            if (dpCheckInDate.getValue() == null) {
                showError("Please select a check-in date.");
                return false;
            }

            if (dpCheckOutDate.getValue() == null) {
                showError("Please select a check-out date.");
                return false;
            }

            // Validate date range
            ValidationUtils.validateDateRange(
                    dpCheckInDate.getValue(),
                    dpCheckOutDate.getValue(),
                    "Check-in date",
                    "Check-out date");

            // Check for future date - only for new check-in dates
            if (!dpCheckInDate.getValue().equals(originalReservation.getCheckInDate()) &&
                    ValidationUtils.isValidFutureDate(dpCheckInDate.getValue())) {
                showError("Check-in date must be today or in the future.");
                return false;
            }

            // Check if any rooms are selected
            int totalRooms = Integer.parseInt(lblTotalRooms.getText());
            if (totalRooms == 0) {
                showError("Please select at least one room.");
                return false;
            }

            // Check that selected rooms match spinner values
            boolean roomSelectionValid = validateRoomSelectionCounts();
            if (!roomSelectionValid) {
                return false;
            }

            // Check if selected rooms have enough capacity
            int capacity = Integer.parseInt(lblTotalCapacity.getText());
            int guests = spnNumberOfGuests.getValue();

            if (capacity < guests) {
                showError("Selected rooms cannot accommodate " + guests + " guests.");
                return false;
            }

            return true;

        } catch (ValidationException e) {
            showError(e.getMessage());
            return false;
        } catch (Exception e) {
            showError("Validation error: " + e.getMessage());
            return false;
        }
    }

    // Validate that selected room counts match spinner values
    private boolean validateRoomSelectionCounts() {
        // Get spinner values (requested room counts)
        int requestedSingle = spnSingleRooms.getValue();
        int requestedDouble = spnDoubleRooms.getValue();
        int requestedDeluxe = spnDeluxeRooms.getValue();
        int requestedPenthouse = spnPenthouseRooms.getValue();

        // Get actual selected counts
        int selectedSingle = selectedRoomIds.get(RoomType.SINGLE).size();
        int selectedDouble = selectedRoomIds.get(RoomType.DOUBLE).size();
        int selectedDeluxe = selectedRoomIds.get(RoomType.DELUXE).size();
        int selectedPenthouse = selectedRoomIds.get(RoomType.PENT_HOUSE).size();

        // Check for mismatches
        List<String> mismatches = new ArrayList<>();

        if (requestedSingle > 0 && selectedSingle != requestedSingle) {
            mismatches.add("Single: " + selectedSingle + " selected (need " + requestedSingle + ")");
        }
        if (requestedDouble > 0 && selectedDouble != requestedDouble) {
            mismatches.add("Double: " + selectedDouble + " selected (need " + requestedDouble + ")");
        }
        if (requestedDeluxe > 0 && selectedDeluxe != requestedDeluxe) {
            mismatches.add("Deluxe: " + selectedDeluxe + " selected (need " + requestedDeluxe + ")");
        }
        if (requestedPenthouse > 0 && selectedPenthouse != requestedPenthouse) {
            mismatches.add("Penthouse: " + selectedPenthouse + " selected (need " + requestedPenthouse + ")");
        }

        if (!mismatches.isEmpty()) {
            showError("Room selection mismatch:\n" + String.join("\n", mismatches));
            return false;
        }

        return true;
    }

    /**
     * Show error message with improved visibility
     */
    @Override
    protected void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setStyle("-fx-text-fill: #cf6679; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }

    /**
     * Hide the error message
     */
    @Override
    protected void hideError() {
        if (lblError != null) {
            lblError.setVisible(false);
        }
    }

    @Override
    protected Stage getStage() {
        if (btnCancel != null && btnCancel.getScene() != null) {
            return (Stage) btnCancel.getScene().getWindow();
        }
        return null;
    }
}