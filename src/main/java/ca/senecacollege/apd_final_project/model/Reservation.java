package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a reservation in the hotel reservation system
 * This is an improved version that uses the ReservationStatus enum
 */
public class Reservation {
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final IntegerProperty guestID = new SimpleIntegerProperty(this, "guestID");
    private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty<>(this, "checkInDate");
    private final ObjectProperty<LocalDate> checkOutDate = new SimpleObjectProperty<>(this, "checkOutDate");
    private final IntegerProperty numberOfGuests = new SimpleIntegerProperty(this, "numberOfGuests");
    private final ObjectProperty<ReservationStatus> status = new SimpleObjectProperty<>(this, "status", ReservationStatus.CONFIRMED);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>(this, "createdAt");
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>(this, "updatedAt");

    /**
     * Default constructor
     */
    public Reservation() {
        // Default constructor
    }

    /**
     * Constructor with all fields
     */
    public Reservation(int reservationID, int guestID, LocalDate checkInDate,
                       LocalDate checkOutDate, int numberOfGuests, ReservationStatus status,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reservationID.set(reservationID);
        this.guestID.set(guestID);
        this.checkInDate.set(checkInDate);
        this.checkOutDate.set(checkOutDate);
        this.numberOfGuests.set(numberOfGuests);
        this.status.set(status);
        this.createdAt.set(createdAt);
        this.updatedAt.set(updatedAt);
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }

    public int getGuestID() {
        return guestID.get();
    }

    public void setGuestID(int guestID) {
        this.guestID.set(guestID);
    }

    public LocalDate getCheckInDate() {
        return checkInDate.get();
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate.set(checkInDate);
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate.get();
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate.set(checkOutDate);
    }

    public int getNumberOfGuests() {
        return numberOfGuests.get();
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests.set(numberOfGuests);
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
        return new SimpleStringProperty(getStatusDisplayName());
    }

    public void setStatus(ReservationStatus status) {
        this.status.set(status);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
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
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param numberOfGuests Number of guests
     */
    public void createReservation(int guestID, LocalDate checkInDate,
                                  LocalDate checkOutDate, int numberOfGuests) {
        setGuestID(guestID);
        setCheckInDate(checkInDate);
        setCheckOutDate(checkOutDate);
        setNumberOfGuests(numberOfGuests);
        setStatus(ReservationStatus.CONFIRMED);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Reservation #" + getReservationID() + " - " + getStatus();
    }
}