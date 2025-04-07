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
     * Find a bill by ID
     *
     * @param billId The bill ID
     * @return The bill, or null if not found
     * @throws DatabaseException If there's an error retrieving the bill
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
            LoggingManager.logException("Database error while finding bill by ID", e);
            throw new DatabaseException("Error finding bill: " + e.getMessage(), e);
        }
    }

    /**
     * Save a new billing record to the database
     *
     * @param billing The billing to save
     * @return The generated billing ID
     * @throws DatabaseException If there's an error saving the billing
     */
    public int save(Billing billing) throws DatabaseException {
        String sql = "INSERT INTO bills (reservation_id, amount, tax, discount, total_amount, payment_method, billing_date, is_paid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, billing.getReservationID());
            stmt.setDouble(2, billing.getAmount());
            stmt.setDouble(3, billing.getTax());
            stmt.setDouble(4, billing.getDiscount());
            stmt.setDouble(5, billing.getTotalAmount());
            stmt.setString(6, billing.getPaymentMethod());
            stmt.setTimestamp(7, Timestamp.valueOf(billing.getBillingDateTime()));
            stmt.setBoolean(8, billing.isPaid());

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
     * Find billings within a date range
     *
     * @param startDateTime Start date/time
     * @param endDateTime   End date/time
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
        billing.setTax(rs.getDouble("tax"));
        billing.setDiscount(rs.getDouble("discount"));
        billing.setTotalAmount(rs.getDouble("total_amount"));
        billing.setPaymentMethod(rs.getString("payment_method"));
        billing.setBillingDateTime(rs.getTimestamp("billing_date").toLocalDateTime());
        billing.setPaid(rs.getBoolean("is_paid"));
        return billing;
    }
}