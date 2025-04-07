package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents the relationship between a reservation and a room.
 * This allows for multiple rooms to be booked under a single reservation.
 */
public class ReservationRoom {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final IntegerProperty roomID = new SimpleIntegerProperty(this, "roomID");
    private final IntegerProperty guestsInRoom = new SimpleIntegerProperty(this, "guestsInRoom");

    /**
     * Default constructor
     */
    public ReservationRoom() {
        // Default constructor
    }

    /**
     * Constructor with all fields
     */
    public ReservationRoom(int id, int reservationID, int roomID, int guestsInRoom) {
        this.id.set(id);
        this.reservationID.set(reservationID);
        this.roomID.set(roomID);
        this.guestsInRoom.set(guestsInRoom);
    }

    /**
     * Constructor without ID (for new records)
     */
    public ReservationRoom(int reservationID, int roomID, int guestsInRoom) {
        this.reservationID.set(reservationID);
        this.roomID.set(roomID);
        this.guestsInRoom.set(guestsInRoom);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }

    public IntegerProperty reservationIDProperty() {
        return reservationID;
    }

    public int getRoomID() {
        return roomID.get();
    }

    public void setRoomID(int roomID) {
        this.roomID.set(roomID);
    }

    public IntegerProperty roomIDProperty() {
        return roomID;
    }

    public int getGuestsInRoom() {
        return guestsInRoom.get();
    }

    public void setGuestsInRoom(int guestsInRoom) {
        this.guestsInRoom.set(guestsInRoom);
    }

    public IntegerProperty guestsInRoomProperty() {
        return guestsInRoom;
    }

    @Override
    public String toString() {
        return "ReservationRoom{" +
                "id=" + getId() +
                ", reservationID=" + getReservationID() +
                ", roomID=" + getRoomID() +
                ", guestsInRoom=" + getGuestsInRoom() +
                '}';
    }
}