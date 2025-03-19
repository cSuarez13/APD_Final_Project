package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.ReservationDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ReservationException;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private ReservationDAO reservationDAO;
    private RoomService roomService;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.roomService = new RoomService();
    }

    /**
     * Create a new reservation
     *
     * @param reservation The reservation details
     * @param roomType The desired room type
     * @return The reservation ID
     * @throws ReservationException If there's an error creating the reservation
     */
    public int createReservation(Reservation reservation, RoomType roomType) throws ReservationException {
        try {
            // Find an available room
            Room room = roomService.findAvailableRoom(roomType, reservation.getCheckInDate(), reservation.getCheckOutDate());

            if (room == null) {
                throw new ReservationException("No available rooms of type " + roomType.getDisplayName());
            }

            // Set the room ID in the reservation
            reservation.setRoomID(room.getRoomID());

            // Set initial status
            reservation.setStatus(Reservation.STATUS_CONFIRMED);

            // Save the reservation
            int reservationId = reservationDAO.save(reservation);

            // Mark the room as unavailable for the reservation dates
            roomService.setRoomAvailability(room.getRoomID(), false);

            LoggingManager.logSystemInfo("Created reservation #" + reservationId + " for room " + room.getRoomID());

            return reservationId;
        } catch (Exception e) {
            LoggingManager.logException("Error creating reservation", e);
            throw new ReservationException("Error creating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Get a reservation by ID
     *
     * @param reservationId The reservation ID
     * @return The reservation
     * @throws DatabaseException If there's an error retrieving the reservation
     */
    public Reservation getReservationById(int reservationId) throws DatabaseException {
        try {
            return reservationDAO.findById(reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving reservation #" + reservationId, e);
            throw new DatabaseException("Error retrieving reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Update a reservation
     *
     * @param reservation The reservation to update
     * @throws DatabaseException If there's an error updating the reservation
     */
    public void updateReservation(Reservation reservation) throws DatabaseException {
        try {
            reservationDAO.update(reservation);
            LoggingManager.logSystemInfo("Updated reservation #" + reservation.getReservationID());
        } catch (Exception e) {
            LoggingManager.logException("Error updating reservation #" + reservation.getReservationID(), e);
            throw new DatabaseException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel a reservation
     *
     * @param reservationId The reservation ID
     * @throws ReservationException If there's an error canceling the reservation
     */
    public void cancelReservation(int reservationId) throws ReservationException {
        try {
            // Get the reservation
            Reservation reservation = getReservationById(reservationId);

            // Update status
            reservation.setStatus(Reservation.STATUS_CANCELLED);
            updateReservation(reservation);

            // Make the room available again
            roomService.setRoomAvailability(reservation.getRoomID(), true);

            LoggingManager.logSystemInfo("Cancelled reservation #" + reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error canceling reservation #" + reservationId, e);
            throw new ReservationException("Error canceling reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Get reservations by guest ID
     *
     * @param guestId The guest ID
     * @return List of reservations for the guest
     * @throws DatabaseException If there's an error retrieving reservations
     */
    public List<Reservation> getReservationsByGuest(int guestId) throws DatabaseException {
        try {
            return reservationDAO.findByGuestId(guestId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving reservations for guest #" + guestId, e);
            throw new DatabaseException("Error retrieving reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Check in a guest
     *
     * @param reservationId The reservation ID
     * @throws ReservationException If there's an error checking in
     */
    public void checkIn(int reservationId) throws ReservationException {
        try {
            Reservation reservation = getReservationById(reservationId);

            // Verify reservation status
            if (!reservation.getStatus().equals(Reservation.STATUS_CONFIRMED)) {
                throw new ReservationException("Cannot check in: reservation is not confirmed");
            }

            // Update status
            reservation.setStatus(Reservation.STATUS_CHECKED_IN);
            updateReservation(reservation);

            LoggingManager.logSystemInfo("Checked in reservation #" + reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error checking in reservation #" + reservationId, e);
            throw new ReservationException("Error checking in: " + e.getMessage(), e);
        }
    }

    /**
     * Check out a guest
     *
     * @param reservationId The reservation ID
     * @throws ReservationException If there's an error checking out
     */
    public void checkOut(int reservationId) throws ReservationException {
        try {
            Reservation reservation = getReservationById(reservationId);

            // Verify reservation status
            if (!reservation.getStatus().equals(Reservation.STATUS_CHECKED_IN)) {
                throw new ReservationException("Cannot check out: guest is not checked in");
            }

            // Update status
            reservation.setStatus(Reservation.STATUS_CHECKED_OUT);
            updateReservation(reservation);

            // Make the room available again
            roomService.setRoomAvailability(reservation.getRoomID(), true);

            LoggingManager.logSystemInfo("Checked out reservation #" + reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error checking out reservation #" + reservationId, e);
            throw new ReservationException("Error checking out: " + e.getMessage(), e);
        }
    }

    /**
     * Get active reservations (confirmed or checked in)
     *
     * @return List of active reservations
     * @throws DatabaseException If there's an error retrieving reservations
     */
    public List<Reservation> getActiveReservations() throws DatabaseException {
        try {
            return reservationDAO.findByStatus(new String[]{
                    Reservation.STATUS_CONFIRMED,
                    Reservation.STATUS_CHECKED_IN
            });
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving active reservations", e);
            throw new DatabaseException("Error retrieving active reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Get reservations by date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of reservations in the date range
     * @throws DatabaseException If there's an error retrieving reservations
     */
    public List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        try {
            return reservationDAO.findByDateRange(startDate, endDate);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving reservations by date range", e);
            throw new DatabaseException("Error retrieving reservations: " + e.getMessage(), e);
        }
    }

    /**
     * Get reservations for check-in today
     *
     * @return List of today's check-ins
     * @throws DatabaseException If there's an error retrieving reservations
     */
    public List<Reservation> getTodayCheckIns() throws DatabaseException {
        try {
            return reservationDAO.findByCheckInDate(LocalDate.now());
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving today's check-ins", e);
            throw new DatabaseException("Error retrieving today's check-ins: " + e.getMessage(), e);
        }
    }

    /**
     * Get reservations for check-out today
     *
     * @return List of today's check-outs
     * @throws DatabaseException If there's an error retrieving reservations
     */
    public List<Reservation> getTodayCheckOuts() throws DatabaseException {
        try {
            return reservationDAO.findByCheckOutDate(LocalDate.now());
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving today's check-outs", e);
            throw new DatabaseException("Error retrieving today's check-outs: " + e.getMessage(), e);
        }
    }

    /**
     * Get reservations by status
     *
     * @param status The status to filter by
     * @return List of reservations with the specified status
     * @throws DatabaseException If there's an error retrieving reservations
     */
    public List<Reservation> getReservationsByStatus(String status) throws DatabaseException {
        try {
            return reservationDAO.findByStatus(new String[]{status});
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving reservations by status: " + status, e);
            throw new DatabaseException("Error retrieving reservations: " + e.getMessage(), e);
        }
    }
}