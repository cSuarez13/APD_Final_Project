package ca.senecacollege.apd_final_project.dao;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Billing;
import ca.senecacollege.apd_final_project.util.DatabaseConnection;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Billing entities
 */
public class BillingDAO {

    /**
     * Save a new billing record to the database
     *
     * @param billing The billing to save
     * @return The generated billing ID
     * @throws DatabaseException If there's an error saving the billing
     */
    public int save(Billing billing) throws DatabaseException {
        String sql = "INSERT INTO bills (reservation_id, amount, tax, discount, total_amount, billing_date, is_paid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, billing.getReservationID());
            stmt.setDouble(2, billing.getAmount());
            stmt.setDouble(3, billing.getTax());
            stmt.setDouble(4, billing.getDiscount());
            stmt.setDouble(5, billing.getTotalAmount());
            stmt.setTimestamp(6, Timestamp.valueOf(billing.getBillingDateTime()));
            stmt.setBoolean(7, billing.isPaid());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating billing failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int billId = generatedKeys.getInt(1);
                    billing.setBillID(billId);
                    return billId;
                } else {
                    throw new DatabaseException("Creating billing failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while saving billing", e);
            throw new DatabaseException("Error saving billing: " + e.getMessage(), e);
        }
    }

    /**
     * Find a billing by ID
     *
     * @param billId The billing ID
     * @return The billing, or null if not found
     * @throws DatabaseException If there's an error retrieving the billing
     */
    public Billing findById(int billId) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBilling(rs);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding billing by ID", e);
            throw new DatabaseException("Error finding billing: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing billing
     *
     * @param billing The billing to update
     * @throws DatabaseException If there's an error updating the billing
     */
    public void update(Billing billing) throws DatabaseException {
        String sql = "UPDATE bills SET reservation_id = ?, amount = ?, tax = ?, discount = ?, " +
                "total_amount = ?, billing_date = ?, is_paid = ? WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billing.getReservationID());
            stmt.setDouble(2, billing.getAmount());
            stmt.setDouble(3, billing.getTax());
            stmt.setDouble(4, billing.getDiscount());
            stmt.setDouble(5, billing.getTotalAmount());
            stmt.setTimestamp(6, Timestamp.valueOf(billing.getBillingDateTime()));
            stmt.setBoolean(7, billing.isPaid());
            stmt.setInt(8, billing.getBillID());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating billing failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating billing", e);
            throw new DatabaseException("Error updating billing: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a billing
     *
     * @param billId The billing ID
     * @throws DatabaseException If there's an error deleting the billing
     */
    public void delete(int billId) throws DatabaseException {
        String sql = "DELETE FROM bills WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Deleting billing failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while deleting billing", e);
            throw new DatabaseException("Error deleting billing: " + e.getMessage(), e);
        }
    }

    /**
     * Find billings by reservation ID
     *
     * @param reservationId The reservation ID
     * @return List of billings for the reservation
     * @throws DatabaseException If there's an error retrieving billings
     */
    public List<Billing> findByReservationId(int reservationId) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Billing> billings = new ArrayList<>();

                while (rs.next()) {
                    billings.add(mapResultSetToBilling(rs));
                }

                return billings;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding billings by reservation ID", e);
            throw new DatabaseException("Error finding billings: " + e.getMessage(), e);
        }
    }

    /**
     * Find billings by paid status
     *
     * @param isPaid The paid status to filter by
     * @return List of billings with the specified paid status
     * @throws DatabaseException If there's an error retrieving billings
     */
    public List<Billing> findByPaidStatus(boolean isPaid) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE is_paid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isPaid);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Billing> billings = new ArrayList<>();

                while (rs.next()) {
                    billings.add(mapResultSetToBilling(rs));
                }

                return billings;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding billings by paid status", e);
            throw new DatabaseException("Error finding billings: " + e.getMessage(), e);
        }
    }

    /**
     * Find billings within a date range
     *
     * @param startDateTime Start date/time
     * @param endDateTime End date/time
     * @return List of billings within the date range
     * @throws DatabaseException If there's an error retrieving billings
     */
    public List<Billing> findByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE billing_date BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDateTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endDateTime));

            try (ResultSet rs = stmt.executeQuery()) {
                List<Billing> billings = new ArrayList<>();

                while (rs.next()) {
                    billings.add(mapResultSetToBilling(rs));
                }

                return billings;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding billings by date range", e);
            throw new DatabaseException("Error finding billings: " + e.getMessage(), e);
        }
    }

    /**
     * Find paid billings within a date range
     *
     * @param startDateTime Start date/time
     * @param endDateTime End date/time
     * @return List of paid billings within the date range
     * @throws DatabaseException If there's an error retrieving billings
     */
    public List<Billing> findPaidByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE billing_date BETWEEN ? AND ? AND is_paid = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDateTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endDateTime));

            try (ResultSet rs = stmt.executeQuery()) {
                List<Billing> billings = new ArrayList<>();

                while (rs.next()) {
                    billings.add(mapResultSetToBilling(rs));
                }

                return billings;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding paid billings by date range", e);
            throw new DatabaseException("Error finding paid billings: " + e.getMessage(), e);
        }
    }

    /**
     * Map a result set row to a Billing object
     *
     * @param rs The result set
     * @return A Billing object
     * @throws SQLException If there's an error accessing the result set
     */
    private Billing mapResultSetToBilling(ResultSet rs) throws SQLException {
        Billing billing = new Billing();
        billing.setBillID(rs.getInt("bill_id"));
        billing.setReservationID(rs.getInt("reservation_id"));
        billing.setAmount(rs.getDouble("amount"));
        billing.setDiscount(rs.getDouble("discount"));
        billing.setBillingDateTime(rs.getTimestamp("billing_date").toLocalDateTime());
        billing.setPaid(rs.getBoolean("is_paid"));

        return billing;
    }
}