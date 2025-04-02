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

    public int getKioskID() {
        return kioskID.get();
    }


    public String getLocation() {
        return location.get();
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    @Override
    public String toString() {
        return "Kiosk #" + getKioskID() + " - " + getLocation();
    }
}