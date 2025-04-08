package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

public class Room {
    private final IntegerProperty roomID = new SimpleIntegerProperty(this, "roomID");
    private final ObjectProperty<RoomType> roomType = new SimpleObjectProperty<>(this, "roomType");
    private final StringProperty roomNumber = new SimpleStringProperty(this, "roomNumber");
    private final IntegerProperty floor = new SimpleIntegerProperty(this, "floor");
    private final DoubleProperty price = new SimpleDoubleProperty(this, "price");
    private final BooleanProperty available = new SimpleBooleanProperty(this, "available");

    public Room() {
        // Default constructor
    }

    public Room(int roomID, RoomType roomType, String roomNumber, int floor, double price, boolean available) {
        this.roomID.set(roomID);
        this.roomType.set(roomType);
        this.roomNumber.set(roomNumber);
        this.floor.set(floor);
        this.price.set(price);
        this.available.set(available);
    }

    // Add getters and setters for roomNumber and floor
    public String getRoomNumber() {
        return roomNumber.get();
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber.set(roomNumber);
    }

    public void setFloor(int floor) {
        this.floor.set(floor);
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
        return "Room " + getRoomNumber() + " - " + getRoomType().getDisplayName() +
                " ($" + String.format("%.2f", getPrice()) + ")";
    }

    public int getFloor() {
        return floor.get();
    }
}