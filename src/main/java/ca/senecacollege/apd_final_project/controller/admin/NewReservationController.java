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
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class NewReservationController extends BaseController {

    public Label lblGuestCount;
    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblGuestId;

    @FXML
    private DatePicker dpCheckInDate;

    @FXML
    private DatePicker dpCheckOutDate;

    @FXML
    private Spinner<Integer> spnSingleRooms;

    @FXML
    private Spinner<Integer> spnDoubleRooms;

    @FXML
    private Spinner<Integer> spnDeluxeRooms;

    @FXML
    private Spinner<Integer> spnPenthouseRooms;

    @FXML
    private Spinner<Integer> spnNumberOfGuests;

    @FXML
    private Label lblTotalRooms;

    @FXML
    private Label lblTotalCapacity;

    @FXML
    private ListView<RoomSelectionItem> lvSingleRooms;

    @FXML
    private ListView<RoomSelectionItem> lvDoubleRooms;

    @FXML
    private ListView<RoomSelectionItem> lvDeluxeRooms;

    @FXML
    private ListView<RoomSelectionItem> lvPenthouseRooms;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    @FXML
    private Label lblError;

    // Services
    private GuestService guestService;
    private ReservationService reservationService;
    private RoomService roomService;

    // Guest info
    private Guest currentGuest;

    // Available rooms by type
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

        // Initialize services
        guestService = ServiceLocator.getService(GuestService.class);
        reservationService = ServiceLocator.getService(ReservationService.class);
        roomService = ServiceLocator.getService(RoomService.class);

        // Initialize the set of selected room IDs for each type
        selectedRoomIds.put(RoomType.SINGLE, new HashSet<>());
        selectedRoomIds.put(RoomType.DOUBLE, new HashSet<>());
        selectedRoomIds.put(RoomType.DELUXE, new HashSet<>());
        selectedRoomIds.put(RoomType.PENT_HOUSE, new HashSet<>());

        // Set up date pickers
        setupDatePickers();

        // Set up spinners
        setupSpinners();

        // Set up room list views with custom cell factories
        setupRoomListViews();

        // Add change listeners to update summary info
        addChangeListeners();

        // Hide error message initially
        hideError();

        LoggingManager.logSystemInfo("NewReservationController initialized");
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

    @Override
    public void initData(Admin admin) {
        super.initData(admin);
        LoggingManager.logSystemInfo("NewReservationController initialized with admin: " + admin.getUsername());
    }

    /**
     * Initialize with guest data
     * @param guestId The ID of the guest making the reservation
     */
    public void initGuest(int guestId) {
        try {
            // Load the guest information
            currentGuest = guestService.getGuestById(guestId);

            if (currentGuest != null) {
                // Display guest info
                lblGuestName.setText(currentGuest.getName());
                lblGuestId.setText(String.valueOf(currentGuest.getGuestID()));

                // Load initial available rooms
                loadAvailableRooms();

                LoggingManager.logSystemInfo("Loaded guest information for ID: " + guestId);
            } else {
                showError("Guest not found with ID: " + guestId);
                LoggingManager.logSystemWarning("Guest not found with ID: " + guestId);
            }

        } catch (DatabaseException e) {
            LoggingManager.logException("Error loading guest information", e);
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Unexpected error loading guest information", e);
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

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

        // Set default values
        dpCheckInDate.setValue(LocalDate.now());
        dpCheckOutDate.setValue(LocalDate.now().plusDays(1));

        // Add listener to update check-out date when check-in date changes
        dpCheckInDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // If check-out date is before new check-in date, update it
                if (dpCheckOutDate.getValue() == null ||
                        dpCheckOutDate.getValue().isBefore(newVal) ||
                        dpCheckOutDate.getValue().isEqual(newVal)) {
                    dpCheckOutDate.setValue(newVal.plusDays(1));
                }

                // Reload available rooms when dates change
                loadAvailableRooms();
            }
        });

        // Reload available rooms when check-out date changes
        dpCheckOutDate.valueProperty().addListener((obs, oldVal, newVal) -> loadAvailableRooms());
    }

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

        // Validate capacity
        validateGuestCapacity();
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

            // Load available rooms by type
            availableSingleRooms = roomService.getAllAvailableRoomsByType(
                    RoomType.SINGLE, checkInDate, checkOutDate);

            availableDoubleRooms = roomService.getAllAvailableRoomsByType(
                    RoomType.DOUBLE, checkInDate, checkOutDate);

            availableDeluxeRooms = roomService.getAllAvailableRoomsByType(
                    RoomType.DELUXE, checkInDate, checkOutDate);

            availablePenthouseRooms = roomService.getAllAvailableRoomsByType(
                    RoomType.PENT_HOUSE, checkInDate, checkOutDate);

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

            // Update spinner maximums based on available rooms
            updateSpinnerMaximums();

            // Suggest initial room allocation based on guest count
            suggestRoomAllocation(spnNumberOfGuests.getValue());

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
     * This method is called when room selections are being validated.
     * Unlike the previous version, it does NOT auto-select rooms,
     * but just checks if there are enough rooms selected.
     */
    private void validateRoomSelections() {
        // Get spinner values
        int singleCount = spnSingleRooms.getValue();
        int doubleCount = spnDoubleRooms.getValue();
        int deluxeCount = spnDeluxeRooms.getValue();
        int penthouseCount = spnPenthouseRooms.getValue();

        // Calculate total rooms and capacity

        // Check if enough capacity for the guests
        int guests = spnNumberOfGuests.getValue();
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
     * Validate guest capacity against selected rooms
     * Shows/hides error messages and enables/disables save button accordingly
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

        // All validations passed
        hideError();
        btnSave.setDisable(false);
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

        // Add listeners to update validation when values change
        spnSingleRooms.valueProperty().addListener((obs, oldVal, newVal) -> validateGuestCapacity());
        spnDoubleRooms.valueProperty().addListener((obs, oldVal, newVal) -> validateGuestCapacity());
        spnDeluxeRooms.valueProperty().addListener((obs, oldVal, newVal) -> validateGuestCapacity());
        spnPenthouseRooms.valueProperty().addListener((obs, oldVal, newVal) -> validateGuestCapacity());

        // When guest count changes, suggest room allocation and validate
        spnNumberOfGuests.valueProperty().addListener((obs, oldVal, newVal) -> {
            suggestRoomAllocation(newVal.intValue());
            validateGuestCapacity();
        });

        // Initial validation
        validateGuestCapacity();
    }

    /**
     * Suggest room allocation based on guest count
     * This provides an initial suggestion without auto-selecting rooms
     */
    private void suggestRoomAllocation(int guestCount) {
        // Only make suggestions when the screen is initialized
        // or when the guest count is increased
        SpinnerValueFactory<Integer> singleFactory =
                (SpinnerValueFactory<Integer>) spnSingleRooms.getValueFactory();
        SpinnerValueFactory<Integer> doubleFactory =
                (SpinnerValueFactory<Integer>) spnDoubleRooms.getValueFactory();
        SpinnerValueFactory<Integer> deluxeFactory =
                (SpinnerValueFactory<Integer>) spnDeluxeRooms.getValueFactory();
        SpinnerValueFactory<Integer> penthouseFactory =
                (SpinnerValueFactory<Integer>) spnPenthouseRooms.getValueFactory();

        // Reset suggestions
        int suggestedSingleRooms = 0;
        int suggestedDoubleRooms = 0;
        int suggestedDeluxeRooms = 0;
        int suggestedPenthouseRooms = 0;

        // Calculate optimal room allocation
        int remainingGuests = guestCount;

        // Start with double rooms for efficiency (4 guests per room)
        while (remainingGuests >= 4) {
            suggestedDoubleRooms++;
            remainingGuests -= 4;
        }

        // Then use single rooms for remaining guests (2 guests per room)
        while (remainingGuests > 0) {
            suggestedSingleRooms++;
            remainingGuests -= 2;
        }

        // Update spinners with suggestions
        singleFactory.setValue(suggestedSingleRooms);
        doubleFactory.setValue(suggestedDoubleRooms);
        deluxeFactory.setValue(suggestedDeluxeRooms);
        penthouseFactory.setValue(suggestedPenthouseRooms);

        // Show a helpful message about the suggestion
        showError("Room allocation suggested: " +
                suggestedDoubleRooms + " double rooms, " +
                suggestedSingleRooms + " single rooms. " +
                "You can adjust as needed.");
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

    @FXML
    private void handleSaveButton() {
        if (validateFields()) {
            try {
                // Collect reservation data
                int guestId = currentGuest.getGuestID();
                LocalDate checkInDate = dpCheckInDate.getValue();
                LocalDate checkOutDate = dpCheckOutDate.getValue();
                int numberOfGuests = spnNumberOfGuests.getValue();

                // Create a new reservation
                Reservation reservation = new Reservation();
                reservation.setGuestID(guestId);
                reservation.setCheckInDate(checkInDate);
                reservation.setCheckOutDate(checkOutDate);
                reservation.setNumberOfGuests(numberOfGuests);
                reservation.setStatus(ReservationStatus.CONFIRMED);

                // Create a list to store reservation-room relationships
                List<ReservationRoom> reservationRooms = new ArrayList<>();

                // Add the specific selected rooms
                addSelectedRooms(RoomType.SINGLE, reservationRooms);
                addSelectedRooms(RoomType.DOUBLE, reservationRooms);
                addSelectedRooms(RoomType.DELUXE, reservationRooms);
                addSelectedRooms(RoomType.PENT_HOUSE, reservationRooms);

                // Save the reservation and assign rooms
                int reservationId = reservationService.createReservationWithRooms(reservation, reservationRooms);

                // Show success message
                DialogService.showInformation(
                        getStage(),
                        "Reservation Created",
                        "Reservation #" + reservationId + " has been created successfully.");

                // Log the action
                logAdminActivity("Created reservation #" + reservationId +
                        " for guest #" + guestId + " (" + currentGuest.getName() + ")");

                // Close the window
                closeWindow();

            } catch (DatabaseException e) {
                LoggingManager.logException("Database error creating reservation", e);
                showError("Database error: " + e.getMessage());
            } catch (ReservationException e) {
                LoggingManager.logException("Reservation error", e);
                showError("Reservation error: " + e.getMessage());
            } catch (Exception e) {
                LoggingManager.logException("Unexpected error creating reservation", e);
                showError("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Add selected rooms of a specific type to the reservation rooms list
     */
    private void addSelectedRooms(RoomType roomType, List<ReservationRoom> reservationRooms)
            throws DatabaseException {
        // Get selected room IDs for this type
        Set<Integer> roomIds = selectedRoomIds.get(roomType);
        if (roomIds.isEmpty()) return;

        // Get the list of room items
        ObservableList<RoomSelectionItem> items = getRoomItemsByType(roomType);

        // Calculate guests per room - approximate assignment
        int maxCapacity = roomType.getMaxOccupancy();
        int remainingGuests = spnNumberOfGuests.getValue();

        // Calculate guests already assigned to other room types
        for (ReservationRoom rr : reservationRooms) {
            remainingGuests -= rr.getGuestsInRoom();
        }

        // Assign guests to the selected rooms
        int roomCount = roomIds.size();
        int guestsPerRoom = Math.min(maxCapacity, remainingGuests / roomCount);
        int extraGuests = remainingGuests % roomCount;

        // Track how many rooms we've processed
        int roomIndex = 0;

        // Find selected rooms and create reservation-room relationships
        for (RoomSelectionItem item : items) {
            if (item.isSelected()) {
                Room room = item.getRoom();

                // Calculate guests for this room
                int guestsInRoom = guestsPerRoom;
                if (roomIndex == 0 && extraGuests > 0) {
                    // Add any extra guests to the first room if it can handle them
                    int possibleExtra = Math.min(extraGuests, maxCapacity - guestsPerRoom);
                    guestsInRoom += possibleExtra;
                    extraGuests -= possibleExtra;
                }

                // Create reservation room
                ReservationRoom reservationRoom = new ReservationRoom();
                reservationRoom.setRoomID(room.getRoomID());
                reservationRoom.setGuestsInRoom(guestsInRoom);
                reservationRoom.setPricePerNight(room.getPrice());

                // Add to list
                reservationRooms.add(reservationRoom);

                // Deduct assigned guests
                remainingGuests -= guestsInRoom;
                roomIndex++;
            }
        }
    }

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

            // Check for future date
            if (ValidationUtils.isValidFutureDate(dpCheckInDate.getValue())) {
                showError("Check-in date must be today or in the future.");
                return false;
            }

            // Check if any rooms are selected
            int totalRooms = Integer.parseInt(lblTotalRooms.getText());
            if (totalRooms == 0) {
                showError("Please select at least one room.");
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

    @Override
    protected Stage getStage() {
        if (btnCancel != null && btnCancel.getScene() != null) {
            return (Stage) btnCancel.getScene().getWindow();
        }
        return null;
    }
}