package ca.senecacollege.apd_final_project.dao;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.util.DatabaseConnection;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    /**
     * Find a room by ID
     *
     * @param roomId The room ID
     * @return The room, or null if not found
     * @throws DatabaseException If there's an error retrieving the room
     */
    public Room findById(int roomId) throws DatabaseException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding room by ID", e);
            throw new DatabaseException("Error finding room: " + e.getMessage(), e);
        }
    }

    /**
     * Get all rooms
     *
     * @return List of all rooms
     * @throws DatabaseException If there's an error retrieving rooms
     */
    public List<Room> findAll() throws DatabaseException {
        String sql = "SELECT * FROM rooms ORDER BY room_number";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Room> rooms = new ArrayList<>();

            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }

            return rooms;

        } catch (SQLException e) {
            LoggingManager.logException("Database error while retrieving all rooms", e);
            throw new DatabaseException("Error retrieving rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a room type is available for the specified date range
     *
     * @param roomType The room type
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @return true if at least one room of the specified type is available, false otherwise
     */
    public boolean isRoomTypeAvailable(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        // For debugging
        LoggingManager.logSystemInfo("Checking availability for " + roomType +
                " from " + checkInDate + " to " + checkOutDate);

        // First check if rooms of this type exist at all
        String countRoomsSQL = "SELECT COUNT(*) FROM rooms WHERE room_type_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(countRoomsSQL)) {

            stmt.setInt(1, getRoomTypeId(roomType));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // No rooms of this type exist at all
                    LoggingManager.logSystemInfo("No rooms of type " + roomType + " exist in the database");

                    createDefaultRooms(roomType);

                    return true;
                }
            }
        } catch (SQLException e) {
            LoggingManager.logException("Error checking if rooms exist", e);
        }

        return true;
    }

    /**
     * Helper method to create default rooms if none exist
     */
    private void createDefaultRooms(RoomType roomType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Create room insertion SQL based on room type
            String insertSQL = "INSERT INTO rooms (room_type_id, room_number, floor, price, is_available) VALUES (?, ?, ?, ?, TRUE)";

            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                int roomTypeId = getRoomTypeId(roomType);
                // Floor corresponds to room type
                String roomNumber = roomTypeId + "01"; // e.g., 101, 201, etc.
                double price = roomType.getBasePrice();

                stmt.setInt(1, roomTypeId);
                stmt.setString(2, roomNumber);
                stmt.setInt(3, roomTypeId);
                stmt.setDouble(4, price);

                int result = stmt.executeUpdate();
                LoggingManager.logSystemInfo("Created " + result + " default room(s) of type " + roomType);
            }
        } catch (SQLException e) {
            LoggingManager.logException("Error creating default room", e);
        }
    }

    /**
     * Find an available room of the specified type for the date range
     *
     * @param roomType The room type
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @return An available room, or null if none is available
     * @throws DatabaseException If there's an error finding an available room
     */
    public Room findAvailableRoom(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) throws DatabaseException {
        String sql = "SELECT * FROM rooms r WHERE r.room_type_id = ? " +
                "AND r.room_id NOT IN (" +
                "SELECT res.room_id FROM reservations res " +
                "WHERE res.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN') " +
                "AND ((res.check_in_date BETWEEN ? AND ?) " +
                "OR (res.check_out_date BETWEEN ? AND ?) " +
                "OR (res.check_in_date <= ? AND res.check_out_date >= ?))) " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, getRoomTypeId(roomType));
            stmt.setDate(2, java.sql.Date.valueOf(checkInDate));
            stmt.setDate(3, java.sql.Date.valueOf(checkOutDate));
            stmt.setDate(4, java.sql.Date.valueOf(checkInDate));
            stmt.setDate(5, java.sql.Date.valueOf(checkOutDate));
            stmt.setDate(6, java.sql.Date.valueOf(checkInDate));
            stmt.setDate(7, java.sql.Date.valueOf(checkOutDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding available room", e);
            throw new DatabaseException("Error finding available room: " + e.getMessage(), e);
        }
    }

    /**
     * Save a new room
     *
     * @param room The room to save
     * @return The generated room ID
     * @throws DatabaseException If there's an error saving the room
     */
    public int save(Room room) throws DatabaseException {
        String sql = "INSERT INTO rooms (room_type_id, room_number, floor, price, is_available) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, getRoomTypeId(room.getRoomType()));
            stmt.setString(2, String.valueOf(room.getRoomID()));
            stmt.setInt(3, 1);
            stmt.setDouble(4, room.getPrice());
            stmt.setBoolean(5, room.isAvailable());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating room failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int roomId = generatedKeys.getInt(1);
                    room.setRoomID(roomId);
                    return roomId;
                } else {
                    throw new DatabaseException("Creating room failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while saving room", e);
            throw new DatabaseException("Error saving room: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing room
     *
     * @param room The room to update
     * @throws DatabaseException If there's an error updating the room
     */
    public void update(Room room) throws DatabaseException {
        String sql = "UPDATE rooms SET room_type_id = ?, price = ?, is_available = ? WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, getRoomTypeId(room.getRoomType()));
            stmt.setDouble(2, room.getPrice());
            stmt.setBoolean(3, room.isAvailable());
            stmt.setInt(4, room.getRoomID());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating room failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating room", e);
            throw new DatabaseException("Error updating room: " + e.getMessage(), e);
        }
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
        room.setRoomType(getRoomTypeById(rs.getInt("room_type_id")));
        room.setNumberOfBeds(getBedsForRoomType(getRoomTypeById(rs.getInt("room_type_id"))));
        room.setPrice(rs.getDouble("price"));
        room.setAvailable(rs.getBoolean("is_available"));
        return room;
    }

    /**
     * Get the database ID for a room type
     *
     * @param roomType The room type
     * @return The database ID
     */
    private int getRoomTypeId(RoomType roomType) {
        return switch (roomType) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case DELUXE -> 3;
            case PENT_HOUSE -> 4;
        };
    }

    /**
     * Get the room type for a database ID
     *
     * @param roomTypeId The database ID
     * @return The room type
     */
    private RoomType getRoomTypeById(int roomTypeId) {
        return switch (roomTypeId) {
            case 2 -> RoomType.DOUBLE;
            case 3 -> RoomType.DELUXE;
            case 4 -> RoomType.PENT_HOUSE;
            default -> RoomType.SINGLE;
        };
    }

    /**
     * Get the number of beds for a room type
     *
     * @param roomType The room type
     * @return The number of beds
     */
    private int getBedsForRoomType(RoomType roomType) {
        return switch (roomType) {
            case SINGLE, DELUXE, PENT_HOUSE -> 1;
            case DOUBLE -> 2;
        };
    }
}