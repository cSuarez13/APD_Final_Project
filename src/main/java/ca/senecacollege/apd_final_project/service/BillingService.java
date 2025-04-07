package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.BillingDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Billing;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationRoom;
import ca.senecacollege.apd_final_project.model.Room;
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
    private final RoomService roomService;

    /**
     * Constructor
     */
    public BillingService() {
        this.billingDAO = new BillingDAO();
        this.reservationService = new ReservationService();
        this.roomService = new RoomService();
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

    /**
     * Generate a new bill for a reservation with multiple rooms
     *
     * @param reservationId The reservation ID
     * @param discount The discount amount (0 for no discount)
     * @return The generated bill
     * @throws DatabaseException If there's an error generating the bill
     */
    public Billing generateBill(int reservationId, double discount) throws DatabaseException {
        try {
            // Get the reservation
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation == null) {
                throw new DatabaseException("Reservation not found");
            }

            // Create a new bill
            Billing bill = new Billing();
            bill.setReservationID(reservationId);
            bill.setBillingDateTime(LocalDateTime.now());
            bill.setPaid(false);
            bill.setDiscount(discount);

            // Get all rooms for this reservation
            List<ReservationRoom> reservationRooms = reservationService.getReservationRooms(reservationId);
            double totalAmount = 0.0;

            // Calculate nights
            int nights = reservation.calculateNumberOfNights();

            // Add billing items for each room
            for (ReservationRoom reservationRoom : reservationRooms) {
                Room room = roomService.getRoomById(reservationRoom.getRoomID());

                // Create a billing item
                Billing.BillingItem item = new Billing.BillingItem(
                        room.getRoomID(),
                        room.getRoomType().getDisplayName(),
                        nights,
                        room.getPrice()
                );

                // Add to the bill
                bill.addBillingItem(item);

                // Add to total
                totalAmount += item.getSubtotal();
            }

            // Set the total amount
            bill.setAmount(totalAmount);

            // Save the bill
            int billId = saveBill(bill);
            bill.setBillID(billId);

            return bill;
        } catch (Exception e) {
            LoggingManager.logException("Error generating bill for reservation #" + reservationId, e);
            throw new DatabaseException("Error generating bill: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate the total bill amount for a reservation without saving it
     *
     * @param reservationId The reservation ID
     * @return The calculated bill
     * @throws DatabaseException If there's an error calculating the bill
     */
    public Billing calculateBill(int reservationId) throws DatabaseException {
        try {
            // Get the reservation
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation == null) {
                throw new DatabaseException("Reservation not found");
            }

            // Create a temporary bill object
            Billing bill = new Billing();
            bill.setReservationID(reservationId);
            bill.setDiscount(0); // No discount by default

            // Get all rooms for this reservation
            List<ReservationRoom> reservationRooms = reservationService.getReservationRooms(reservationId);

            // Calculate nights
            int nights = reservation.calculateNumberOfNights();

            // Add billing items for each room
            for (ReservationRoom reservationRoom : reservationRooms) {
                Room room = roomService.getRoomById(reservationRoom.getRoomID());

                // Create a billing item
                Billing.BillingItem item = new Billing.BillingItem(
                        room.getRoomID(),
                        room.getRoomType().getDisplayName(),
                        nights,
                        room.getPrice()
                );

                // Add to the bill
                bill.addBillingItem(item);
            }

            return bill;
        } catch (Exception e) {
            LoggingManager.logException("Error calculating bill for reservation #" + reservationId, e);
            throw new DatabaseException("Error calculating bill: " + e.getMessage(), e);
        }
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public RoomService getRoomService() {
        return roomService;
    }
}