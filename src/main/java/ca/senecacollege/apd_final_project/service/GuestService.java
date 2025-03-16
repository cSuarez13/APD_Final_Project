package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.GuestDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;

import java.util.List;

public class GuestService {

    private GuestDAO guestDAO;

    public GuestService() {
        this.guestDAO = new GuestDAO();
    }

    /**
     * Save a new guest
     *
     * @param guest The guest to save
     * @return The guest ID
     * @throws DatabaseException If there's an error saving the guest
     */
    public int saveGuest(Guest guest) throws DatabaseException {
        try {
            validateGuest(guest);
            int guestId = guestDAO.save(guest);
            LoggingManager.logSystemInfo("Saved guest: " + guest.getName() + " with ID: " + guestId);
            return guestId;
        } catch (Exception e) {
            LoggingManager.logException("Error saving guest: " + guest.getName(), e);
            throw new DatabaseException("Error saving guest: " + e.getMessage(), e);
        }
    }

    /**
     * Get a guest by ID
     *
     * @param guestId The guest ID
     * @return The guest
     * @throws DatabaseException If there's an error retrieving the guest
     */
    public Guest getGuestById(int guestId) throws DatabaseException {
        try {
            return guestDAO.findById(guestId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving guest with ID: " + guestId, e);
            throw new DatabaseException("Error retrieving guest: " + e.getMessage(), e);
        }
    }

    /**
     * Update a guest
     *
     * @param guest The guest to update
     * @throws DatabaseException If there's an error updating the guest
     */
    public void updateGuest(Guest guest) throws DatabaseException {
        try {
            validateGuest(guest);
            guestDAO.update(guest);
            LoggingManager.logSystemInfo("Updated guest: " + guest.getName() + " with ID: " + guest.getGuestID());
        } catch (Exception e) {
            LoggingManager.logException("Error updating guest: " + guest.getName(), e);
            throw new DatabaseException("Error updating guest: " + e.getMessage(), e);
        }
    }

    /**
     * Search for guests by name
     *
     * @param name The name to search for
     * @return List of matching guests
     * @throws DatabaseException If there's an error searching for guests
     */
    public List<Guest> searchGuestsByName(String name) throws DatabaseException {
        try {
            return guestDAO.findByName(name);
        } catch (Exception e) {
            LoggingManager.logException("Error searching for guests by name: " + name, e);
            throw new DatabaseException("Error searching for guests: " + e.getMessage(), e);
        }
    }

    /**
     * Search for guests by phone number
     *
     * @param phoneNumber The phone number to search for
     * @return List of matching guests
     * @throws DatabaseException If there's an error searching for guests
     */
    public List<Guest> searchGuestsByPhone(String phoneNumber) throws DatabaseException {
        try {
            return guestDAO.findByPhone(phoneNumber);
        } catch (Exception e) {
            LoggingManager.logException("Error searching for guests by phone: " + phoneNumber, e);
            throw new DatabaseException("Error searching for guests: " + e.getMessage(), e);
        }
    }

    /**
     * Search for guests by email
     *
     * @param email The email to search for
     * @return List of matching guests
     * @throws DatabaseException If there's an error searching for guests
     */
    public List<Guest> searchGuestsByEmail(String email) throws DatabaseException {
        try {
            return guestDAO.findByEmail(email);
        } catch (Exception e) {
            LoggingManager.logException("Error searching for guests by email: " + email, e);
            throw new DatabaseException("Error searching for guests: " + e.getMessage(), e);
        }
    }

    /**
     * Get all guests
     *
     * @return List of all guests
     * @throws DatabaseException If there's an error retrieving guests
     */
    public List<Guest> getAllGuests() throws DatabaseException {
        try {
            return guestDAO.findAll();
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving all guests", e);
            throw new DatabaseException("Error retrieving all guests: " + e.getMessage(), e);
        }
    }

    /**
     * Validate guest data
     *
     * @param guest The guest to validate
     * @throws IllegalArgumentException If the guest data is invalid
     */
    private void validateGuest(Guest guest) throws IllegalArgumentException {
        // Name validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getName())) {
            throw new IllegalArgumentException("Guest name cannot be empty");
        }

        // Phone validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getPhoneNumber())) {
            throw new IllegalArgumentException("Guest phone number cannot be empty");
        }

        if (!ValidationUtils.isValidPhoneNumber(guest.getPhoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        // Email validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getEmail())) {
            throw new IllegalArgumentException("Guest email cannot be empty");
        }

        if (!ValidationUtils.isValidEmail(guest.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Address validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getAddress())) {
            throw new IllegalArgumentException("Guest address cannot be empty");
        }
    }

    /**
     * Check if a guest with the given email already exists
     *
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    public boolean isEmailExists(String email) {
        try {
            List<Guest> guests = searchGuestsByEmail(email);
            return !guests.isEmpty();
        } catch (Exception e) {
            LoggingManager.logException("Error checking if email exists: " + email, e);
            return false;
        }
    }

    /**
     * Check if a guest with the given phone number already exists
     *
     * @param phoneNumber The phone number to check
     * @return true if the phone number exists, false otherwise
     */
    public boolean isPhoneExists(String phoneNumber) {
        try {
            List<Guest> guests = searchGuestsByPhone(phoneNumber);
            return !guests.isEmpty();
        } catch (Exception e) {
            LoggingManager.logException("Error checking if phone exists: " + phoneNumber, e);
            return false;
        }
    }
}