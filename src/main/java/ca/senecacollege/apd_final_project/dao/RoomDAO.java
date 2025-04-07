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
        String sql = "SELECT r.*, rt.name, rt.base_price, rt.max_occupancy FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                "WHERE r.room_id = ?";

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
     * Count the number of available rooms of a specific type for the given date range
     *
     * @param roomType The room type
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return The number of available rooms
     * @throws DatabaseException If there's an error counting available rooms
     */
    public int countAvailableRooms(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM rooms r WHERE r.room_type_id = ? " +
                "AND r.is_available = TRUE " +
                "AND r.room_id NOT IN (" +
                "SELECT rr.room_id FROM reservations res " +
                "JOIN reservation_rooms rr ON res.reservation_id = rr.reservation_id " +
                "WHERE res.status IN ('Pending', 'Confirmed', 'Checked In') " +
                "AND ((res.check_in_date BETWEEN ? AND ?) " +
                "OR (res.check_out_date BETWEEN ? AND ?) " +
                "OR (res.check_in_date <= ? AND res.check_out_date >= ?)))";

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
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while counting available rooms", e);
            throw new DatabaseException("Error counting available rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Find an available room of the specified type for the given date range
     *
     * @param roomType The room type
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return An available room, or null if none is available
     * @throws DatabaseException If there's an error finding an available room
     */
    public Room findAvailableRoom(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) throws DatabaseException {
        String sql = "SELECT * FROM rooms r WHERE r.room_type_id = ? " +
                "AND r.is_available = TRUE " +
                "AND r.room_id NOT IN (" +
                "SELECT rr.room_id FROM reservations res " +
                "JOIN reservation_rooms rr ON res.reservation_id = rr.reservation_id " +
                "WHERE res.status IN ('Pending', 'Confirmed', 'Checked In') " +
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
                    // If no rooms are available, check if rooms of this type exist at all
                    if (countRoomsByType(roomType) == 0) {
                        // Create default rooms if none exist
                        createDefaultRooms(roomType);
                        // Try again to find an available room
                        return findAvailableRoom(roomType, checkInDate, checkOutDate);
                    }
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding available room", e);
            throw new DatabaseException("Error finding available room: " + e.getMessage(), e);
        }
    }

    /**
     * NEW METHOD: Find all available rooms of the specified type for the given date range
     * Used for booking multiple rooms of the same type without duplicates
     *
     * @param roomType The room type
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return List of all available rooms of the specified type
     * @throws DatabaseException If there's an error finding available rooms
     */
    public List<Room> findAllAvailableRoomsByType(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate)
            throws DatabaseException {
        // Same query as findAvailableRoom but without LIMIT 1
        String sql = "SELECT * FROM rooms r WHERE r.room_type_id = ? " +
                "AND r.is_available = TRUE " +
                "AND r.room_id NOT IN (" +
                "SELECT rr.room_id FROM reservations res " +
                "JOIN reservation_rooms rr ON res.reservation_id = rr.reservation_id " +
                "WHERE res.status IN ('Pending', 'Confirmed', 'Checked In') " +
                "AND ((res.check_in_date BETWEEN ? AND ?) " +
                "OR (res.check_out_date BETWEEN ? AND ?) " +
                "OR (res.check_in_date <= ? AND res.check_out_date >= ?)))";

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
                List<Room> availableRooms = new ArrayList<>();
                while (rs.next()) {
                    availableRooms.add(mapResultSetToRoom(rs));
                }

                // If no rooms are available, check if rooms of this type exist at all
                if (availableRooms.isEmpty() && countRoomsByType(roomType) == 0) {
                    // Create default rooms if none exist
                    createDefaultRooms(roomType);
                    // Try again to find available rooms
                    return findAllAvailableRoomsByType(roomType, checkInDate, checkOutDate);
                }

                return availableRooms;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding available rooms", e);
            throw new DatabaseException("Error finding available rooms: " + e.getMessage(), e);
        }
    }

    /**
     * Count how many rooms of a specific type exist in the database
     *
     * @param roomType The room type
     * @return The number of rooms of the specified type
     * @throws SQLException If there's a database error
     */
    private int countRoomsByType(RoomType roomType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_type_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, getRoomTypeId(roomType));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }

    /**
     * Create default rooms of a specific type
     *
     * @param roomType The room type
     * @throws SQLException If there's a database error
     */
    private void createDefaultRooms(RoomType roomType) throws SQLException {
        LoggingManager.logSystemInfo("Creating default rooms for type: " + roomType);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Create room insertion SQL based on room type
            String insertSQL = "INSERT INTO rooms (room_type_id, room_number, floor, price, is_available) VALUES (?, ?, ?, ?, TRUE)";

            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                int roomTypeId = getRoomTypeId(roomType);
                int roomCount = switch (roomType) {
                    case SINGLE -> 40;  // Create 40 single rooms
                    case DOUBLE -> 30;  // Create 30 double rooms
                    case DELUXE -> 20;  // Create 20 deluxe rooms
                    case PENT_HOUSE -> 10;  // Create 10 pent houses
                };

                // Number of rooms to create for each type

                // Use batch processing for efficiency
                for (int i = 1; i <= roomCount; i++) {
                    // Floor corresponds to room type
                    int roomNumber = roomTypeId * 100 + i;  // e.g., 101, 102, 201, 202, etc.
                    double price = roomType.getBasePrice();

                    stmt.setInt(1, roomTypeId);
                    stmt.setString(2, String.valueOf(roomNumber));
                    stmt.setInt(3, roomTypeId);
                    stmt.setDouble(4, price);
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                LoggingManager.logSystemInfo("Created " + results.length + " default room(s) of type " + roomType);
            }
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
        room.setRoomNumber(rs.getString("room_number"));
        room.setFloor(rs.getInt("floor"));
        room.setPrice(rs.getDouble("price"));
        room.setAvailable(rs.getBoolean("is_available"));

        // Map room type from type_id
        int roomTypeId = rs.getInt("room_type_id");
        room.setRoomType(getRoomTypeById(roomTypeId));

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
     * Check if a specific room is available for the given date range,
     * excluding a specific reservation (for modification purposes)
     *
     * @param roomId The room ID
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @param excludeReservationId Reservation ID to exclude from availability check
     * @return true if the room is available, false otherwise
     * @throws SQLException If there's a database error
     */
    public boolean isRoomAvailable(int roomId, LocalDate checkInDate, LocalDate checkOutDate, int excludeReservationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations " +
                "WHERE room_id = ? " +
                "AND reservation_id != ? " +
                "AND status IN ('Pending', 'Confirmed', 'Checked In') " +
                "AND ((check_in_date BETWEEN ? AND ?) " +
                "OR (check_out_date BETWEEN ? AND ?) " +
                "OR (check_in_date <= ? AND check_out_date >= ?))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            stmt.setInt(2, excludeReservationId);
            stmt.setDate(3, java.sql.Date.valueOf(checkInDate));
            stmt.setDate(4, java.sql.Date.valueOf(checkOutDate));
            stmt.setDate(5, java.sql.Date.valueOf(checkInDate));
            stmt.setDate(6, java.sql.Date.valueOf(checkOutDate));
            stmt.setDate(7, java.sql.Date.valueOf(checkInDate));
            stmt.setDate(8, java.sql.Date.valueOf(checkOutDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int overlappingReservations = rs.getInt(1);
                    return overlappingReservations == 0;
                }
                return false;
            }
        }
    }
}