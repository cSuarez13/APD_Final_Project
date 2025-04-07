package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

/**
 * Represents the relationship between a reservation and a room.
 * This allows for multiple rooms to be booked under a single reservation.
 */
public class ReservationRoom {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final IntegerProperty roomID = new SimpleIntegerProperty(this, "roomID");
    private final IntegerProperty guestsInRoom = new SimpleIntegerProperty(this, "guestsInRoom");
    private final DoubleProperty pricePerNight = new SimpleDoubleProperty(this, "pricePerNight");

    // Add constructors
    public ReservationRoom() {
        // Default constructor
    }

    public ReservationRoom(int id, int reservationID, int roomID, int guestsInRoom, double pricePerNight) {
        this.id.set(id);
        this.reservationID.set(reservationID);
        this.roomID.set(roomID);
        this.guestsInRoom.set(guestsInRoom);
        this.pricePerNight.set(pricePerNight);
    }

    public ReservationRoom(int reservationID, int roomID, int guestsInRoom, double pricePerNight) {
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

    public double getPricePerNight() {
        return pricePerNight.get();
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight.set(pricePerNight);
    }

    public DoubleProperty pricePerNightProperty() {
        return pricePerNight;
    }

    @Override
    public String toString() {
        return "ReservationRoom{" +
                "id=" + getId() +
                ", reservationID=" + getReservationID() +
                ", roomID=" + getRoomID() +
                ", guestsInRoom=" + getGuestsInRoom() +
                ", pricePerNight=$" + String.format("%.2f", getPricePerNight()) +
                '}';
    }
}
