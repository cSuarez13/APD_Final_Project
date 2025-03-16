package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Kiosk {
    private final IntegerProperty kioskID = new SimpleIntegerProperty(this, "kioskID");
    private final StringProperty location = new SimpleStringProperty(this, "location");
    private final StringProperty status = new SimpleStringProperty(this, "status");

    public Kiosk() {
        // Default constructor
    }

    public Kiosk(int kioskID, String location, String status) {
        this.kioskID.set(kioskID);
        this.location.set(location);
        this.status.set(status);
    }

    // KioskID property
    public IntegerProperty kioskIDProperty() {
        return kioskID;
    }

    public int getKioskID() {
        return kioskID.get();
    }

    public void setKioskID(int kioskID) {
        this.kioskID.set(kioskID);
    }

    // Location property
    public StringProperty locationProperty() {
        return location;
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
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

    // Methods
    public String displayWelcomeMessage() {
        return "Welcome to Hotel ABC! Please use this kiosk to make your reservation.";
    }

    public String guideBookingProcess() {
        return "Follow the simple steps to complete your booking:\n" +
                "1. Enter the number of guests\n" +
                "2. Select your check-in and check-out dates\n" +
                "3. Choose your room type\n" +
                "4. Enter your personal details\n" +
                "5. Confirm your booking";
    }

    public boolean validateInput(String input, String fieldName) {
        return input != null && !input.trim().isEmpty();
    }

    public String confirmBooking(Reservation reservation) {
        return "Your booking has been confirmed!\n" +
                "Reservation #" + reservation.getReservationID() + "\n" +
                "Check-in: " + reservation.getCheckInDate() + "\n" +
                "Check-out: " + reservation.getCheckOutDate() + "\n" +
                "Number of guests: " + reservation.getNumberOfGuests() + "\n" +
                "Please see the front desk for billing information.";
    }

    @Override
    public String toString() {
        return "Kiosk #" + getKioskID() + " - " + getLocation();
    }
}