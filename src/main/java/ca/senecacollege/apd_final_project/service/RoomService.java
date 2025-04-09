package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.RoomDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.time.LocalDate;
import java.util.List;

public class RoomService {

    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
    }

    /**
     * Get a room by its ID
     *
     * @param roomId The room ID
     * @return The room object
     * @throws DatabaseException If there's an error retrieving the room
     */
    public Room getRoomById(int roomId) throws DatabaseException {
        try {
            return roomDAO.findById(roomId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving room with ID: " + roomId, e);
            throw new DatabaseException("Error retrieving room: " + e.getMessage(), e);
        }
    }

    /**
     * Count how many rooms of a specific type are available for the given date range
     *
     * @param roomType The room type
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return The number of available rooms
     * @throws DatabaseException If there's an error counting available rooms
     */
    public int countAvailableRooms(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) throws DatabaseException {
        try {
            return roomDAO.countAvailableRooms(roomType, checkInDate, checkOutDate);
        } catch (Exception e) {
            LoggingManager.logException("Error counting available rooms", e);
            throw new DatabaseException("Error counting available rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Update a room's details
     *
     * @param room The room to update
     * @throws DatabaseException If there's an error updating the room
     */
    public void updateRoom(Room room) throws DatabaseException {
        try {
            roomDAO.update(room);
            LoggingManager.logSystemInfo("Room updated: " + room.getRoomID());
        } catch (Exception e) {
            LoggingManager.logException("Error updating room", e);
            throw new DatabaseException("Error updating room: " + e.getMessage(), e);
        }
    }

    /**
     * Set a room's availability status
     *
     * @param roomId The room ID
     * @param available The availability status
     * @throws DatabaseException If there's an error updating the room
     */
    public void setRoomAvailability(int roomId, boolean available) throws DatabaseException {
        try {
            Room room = getRoomById(roomId);
            room.setAvailable(available);
            updateRoom(room);
            LoggingManager.logSystemInfo("Room " + roomId + " availability set to: " + available);
        } catch (Exception e) {
            LoggingManager.logException("Error setting room availability", e);
            throw new DatabaseException("Error setting room availability: " + e.getMessage(), e);
        }
    }

    /**
     * Get all rooms
     *
     * @return List of all rooms
     * @throws DatabaseException If there's an error retrieving rooms
     */
    public List<Room> getAllRooms() throws DatabaseException {
        try {
            return roomDAO.findAll();
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving all rooms", e);
            throw new DatabaseException("Error retrieving all rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Finds an available room of specified type for given dates
     * @param roomType Type of room needed
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @return Available Room or null if none found
     * @throws DatabaseException If database error occurs
     */
    public Room findAvailableRoom(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate)
            throws DatabaseException {
        try {
            return roomDAO.findAvailableRoom(roomType, checkInDate, checkOutDate);
        } catch (Exception e) {
            LoggingManager.logException("Error finding available room", e);
            throw new DatabaseException("Error finding available room: " + e.getMessage(), e);
        }
    }

    /**
     * Get all available rooms of a specific type for the given date range
     * This allows the booking system to book multiple different rooms of the same type
     *
     * @param roomType The room type
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return List of all available rooms of the specified type
     * @throws DatabaseException If there's an error retrieving the rooms
     */
    public List<Room> getAllAvailableRoomsByType(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate)
            throws DatabaseException {
        try {
            return roomDAO.findAllAvailableRoomsByType(roomType, checkInDate, checkOutDate);
        } catch (Exception e) {
            LoggingManager.logException("Error finding available rooms by type", e);
            throw new DatabaseException("Error finding available rooms: " + e.getMessage(), e);
        }
    }
}