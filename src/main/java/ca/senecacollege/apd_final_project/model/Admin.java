package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Admin {
    private final IntegerProperty adminID = new SimpleIntegerProperty(this, "adminID");
    private final StringProperty username = new SimpleStringProperty(this, "username");
    private final StringProperty password = new SimpleStringProperty(this, "password");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty role = new SimpleStringProperty(this, "role");

    public Admin() {
        // Default constructor
    }

    public Admin(int adminID, String username, String password, String name, String role) {
        this.adminID.set(adminID);
        this.username.set(username);
        this.password.set(password);
        this.name.set(name);
        this.role.set(role);
    }

    public int getAdminID() {
        return adminID.get();
    }

    public void setAdminID(int adminID) {
        this.adminID.set(adminID);
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getRole() {
        return role.get();
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    // Methods
    public boolean login(String username, String password) {
        return this.username.get().equals(username) && this.password.get().equals(password);
    }

    @Override
    public String toString() {
        return getName() + " (" + getUsername() + ")";
    }
}