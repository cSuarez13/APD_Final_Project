package ca.senecacollege.apd_final_project.dao;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.util.DatabaseConnection;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    /**
     * Save a new reservation
     *
     * @param reservation The reservation to save
     * @return The generated reservation ID
     * @throws DatabaseException If there's an error saving the reservation
     */
    public int save(Reservation reservation) throws DatabaseException {
        String sql = "INSERT INTO reservations (guest_id, check_in_date, check_out_date, " +
                "number_of_guests, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reservation.getGuestID());
            stmt.setDate(2, java.sql.Date.valueOf(reservation.getCheckInDate()));
            stmt.setDate(3, java.sql.Date.valueOf(reservation.getCheckOutDate()));
            stmt.setInt(4, reservation.getNumberOfGuests());
            stmt.setString(5, reservation.getStatus().getDisplayName());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating reservation failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int reservationId = generatedKeys.getInt(1);
                    reservation.setReservationID(reservationId);
                    return reservationId;
                } else {
                    throw new DatabaseException("Creating reservation failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while saving reservation", e);
            throw new DatabaseException("Error saving reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Find a reservation by ID
     *
     * @param reservationId The reservation ID
     * @return The reservation, or null if not found
     * @throws DatabaseException If there's an error retrieving the reservation
     */
    public Reservation findById(int reservationId) throws DatabaseException {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservation by ID", e);
            throw new DatabaseException("Error finding reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Find reservations by guest ID
     *
     * @param guestId The guest ID
     * @return List of reservations for the guest
     * @throws DatabaseException If there's an error retrieving the reservations
     */
    public List<Reservation> findByGuestId(int guestId) throws DatabaseException {
        String sql = "SELECT * FROM reservations WHERE guest_id = ? ORDER BY check_in_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();

                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }

                return reservations;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservations by guest ID", e);
            throw new DatabaseException("Error finding reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Find reservations by status
     *
     * @param statuses Array of statuses to search for
     * @return List of reservations with the specified statuses
     * @throws DatabaseException If there's an error retrieving the reservations
     */
    public List<Reservation> findByStatus(String[] statuses) throws DatabaseException {
        if (statuses == null || statuses.length == 0) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM reservations WHERE status IN (");
        for (int i = 0; i < statuses.length; i++) {
            sql.append("?");
            if (i < statuses.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(") ORDER BY check_in_date");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < statuses.length; i++) {
                stmt.setString(i + 1, statuses[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();

                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }

                return reservations;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservations by status", e);
            throw new DatabaseException("Error finding reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Find reservations by check-in date
     *
     * @param checkInDate The check-in date
     * @return List of reservations with the specified check-in date
     * @throws DatabaseException If there's an error retrieving the reservations
     */
    public List<Reservation> findByCheckInDate(LocalDate checkInDate) throws DatabaseException {
        String sql = "SELECT * FROM reservations WHERE check_in_date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(checkInDate));

            try (ResultSet rs = stmt.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();

                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }

                return reservations;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservations by check-in date", e);
            throw new DatabaseException("Error finding reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Find reservations by check-out date
     *
     * @param checkOutDate The check-out date
     * @return List of reservations with the specified check-out date
     * @throws DatabaseException If there's an error retrieving the reservations
     */
    public List<Reservation> findByCheckOutDate(LocalDate checkOutDate) throws DatabaseException {
        String sql = "SELECT * FROM reservations WHERE check_out_date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(checkOutDate));

            try (ResultSet rs = stmt.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();

                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }

                return reservations;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservations by check-out date", e);
            throw new DatabaseException("Error finding reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Find reservations by date range
     *
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of reservations that overlap with the date range
     * @throws DatabaseException If there's an error retrieving the reservations
     */
    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT * FROM reservations WHERE " +
                "(check_in_date BETWEEN ? AND ?) OR " +
                "(check_out_date BETWEEN ? AND ?) OR " +
                "(check_in_date <= ? AND check_out_date >= ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));
            stmt.setDate(3, java.sql.Date.valueOf(startDate));
            stmt.setDate(4, java.sql.Date.valueOf(endDate));
            stmt.setDate(5, java.sql.Date.valueOf(startDate));
            stmt.setDate(6, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();

                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }

                return reservations;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding reservations by date range", e);
            throw new DatabaseException("Error finding reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Update a reservation
     *
     * @param reservation The reservation to update
     * @throws DatabaseException If there's an error updating the reservation
     */
    public void update(Reservation reservation) throws DatabaseException {
        String sql = "UPDATE reservations SET guest_id = ?, check_in_date = ?, " +
                "check_out_date = ?, number_of_guests = ?, status = ? WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservation.getGuestID());
            stmt.setDate(2, java.sql.Date.valueOf(reservation.getCheckInDate()));
            stmt.setDate(3, java.sql.Date.valueOf(reservation.getCheckOutDate()));
            stmt.setInt(4, reservation.getNumberOfGuests());
            stmt.setString(5, reservation.getStatus().getDisplayName());
            stmt.setInt(6, reservation.getReservationID());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating reservation failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating reservation", e);
            throw new DatabaseException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Map a result set row to a Reservation object
     *
     * @param rs The result set
     * @return A Reservation object
     * @throws SQLException If there's an error accessing the result set
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationID(rs.getInt("reservation_id"));
        reservation.setGuestID(rs.getInt("guest_id"));
        reservation.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        reservation.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        reservation.setNumberOfGuests(rs.getInt("number_of_guests"));

        String statusStr = rs.getString("status");
        reservation.setStatus(ReservationStatus.fromDisplayName(statusStr));

        return reservation;
    }
}