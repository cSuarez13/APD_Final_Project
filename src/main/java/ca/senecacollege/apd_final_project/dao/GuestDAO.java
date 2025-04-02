package ca.senecacollege.apd_final_project.dao;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.util.DatabaseConnection;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    /**
     * Save a new guest to the database
     *
     * @param guest The guest to save
     * @return The generated guest ID
     * @throws DatabaseException If there's an error saving the guest
     */
    public int save(Guest guest) throws DatabaseException {
        String sql = "INSERT INTO guests (name, phone_number, email, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getPhoneNumber());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getAddress());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating guest failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int guestId = generatedKeys.getInt(1);
                    guest.setGuestID(guestId);
                    return guestId;
                } else {
                    throw new DatabaseException("Creating guest failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while saving guest", e);
            throw new DatabaseException("Error saving guest: " + e.getMessage(), e);
        }
    }

    /**
     * Find a guest by ID
     *
     * @param guestId The guest ID
     * @return The guest, or null if not found
     * @throws DatabaseException If there's an error retrieving the guest
     */
    public Guest findById(int guestId) throws DatabaseException {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGuest(rs);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding guest by ID", e);
            throw new DatabaseException("Error finding guest: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing guest
     *
     * @param guest The guest to update
     * @throws DatabaseException If there's an error updating the guest
     */
    public void update(Guest guest) throws DatabaseException {
        String sql = "UPDATE guests SET name = ?, phone_number = ?, email = ?, address = ? WHERE guest_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getPhoneNumber());
            stmt.setString(3, guest.getEmail());
            stmt.setString(4, guest.getAddress());
            stmt.setInt(5, guest.getGuestID());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating guest failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating guest", e);
            throw new DatabaseException("Error updating guest: " + e.getMessage(), e);
        }
    }

    /**
     * Find guests by name (partial match)
     *
     * @param name The name to search for
     * @return List of matching guests
     * @throws DatabaseException If there's an error searching for guests
     */
    public List<Guest> findByName(String name) throws DatabaseException {
        String sql = "SELECT * FROM guests WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                List<Guest> guests = new ArrayList<>();

                while (rs.next()) {
                    guests.add(mapResultSetToGuest(rs));
                }

                return guests;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding guests by name", e);
            throw new DatabaseException("Error finding guests: " + e.getMessage(), e);
        }
    }

    /**
     * Find guests by phone number (partial match)
     *
     * @param phoneNumber The phone number to search for
     * @return List of matching guests
     * @throws DatabaseException If there's an error searching for guests
     */
    public List<Guest> findByPhone(String phoneNumber) throws DatabaseException {
        String sql = "SELECT * FROM guests WHERE phone_number LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + phoneNumber + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                List<Guest> guests = new ArrayList<>();

                while (rs.next()) {
                    guests.add(mapResultSetToGuest(rs));
                }

                return guests;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding guests by phone", e);
            throw new DatabaseException("Error finding guests: " + e.getMessage(), e);
        }
    }

    /**
     * Find guests by email (exact match)
     *
     * @param email The email to search for
     * @return List of matching guests
     * @throws DatabaseException If there's an error searching for guests
     */
    public List<Guest> findByEmail(String email) throws DatabaseException {
        String sql = "SELECT * FROM guests WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Guest> guests = new ArrayList<>();

                while (rs.next()) {
                    guests.add(mapResultSetToGuest(rs));
                }

                return guests;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding guests by email", e);
            throw new DatabaseException("Error finding guests: " + e.getMessage(), e);
        }
    }

    /**
     * Map a result set row to a Guest object
     *
     * @param rs The result set
     * @return A Guest object
     * @throws SQLException If there's an error accessing the result set
     */
    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setGuestID(rs.getInt("guest_id"));
        guest.setName(rs.getString("name"));
        guest.setPhoneNumber(rs.getString("phone_number"));
        guest.setEmail(rs.getString("email"));
        guest.setAddress(rs.getString("address"));

        // Check if feedback column exists and has a value
        try {
            String feedback = rs.getString("feedback");
            if (feedback != null) {
                guest.setFeedback(feedback);
            }
        } catch (SQLException ignored) {
        }

        return guest;
    }

    /**
     * Update guest feedback
     *
     * @param guestId The guest ID
     * @param feedback The feedback text
     * @throws DatabaseException If there's an error updating the feedback
     */
    public void updateFeedback(int guestId, String feedback) throws DatabaseException {
        String sql = "UPDATE guests SET feedback = ? WHERE guest_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, feedback);
            stmt.setInt(2, guestId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating guest feedback failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating guest feedback", e);
            throw new DatabaseException("Error updating guest feedback: " + e.getMessage(), e);
        }
    }
}