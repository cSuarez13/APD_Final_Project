package ca.senecacollege.apd_final_project.dao;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.ReservationRoom;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.util.DatabaseConnection;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ReservationRoom entities
 * This manages the relationship between reservations and rooms
 */
public class ReservationRoomDAO {

    /**
     * Save a new reservation-room relationship
     *
     * @param reservationRoom The reservation-room relationship to save
     * @return The generated ID
     * @throws DatabaseException If there's an error saving the relationship
     */
    public int save(ReservationRoom reservationRoom) throws DatabaseException {
        String sql = "INSERT INTO reservation_rooms (reservation_id, room_id, price_per_night) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reservationRoom.getReservationID());
            stmt.setInt(2, reservationRoom.getRoomID());
            stmt.setDouble(3, reservationRoom.getPricePerNight());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating reservation-room relationship failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    reservationRoom.setId(id);
                    return id;
                } else {
                    throw new DatabaseException("Creating reservation-room relationship failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while saving reservation-room relationship", e);
            throw new DatabaseException("Error saving reservation-room relationship: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing reservation-room relationship
     *
     * @param reservationRoom The reservation-room relationship to update
     * @throws DatabaseException If there's an error updating the relationship
     */
    public void update(ReservationRoom reservationRoom) throws DatabaseException {
        String sql = "UPDATE reservation_rooms SET reservation_id = ?, room_id = ?, guests_in_room = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationRoom.getReservationID());
            stmt.setInt(2, reservationRoom.getRoomID());
            stmt.setInt(3, reservationRoom.getGuestsInRoom());
            stmt.setInt(4, reservationRoom.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating reservation-room relationship failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating reservation-room relationship", e);
            throw new DatabaseException("Error updating reservation-room relationship: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a reservation-room relationship
     *
     * @param id The ID of the relationship to delete
     * @throws DatabaseException If there's an error deleting the relationship
     */
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM reservation_rooms WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            LoggingManager.logException("Database error while deleting reservation-room relationship", e);
            throw new DatabaseException("Error deleting reservation-room relationship: " + e.getMessage(), e);
        }
    }

    /**
     * Delete all relationships for a reservation
     *
     * @param reservationId The ID of the reservation
     * @throws DatabaseException If there's an error deleting the relationships
     */
    public void deleteByReservationId(int reservationId) throws DatabaseException {
        String sql = "DELETE FROM reservation_rooms WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            LoggingManager.logException("Database error while deleting reservation-room relationships by reservation ID", e);
            throw new DatabaseException("Error deleting reservation-room relationships: " + e.getMessage(), e);
        }
    }

    /**
     * Get all room relationships for a reservation
     *
     * @param reservationId The ID of the reservation
     * @return List of reservation-room relationships
     * @throws DatabaseException If there's an error retrieving the relationships
     */
    public List<ReservationRoom> findByReservationId(int reservationId) throws DatabaseException {
        String sql = "SELECT * FROM reservation_rooms WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ReservationRoom> relationships = new ArrayList<>();

                while (rs.next()) {
                    relationships.add(mapResultSetToReservationRoom(rs));
                }

                return relationships;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservation-room relationships by reservation ID", e);
            throw new DatabaseException("Error finding reservation-room relationships: " + e.getMessage(), e);
        }
    }

    /**
     * Get all rooms for a reservation
     *
     * @param reservationId The ID of the reservation
     * @return List of rooms
     * @throws DatabaseException If there's an error retrieving the rooms
     */
    public List<Room> findRoomsByReservationId(int reservationId) throws DatabaseException {
        String sql = "SELECT r.* FROM rooms r " +
                "JOIN reservation_rooms rr ON r.room_id = rr.room_id " +
                "WHERE rr.reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Room> rooms = new ArrayList<>();

                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }

                return rooms;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding rooms by reservation ID", e);
            throw new DatabaseException("Error finding rooms for reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Find a specific reservation-room relationship
     *
     * @param reservationId The reservation ID
     * @param roomId The room ID
     * @return The reservation-room relationship, or null if not found
     * @throws DatabaseException If there's an error retrieving the relationship
     */
    public ReservationRoom findByReservationAndRoom(int reservationId, int roomId) throws DatabaseException {
        String sql = "SELECT * FROM reservation_rooms WHERE reservation_id = ? AND room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            stmt.setInt(2, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservationRoom(rs);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservation-room relationship", e);
            throw new DatabaseException("Error finding reservation-room relationship: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a room is already booked for a reservation
     *
     * @param reservationId The reservation ID
     * @param roomId The room ID
     * @return true if the room is already booked for the reservation, false otherwise
     * @throws DatabaseException If there's an error checking the booking
     */
    public boolean isRoomBookedForReservation(int reservationId, int roomId) throws DatabaseException {
        return findByReservationAndRoom(reservationId, roomId) != null;
    }

    /**
     * Map a result set row to a ReservationRoom object
     *
     * @param rs The result set
     * @return A ReservationRoom object
     * @throws SQLException If there's an error accessing the result set
     */
    private ReservationRoom mapResultSetToReservationRoom(ResultSet rs) throws SQLException {
        ReservationRoom reservationRoom = new ReservationRoom();
        reservationRoom.setId(rs.getInt("reservation_room_id"));
        reservationRoom.setReservationID(rs.getInt("reservation_id"));
        reservationRoom.setRoomID(rs.getInt("room_id"));
        reservationRoom.setPricePerNight(rs.getDouble("price_per_night"));
        return reservationRoom;
    }

    /**
     * Map a result set row to a Room object
     *
     * @param rs The result set
     * @return A Room object
     * @throws SQLException If there's an error accessing the result set
     */
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomID(rs.getInt("room_id"));
        // Set room type based on room_type_id
        int roomTypeId = rs.getInt("room_type_id");
        switch (roomTypeId) {
            case 2:
                room.setRoomType(RoomType.DOUBLE);
                break;
            case 3:
                room.setRoomType(RoomType.DELUXE);
                break;
            case 4:
                room.setRoomType(RoomType.PENT_HOUSE);
                break;
            default:
                room.setRoomType(RoomType.SINGLE);
        }
        room.setPrice(rs.getDouble("price"));
        room.setAvailable(rs.getBoolean("is_available"));
        return room;
    }
}