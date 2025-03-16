package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final IntegerProperty guestID = new SimpleIntegerProperty(this, "guestID");
    private final IntegerProperty roomID = new SimpleIntegerProperty(this, "roomID");
    private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty<>(this, "checkInDate");
    private final ObjectProperty<LocalDate> checkOutDate = new SimpleObjectProperty<>(this, "checkOutDate");
    private final IntegerProperty numberOfGuests = new SimpleIntegerProperty(this, "numberOfGuests");
    private final StringProperty status = new SimpleStringProperty(this, "status");

    // Status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_CHECKED_IN = "Checked In";
    public static final String STATUS_CHECKED_OUT = "Checked Out";
    public static final String STATUS_CANCELLED = "Cancelled";

    public Reservation() {
        // Default constructor
    }

    public Reservation(int reservationID, int guestID, int roomID, LocalDate checkInDate,
                       LocalDate checkOutDate, int numberOfGuests, String status) {
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
    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    // Helper methods
    public int calculateNumberOfNights() {
        if (getCheckInDate() != null && getCheckOutDate() != null) {
            return (int) ChronoUnit.DAYS.between(getCheckInDate(), getCheckOutDate());
        }
        return 0;
    }

    public void createReservation(int guestID, int roomID, LocalDate checkInDate,
                                  LocalDate checkOutDate, int numberOfGuests) {
        setGuestID(guestID);
        setRoomID(roomID);
        setCheckInDate(checkInDate);
        setCheckOutDate(checkOutDate);
        setNumberOfGuests(numberOfGuests);
        setStatus(STATUS_PENDING);
    }

    public void cancelReservation() {
        setStatus(STATUS_CANCELLED);
    }

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

    public void confirmReservation() {
        setStatus(STATUS_CONFIRMED);
    }

    @Override
    public String toString() {
        return "Reservation #" + getReservationID() + " - " + getStatus();
    }
}