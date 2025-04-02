package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

public class Room {
    private final IntegerProperty roomID = new SimpleIntegerProperty(this, "roomID");
    private final ObjectProperty<RoomType> roomType = new SimpleObjectProperty<>(this, "roomType");
    private final IntegerProperty numberOfBeds = new SimpleIntegerProperty(this, "numberOfBeds");
    private final DoubleProperty price = new SimpleDoubleProperty(this, "price");
    private final BooleanProperty available = new SimpleBooleanProperty(this, "available");

    public Room() {
        // Default constructor
    }

    public Room(int roomID, RoomType roomType, int numberOfBeds, double price, boolean available) {
        this.roomID.set(roomID);
        this.roomType.set(roomType);
        this.numberOfBeds.set(numberOfBeds);
        this.price.set(price);
        this.available.set(available);
    }

    public int getRoomID() {
        return roomID.get();
    }

    public void setRoomID(int roomID) {
        this.roomID.set(roomID);
    }

    public RoomType getRoomType() {
        return roomType.get();
    }

    public void setRoomType(RoomType roomType) {
        this.roomType.set(roomType);
    }

    public void setNumberOfBeds(int numberOfBeds) {
        this.numberOfBeds.set(numberOfBeds);
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public boolean isAvailable() {
        return available.get();
    }

    public void setAvailable(boolean available) {
        this.available.set(available);
    }

    @Override
    public String toString() {
        return "Room " + getRoomID() + " - " + getRoomType().getDisplayName() +
                " ($" + String.format("%.2f", getPrice()) + ")";
    }
}