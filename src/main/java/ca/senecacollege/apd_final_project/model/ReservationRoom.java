package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;

/**
 * Represents the relationship between a reservation and a room.
 * This allows for multiple rooms to be booked under a single reservation.
 */
public class ReservationRoom {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final IntegerProperty roomID = new SimpleIntegerProperty(this, "roomID");
    private final IntegerProperty guestsInRoom = new SimpleIntegerProperty(this, "guestsInRoom");
    private final ObjectProperty<BigDecimal> pricePerNight = new SimpleObjectProperty<>(this, "pricePerNight", BigDecimal.ZERO);

    // Default constructor
    public ReservationRoom() {
        // Default constructor
    }

    // Constructor with all fields
    public ReservationRoom(int id, int reservationID, int roomID, int guestsInRoom, BigDecimal pricePerNight) {
        this.id.set(id);
        this.reservationID.set(reservationID);
        this.roomID.set(roomID);
        this.guestsInRoom.set(guestsInRoom);
        this.pricePerNight.set(pricePerNight);
    }

    // Constructor without ID (for new records)
    public ReservationRoom(int reservationID, int roomID, int guestsInRoom, BigDecimal pricePerNight) {
        this.reservationID.set(reservationID);
        this.roomID.set(roomID);
        this.guestsInRoom.set(guestsInRoom);
        this.pricePerNight.set(pricePerNight);
    }

    // Getter and Setter for ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Getter and Setter for ReservationID
    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }

    public IntegerProperty reservationIDProperty() {
        return reservationID;
    }

    // Getter and Setter for RoomID
    public int getRoomID() {
        return roomID.get();
    }

    public void setRoomID(int roomID) {
        this.roomID.set(roomID);
    }

    public IntegerProperty roomIDProperty() {
        return roomID;
    }

    // Getter and Setter for Guests in Room
    public int getGuestsInRoom() {
        return guestsInRoom.get();
    }

    public void setGuestsInRoom(int guestsInRoom) {
        this.guestsInRoom.set(guestsInRoom);
    }

    public IntegerProperty guestsInRoomProperty() {
        return guestsInRoom;
    }

    // Getter and Setter for Price per Night
    public BigDecimal getPricePerNight() {
        return pricePerNight.get();
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight.set(pricePerNight);
    }

    public ObjectProperty<BigDecimal> pricePerNightProperty() {
        return pricePerNight;
    }

    @Override
    public String toString() {
        return "ReservationRoom{" +
                "id=" + getId() +
                ", reservationID=" + getReservationID() +
                ", roomID=" + getRoomID() +
                ", guestsInRoom=" + getGuestsInRoom() +
                ", pricePerNight=" + getPricePerNight() +
                '}';
    }
}
