package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.BillingDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Billing;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class to handle all billing-related operations
 */
public class BillingService {

    private BillingDAO billingDAO;
    private ReservationService reservationService;
    private RoomService roomService;

    /**
     * Constructor
     */
    public BillingService() {
        this.billingDAO = new BillingDAO();
        this.reservationService = new ReservationService();
        this.roomService = new RoomService();
    }

    /**
     * Generate a bill for a reservation
     *
     * @param reservationId The reservation ID
     * @return The generated bill
     * @throws DatabaseException If there's an error generating the bill
     */
    public Billing generateBill(int reservationId) throws DatabaseException {
        try {
            // Get the reservation
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation == null) {
                throw new DatabaseException("Reservation not found");
            }

            // Get the room
            Room room = roomService.getRoomById(reservation.getRoomID());
            if (room == null) {
                throw new DatabaseException("Room not found");
            }

            // Calculate total amount
            int nights = reservation.calculateNumberOfNights();
            double amount = room.getPrice() * nights;

            // Create bill
            Billing bill = new Billing();
            bill.generateBill(reservationId, amount);

            // Save to database
            int billId = billingDAO.save(bill);
            bill.setBillID(billId);

            LoggingManager.logSystemInfo("Generated bill #" + billId + " for reservation #" + reservationId);

            return bill;
        } catch (Exception e) {
            LoggingManager.logException("Error generating bill for reservation #" + reservationId, e);
            throw new DatabaseException("Error generating bill: " + e.getMessage(), e);
        }
    }

    /**
     * Get a bill by ID
     *
     * @param billId The bill ID
     * @return The bill
     * @throws DatabaseException If there's an error retrieving the bill
     */
    public Billing getBillById(int billId) throws DatabaseException {
        try {
            return billingDAO.findById(billId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving bill #" + billId, e);
            throw new DatabaseException("Error retrieving bill: " + e.getMessage(), e);
        }
    }

    /**
     * Get bills by reservation ID
     *
     * @param reservationId The reservation ID
     * @return List of bills for the reservation
     * @throws DatabaseException If there's an error retrieving bills
     */
    public List<Billing> getBillsByReservation(int reservationId) throws DatabaseException {
        try {
            return billingDAO.findByReservationId(reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving bills for reservation #" + reservationId, e);
            throw new DatabaseException("Error retrieving bills: " + e.getMessage(), e);
        }
    }

    /**
     * Apply a discount to a bill
     *
     * @param billId The bill ID
     * @param discountAmount The discount amount
     * @throws DatabaseException If there's an error applying the discount
     */
    public void applyDiscount(int billId, double discountAmount) throws DatabaseException {
        try {
            // Get the bill
            Billing bill = getBillById(billId);
            if (bill == null) {
                throw new DatabaseException("Bill not found");
            }

            // Ensure discount is not greater than amount
            if (discountAmount > bill.getAmount()) {
                throw new IllegalArgumentException("Discount cannot be greater than the bill amount");
            }

            // Apply discount
            bill.applyDiscount(discountAmount);

            // Update bill in database
            billingDAO.update(bill);

            LoggingManager.logSystemInfo("Applied discount of $" + discountAmount + " to bill #" + billId);
        } catch (Exception e) {
            LoggingManager.logException("Error applying discount to bill #" + billId, e);
            throw new DatabaseException("Error applying discount: " + e.getMessage(), e);
        }
    }

    /**
     * Mark a bill as paid
     *
     * @param billId The bill ID
     * @throws DatabaseException If there's an error updating the bill
     */
    public void markAsPaid(int billId) throws DatabaseException {
        try {
            // Get the bill
            Billing bill = getBillById(billId);
            if (bill == null) {
                throw new DatabaseException("Bill not found");
            }

            // Mark as paid
            bill.setPaid(true);

            // Update bill in database
            billingDAO.update(bill);

            LoggingManager.logSystemInfo("Marked bill #" + billId + " as paid");
        } catch (Exception e) {
            LoggingManager.logException("Error marking bill #" + billId + " as paid", e);
            throw new DatabaseException("Error marking bill as paid: " + e.getMessage(), e);
        }
    }

    /**
     * Get all unpaid bills
     *
     * @return List of unpaid bills
     * @throws DatabaseException If there's an error retrieving bills
     */
    public List<Billing> getUnpaidBills() throws DatabaseException {
        try {
            return billingDAO.findByPaidStatus(false);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving unpaid bills", e);
            throw new DatabaseException("Error retrieving unpaid bills: " + e.getMessage(), e);
        }
    }

    /**
     * Get bills created within a date range
     *
     * @param startDateTime Start date/time
     * @param endDateTime End date/time
     * @return List of bills within the date range
     * @throws DatabaseException If there's an error retrieving bills
     */
    public List<Billing> getBillsByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DatabaseException {
        try {
            return billingDAO.findByDateRange(startDateTime, endDateTime);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving bills by date range", e);
            throw new DatabaseException("Error retrieving bills by date range: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a bill
     *
     * @param billId The bill ID
     * @throws DatabaseException If there's an error deleting the bill
     */
    public void deleteBill(int billId) throws DatabaseException {
        try {
            billingDAO.delete(billId);
            LoggingManager.logSystemInfo("Deleted bill #" + billId);
        } catch (Exception e) {
            LoggingManager.logException("Error deleting bill #" + billId, e);
            throw new DatabaseException("Error deleting bill: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate total revenue from paid bills within a date range
     *
     * @param startDateTime Start date/time
     * @param endDateTime End date/time
     * @return Total revenue
     * @throws DatabaseException If there's an error calculating revenue
     */
    public double calculateTotalRevenue(LocalDateTime startDateTime, LocalDateTime endDateTime) throws DatabaseException {
        try {
            List<Billing> bills = billingDAO.findPaidByDateRange(startDateTime, endDateTime);
            double totalRevenue = 0.0;

            for (Billing bill : bills) {
                totalRevenue += bill.getTotalAmount();
            }

            return totalRevenue;
        } catch (Exception e) {
            LoggingManager.logException("Error calculating total revenue", e);
            throw new DatabaseException("Error calculating total revenue: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a printable bill summary
     *
     * @param billId The bill ID
     * @return The bill summary as a string
     * @throws DatabaseException If there's an error generating the summary
     */
    public String generateBillSummary(int billId) throws DatabaseException {
        try {
            Billing bill = getBillById(billId);
            if (bill == null) {
                throw new DatabaseException("Bill not found");
            }

            Reservation reservation = reservationService.getReservationById(bill.getReservationID());
            if (reservation == null) {
                throw new DatabaseException("Reservation not found");
            }

            return bill.generateBillSummary();
        } catch (Exception e) {
            LoggingManager.logException("Error generating bill summary for bill #" + billId, e);
            throw new DatabaseException("Error generating bill summary: " + e.getMessage(), e);
        }
    }

    /**
     * Save a new bill
     *
     * @param bill The bill to save
     * @return The generated bill ID
     * @throws DatabaseException If there's an error saving the bill
     */
    public int saveBill(Billing bill) throws DatabaseException {
        try {
            int billId = billingDAO.save(bill);
            LoggingManager.logSystemInfo("Saved bill #" + billId + " for reservation #" + bill.getReservationID());
            return billId;
        } catch (Exception e) {
            LoggingManager.logException("Error saving bill for reservation #" + bill.getReservationID(), e);
            throw new DatabaseException("Error saving bill: " + e.getMessage(), e);
        }
    }

    /**
     * Get bills by date range using LocalDate instead of LocalDateTime
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of bills within the date range
     * @throws DatabaseException If there's an error retrieving bills
     */
    public List<Billing> getBillingsByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        // Convert LocalDate to LocalDateTime (start of day and end of day)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return getBillsByDateRange(startDateTime, endDateTime);
    }
}