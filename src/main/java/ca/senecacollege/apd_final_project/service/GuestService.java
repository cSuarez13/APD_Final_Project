package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.GuestDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ValidationUtils;

import java.util.List;

public class GuestService {

    private final GuestDAO guestDAO;

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
     * Validate guest data
     *
     * @param guest The guest to validate
     * @throws IllegalArgumentException If the guest data is invalid
     */
    private void validateGuest(Guest guest) throws IllegalArgumentException {
        // Name validation
        if (ValidationUtils.isNotNullOrEmpty(guest.getName())) {
            throw new IllegalArgumentException("Guest name cannot be empty");
        }

        // Phone validation
        if (ValidationUtils.isNotNullOrEmpty(guest.getPhoneNumber())) {
            throw new IllegalArgumentException("Guest phone number cannot be empty");
        }

        if (ValidationUtils.isValidPhoneNumber(guest.getPhoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        // Email validation
        if (ValidationUtils.isNotNullOrEmpty(guest.getEmail())) {
            throw new IllegalArgumentException("Guest email cannot be empty");
        }

        if (ValidationUtils.isValidEmail(guest.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Address validation
        if (ValidationUtils.isNotNullOrEmpty(guest.getAddress())) {
            throw new IllegalArgumentException("Guest address cannot be empty");
        }
    }

    /**
     * Update a guest's feedback
     *
     * @param guestId The guest ID
     * @param feedback The feedback text
     * @throws DatabaseException If there's an error updating the feedback
     */
    public void updateGuestFeedback(int guestId, String feedback) throws DatabaseException {
        try {
            guestDAO.updateFeedback(guestId, feedback);
            LoggingManager.logSystemInfo("Updated feedback for guest ID: " + guestId);
        } catch (Exception e) {
            LoggingManager.logException("Error updating feedback for guest ID: " + guestId, e);
            throw new DatabaseException("Error updating guest feedback: " + e.getMessage(), e);
        }
    }
}