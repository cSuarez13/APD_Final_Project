package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Guest {
    private final IntegerProperty guestID = new SimpleIntegerProperty(this, "guestID");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty phoneNumber = new SimpleStringProperty(this, "phoneNumber");
    private final StringProperty email = new SimpleStringProperty(this, "email");
    private final StringProperty address = new SimpleStringProperty(this, "address");
    private final StringProperty feedback = new SimpleStringProperty(this, "feedback");

    public Guest() {
    }

    public Guest(int guestID, String name, String phoneNumber, String email, String address) {
        this.guestID.set(guestID);
        this.name.set(name);
        this.phoneNumber.set(phoneNumber);
        this.email.set(email);
        this.address.set(address);
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

    // Name property
    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    // PhoneNumber property
    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    // Email property
    public StringProperty emailProperty() {
        return email;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    // Address property
    public StringProperty addressProperty() {
        return address;
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    // Feedback property
    public StringProperty feedbackProperty() {
        return feedback;
    }

    public String getFeedback() {
        return feedback.get();
    }

    public void setFeedback(String feedback) {
        this.feedback.set(feedback);
    }

    public String getGuestDetails() {
        return "Guest ID: " + getGuestID() +
                "\nName: " + getName() +
                "\nPhone: " + getPhoneNumber() +
                "\nEmail: " + getEmail() +
                "\nAddress: " + getAddress();
    }

    public void setGuestDetails(String name, String phoneNumber, String email, String address) {
        setName(name);
        setPhoneNumber(phoneNumber);
        setEmail(email);
        setAddress(address);
    }

    public boolean validateGuestDetails() {
        // Basic validation
        return getName() != null && !getName().isEmpty() &&
                getPhoneNumber() != null && !getPhoneNumber().isEmpty() &&
                getEmail() != null && !getEmail().isEmpty() &&
                getAddress() != null && !getAddress().isEmpty();
    }

    @Override
    public String toString() {
        return getName();
    }
}