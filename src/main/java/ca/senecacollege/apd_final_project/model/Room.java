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

    // RoomType property
    public ObjectProperty<RoomType> roomTypeProperty() {
        return roomType;
    }

    public RoomType getRoomType() {
        return roomType.get();
    }

    public void setRoomType(RoomType roomType) {
        this.roomType.set(roomType);
    }

    // NumberOfBeds property
    public IntegerProperty numberOfBedsProperty() {
        return numberOfBeds;
    }

    public int getNumberOfBeds() {
        return numberOfBeds.get();
    }

    public void setNumberOfBeds(int numberOfBeds) {
        this.numberOfBeds.set(numberOfBeds);
    }

    // Price property
    public DoubleProperty priceProperty() {
        return price;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    // Available property
    public BooleanProperty availableProperty() {
        return available;
    }

    public boolean isAvailable() {
        return available.get();
    }

    public void setAvailable(boolean available) {
        this.available.set(available);
    }

    public String getRoomDetails() {
        return "Room #" + getRoomID() +
                "\nType: " + getRoomType().getDisplayName() +
                "\nBeds: " + getNumberOfBeds() +
                "\nPrice: $" + String.format("%.2f", getPrice()) +
                "\nStatus: " + (isAvailable() ? "Available" : "Occupied");
    }

    public void setRoomDetails(RoomType roomType, int numberOfBeds, double price) {
        setRoomType(roomType);
        setNumberOfBeds(numberOfBeds);
        setPrice(price);
    }

    public boolean checkRoomAvailability() {
        return isAvailable();
    }

    @Override
    public String toString() {
        return "Room " + getRoomID() + " - " + getRoomType().getDisplayName() +
                " ($" + String.format("%.2f", getPrice()) + ")";
    }
}