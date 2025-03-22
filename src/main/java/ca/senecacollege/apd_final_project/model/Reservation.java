package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a reservation in the hotel reservation system
 * This is an improved version that uses the ReservationStatus enum
 */
public class Reservation {
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final IntegerProperty guestID = new SimpleIntegerProperty(this, "guestID");
    private final IntegerProperty roomID = new SimpleIntegerProperty(this, "roomID");
    private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty<>(this, "checkInDate");
    private final ObjectProperty<LocalDate> checkOutDate = new SimpleObjectProperty<>(this, "checkOutDate");
    private final IntegerProperty numberOfGuests = new SimpleIntegerProperty(this, "numberOfGuests");
    private final ObjectProperty<ReservationStatus> status = new SimpleObjectProperty<>(this, "status");

    /**
     * Default constructor
     */
    public Reservation() {
        // Default constructor
    }

    /**
     * Constructor with all fields
     */
    public Reservation(int reservationID, int guestID, int roomID, LocalDate checkInDate,
                               LocalDate checkOutDate, int numberOfGuests, ReservationStatus status) {
        this.reservationID.set(reservationID);
        this.guestID.set(guestID);
        this.roomID.set(roomID);
        this.checkInDate.set(checkInDate);
        this.checkOutDate.set(checkOutDate);
        this.numberOfGuests.set(numberOfGuests);
        this.status.set(status);
    }

    // ReservationID property
    public IntegerProperty reservationIDProperty() {
        return reservationID;
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }

    // GuestID property
    public IntegerProperty guestIDProperty() {
        return guestID;
    }

    public int getGuestID() {
        return guestID.get();
    }

    public void setGuestID(int guestID) {
        this.guestID.set(guestID);
    }

    // RoomID property
    public IntegerProperty roomIDProperty() {
        return roomID;
    }

    public int getRoomID() {
        return roomID.get();
    }

    public void setRoomID(int roomID) {
        this.roomID.set(roomID);
    }

    // CheckInDate property
    public ObjectProperty<LocalDate> checkInDateProperty() {
        return checkInDate;
    }

    public LocalDate getCheckInDate() {
        return checkInDate.get();
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate.set(checkInDate);
    }

    // CheckOutDate property
    public ObjectProperty<LocalDate> checkOutDateProperty() {
        return checkOutDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate.get();
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate.set(checkOutDate);
    }

    // NumberOfGuests property
    public IntegerProperty numberOfGuestsProperty() {
        return numberOfGuests;
    }

    public int getNumberOfGuests() {
        return numberOfGuests.get();
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests.set(numberOfGuests);
    }

    // Status property
    public ObjectProperty<ReservationStatus> statusProperty() {
        return status;
    }

    public ReservationStatus getStatus() {
        return status.get();
    }

    /**
     * Get status display name for UI binding
     * @return The status display name
     */
    public String getStatusDisplayName() {
        ReservationStatus currentStatus = getStatus();
        return currentStatus != null ? currentStatus.getDisplayName() : "";
    }

    /**
     * String property for the status display name (for UI binding)
     * @return The status display name property
     */
    public StringProperty statusDisplayNameProperty() {
        StringProperty prop = new SimpleStringProperty();
        prop.bind(status.asString());
        return prop;
    }

    public void setStatus(ReservationStatus status) {
        this.status.set(status);
    }

    /**
     * Change the reservation status if the transition is valid
     * @param newStatus The new status
     * @throws IllegalStateException If the transition is not valid
     */
    public void changeStatus(ReservationStatus newStatus) throws IllegalStateException {
        ReservationStatus currentStatus = getStatus();
        if (currentStatus == null) {
            setStatus(newStatus);
        } else if (currentStatus.canTransitionTo(newStatus)) {
            setStatus(newStatus);
        } else {
            throw new IllegalStateException(
                    "Cannot transition from " + currentStatus + " to " + newStatus);
        }
    }

    // Helper methods
    /**
     * Calculate the number of nights for this reservation
     * @return The number of nights
     */
    public int calculateNumberOfNights() {
        if (getCheckInDate() != null && getCheckOutDate() != null) {
            return (int) ChronoUnit.DAYS.between(getCheckInDate(), getCheckOutDate());
        }
        return 0;
    }

    /**
     * Create a new reservation
     * @param guestID Guest ID
     * @param roomID Room ID
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param numberOfGuests Number of guests
     */
    public void createReservation(int guestID, int roomID, LocalDate checkInDate,
                                  LocalDate checkOutDate, int numberOfGuests) {
        setGuestID(guestID);
        setRoomID(roomID);
        setCheckInDate(checkInDate);
        setCheckOutDate(checkOutDate);
        setNumberOfGuests(numberOfGuests);
        setStatus(ReservationStatus.PENDING);
    }

    /**
     * Cancel the reservation
     * @throws IllegalStateException If the reservation cannot be cancelled
     */
    public void cancelReservation() throws IllegalStateException {
        changeStatus(ReservationStatus.CANCELLED);
    }

    /**
     * Confirm the reservation
     * @throws IllegalStateException If the reservation cannot be confirmed
     */
    public void confirmReservation() throws IllegalStateException {
        changeStatus(ReservationStatus.CONFIRMED);
    }

    /**
     * Check in the guest
     * @throws IllegalStateException If the guest cannot be checked in
     */
    public void checkIn() throws IllegalStateException {
        changeStatus(ReservationStatus.CHECKED_IN);
    }

    /**
     * Check out the guest
     * @throws IllegalStateException If the guest cannot be checked out
     */
    public void checkOut() throws IllegalStateException {
        changeStatus(ReservationStatus.CHECKED_OUT);
    }

    /**
     * Get a detailed representation of the reservation
     * @return Reservation details
     */
    public String getReservationDetails() {
        return "Reservation #" + getReservationID() +
                "\nGuest ID: " + getGuestID() +
                "\nRoom ID: " + getRoomID() +
                "\nCheck-in: " + getCheckInDate() +
                "\nCheck-out: " + getCheckOutDate() +
                "\nNumber of nights: " + calculateNumberOfNights() +
                "\nNumber of guests: " + getNumberOfGuests() +
                "\nStatus: " + getStatus();
    }

    @Override
    public String toString() {
        return "Reservation #" + getReservationID() + " - " + getStatus();
    }
}