package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.BillingDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Billing;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class to handle all billing-related operations
 */
public class BillingService {

    private final BillingDAO billingDAO;
    private final ReservationService reservationService;

    /**
     * Constructor
     */
    public BillingService() {
        this.billingDAO = new BillingDAO();
        this.reservationService = new ReservationService();
    }

    /**
     * Get a bill by its ID
     *
     * @param billId The bill ID
     * @return The bill or null if not found
     * @throws DatabaseException If there's an error retrieving the bill
     */
    public Billing getBillById(int billId) throws DatabaseException {
        try {
            return billingDAO.findById(billId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving bill with ID: " + billId, e);
            throw new DatabaseException("Error retrieving bill: " + e.getMessage(), e);
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

    public ReservationService getReservationService() {
        return reservationService;
    }
}