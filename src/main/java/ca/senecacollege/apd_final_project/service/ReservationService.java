package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.ReservationDAO;
import ca.senecacollege.apd_final_project.dao.ReservationRoomDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ReservationException;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationRoom;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.model.Room;
import ca.senecacollege.apd_final_project.model.RoomType;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    public final ReservationRoomDAO reservationRoomDAO;
    private final RoomService roomService;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.reservationRoomDAO = new ReservationRoomDAO();
        this.roomService = new RoomService();
    }

    /**
     * Create a new reservation with multiple rooms
     *
     * @param reservation The reservation details
     * @param roomsWithGuests Map of room IDs to number of guests in that room
     * @return The reservation ID
     * @throws ReservationException If there's an error creating the reservation
     */
    public int createReservation(Reservation reservation, Map<Integer, Integer> roomsWithGuests) throws ReservationException {
        try {
            // Validate rooms are available
            for (int roomId : roomsWithGuests.keySet()) {
                Room room = roomService.getRoomById(roomId);
                if (room == null) {
                    throw new ReservationException("Room #" + roomId + " does not exist");
                }

                if (!room.isAvailable()) {
                    throw new ReservationException("Room #" + roomId + " is not available");
                }

                // Check room capacity
                int guestsInRoom = roomsWithGuests.get(roomId);
                if (guestsInRoom > room.getRoomType().getMaxOccupancy()) {
                    throw new ReservationException("Room #" + roomId + " cannot accommodate " +
                            guestsInRoom + " guests. Maximum capacity is " +
                            room.getRoomType().getMaxOccupancy());
                }
            }

            // Set initial status
            reservation.setStatus(ReservationStatus.CONFIRMED);

            // Save the reservation first (with a placeholder room ID)
            int reservationId = reservationDAO.save(reservation);

            // Now save each room assignment
            for (Map.Entry<Integer, Integer> entry : roomsWithGuests.entrySet()) {
                int roomId = entry.getKey();
                int guestsInRoom = entry.getValue();

                ReservationRoom reservationRoom = new ReservationRoom(reservationId, roomId, guestsInRoom);
                reservationRoomDAO.save(reservationRoom);

                // Mark the room as unavailable
                roomService.setRoomAvailability(roomId, false);
            }

            LoggingManager.logSystemInfo("Created reservation #" + reservationId + " with " +
                    roomsWithGuests.size() + " rooms");

            return reservationId;
        } catch (Exception e) {
            LoggingManager.logException("Error creating reservation", e);
            throw new ReservationException("Error creating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new reservation with a single room (for backward compatibility)
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
            reservation.setStatus(ReservationStatus.CONFIRMED);

            // Save the reservation
            int reservationId = reservationDAO.save(reservation);

            // Create a reservation-room relationship
            ReservationRoom reservationRoom = new ReservationRoom(reservationId, room.getRoomID(),
                    reservation.getNumberOfGuests());
            reservationRoomDAO.save(reservationRoom);

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
     * Get all rooms for a reservation
     *
     * @param reservationId The reservation ID
     * @return List of rooms associated with the reservation
     * @throws DatabaseException If there's an error retrieving the rooms
     */
    public List<Room> getRoomsForReservation(int reservationId) throws DatabaseException {
        return reservationRoomDAO.findRoomsByReservationId(reservationId);
    }

    /**
     * Get all reservation-room relationships for a reservation
     *
     * @param reservationId The reservation ID
     * @return List of reservation-room relationships
     * @throws DatabaseException If there's an error retrieving the relationships
     */
    public List<ReservationRoom> getReservationRooms(int reservationId) throws DatabaseException {
        return reservationRoomDAO.findByReservationId(reservationId);
    }

    /**
     * Add a room to an existing reservation
     *
     * @param reservationId The reservation ID
     * @param roomId The room ID to add
     * @param guestsInRoom Number of guests assigned to this room
     * @throws ReservationException If there's an error adding the room
     */
    public void addRoomToReservation(int reservationId, int roomId, int guestsInRoom) throws ReservationException {
        try {
            // Get the reservation
            Reservation reservation = getReservationById(reservationId);
            if (reservation == null) {
                throw new ReservationException("Reservation not found");
            }

            // Check reservation status
            if (reservation.getStatus() != ReservationStatus.CONFIRMED &&
                    reservation.getStatus() != ReservationStatus.PENDING) {
                throw new ReservationException("Cannot add room to reservation with status: " +
                        reservation.getStatus().getDisplayName());
            }

            // Check if room is already assigned to this reservation
            if (reservationRoomDAO.isRoomBookedForReservation(reservationId, roomId)) {
                throw new ReservationException("Room #" + roomId + " is already assigned to this reservation");
            }

            // Check if room exists and is available
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                throw new ReservationException("Room #" + roomId + " does not exist");
            }

            if (!room.isAvailable()) {
                throw new ReservationException("Room #" + roomId + " is not available");
            }

            // Check room capacity
            if (guestsInRoom > room.getRoomType().getMaxOccupancy()) {
                throw new ReservationException("Room #" + roomId + " cannot accommodate " +
                        guestsInRoom + " guests. Maximum capacity is " +
                        room.getRoomType().getMaxOccupancy());
            }

            // Create the reservation-room relationship
            ReservationRoom reservationRoom = new ReservationRoom(reservationId, roomId, guestsInRoom);
            reservationRoomDAO.save(reservationRoom);

            // Mark the room as unavailable
            roomService.setRoomAvailability(roomId, false);

            // Update the total number of guests in the reservation
            updateTotalGuestCount(reservationId);

            LoggingManager.logSystemInfo("Added room #" + roomId + " to reservation #" + reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error adding room to reservation", e);
            throw new ReservationException("Error adding room to reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Remove a room from an existing reservation
     *
     * @param reservationId The reservation ID
     * @param roomId The room ID to remove
     * @throws ReservationException If there's an error removing the room
     */
    public void removeRoomFromReservation(int reservationId, int roomId) throws ReservationException {
        try {
            // Get the reservation
            Reservation reservation = getReservationById(reservationId);
            if (reservation == null) {
                throw new ReservationException("Reservation not found");
            }

            // Check reservation status
            if (reservation.getStatus() != ReservationStatus.CONFIRMED &&
                    reservation.getStatus() != ReservationStatus.PENDING) {
                throw new ReservationException("Cannot remove room from reservation with status: " +
                        reservation.getStatus().getDisplayName());
            }

            // Get the reservation-room relationship
            ReservationRoom reservationRoom = reservationRoomDAO.findByReservationAndRoom(reservationId, roomId);
            if (reservationRoom == null) {
                throw new ReservationException("Room #" + roomId + " is not assigned to this reservation");
            }

            // Check if this is the only room (can't remove all rooms)
            List<ReservationRoom> rooms = reservationRoomDAO.findByReservationId(reservationId);
            if (rooms.size() == 1) {
                throw new ReservationException("Cannot remove the only room from a reservation");
            }

            // Delete the relationship
            reservationRoomDAO.delete(reservationRoom.getId());

            // Make the room available again
            roomService.setRoomAvailability(roomId, true);

            // Update the total number of guests in the reservation
            updateTotalGuestCount(reservationId);

            LoggingManager.logSystemInfo("Removed room #" + roomId + " from reservation #" + reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error removing room from reservation", e);
            throw new ReservationException("Error removing room from reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Update the total guest count in a reservation based on the sum of guests in all rooms
     *
     * @param reservationId The reservation ID
     * @throws DatabaseException If there's an error updating the reservation
     */
    private void updateTotalGuestCount(int reservationId) throws DatabaseException {
        try {
            // Get all reservation-room relationships
            List<ReservationRoom> rooms = reservationRoomDAO.findByReservationId(reservationId);

            // Calculate total guests
            int totalGuests = 0;
            for (ReservationRoom room : rooms) {
                totalGuests += room.getGuestsInRoom();
            }

            // Update the reservation
            Reservation reservation = getReservationById(reservationId);
            reservation.setNumberOfGuests(totalGuests);
            updateReservation(reservation);

            LoggingManager.logSystemInfo("Updated total guest count for reservation #" +
                    reservationId + " to " + totalGuests);
        } catch (Exception e) {
            LoggingManager.logException("Error updating total guest count", e);
            throw new DatabaseException("Error updating total guest count: " + e.getMessage(), e);
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
            reservation.setStatus(ReservationStatus.CANCELLED);
            updateReservation(reservation);

            // Get all rooms associated with this reservation
            List<Room> rooms = getRoomsForReservation(reservationId);

            // Make all rooms available again
            for (Room room : rooms) {
                roomService.setRoomAvailability(room.getRoomID(), true);
            }

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
            if (!reservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
                throw new ReservationException("Cannot check in: reservation is not confirmed");
            }

            // Update status
            reservation.setStatus(ReservationStatus.CHECKED_IN);
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
            if (!reservation.getStatus().equals(ReservationStatus.CHECKED_IN)) {
                throw new ReservationException("Cannot check out: guest is not checked in");
            }

            // Update status
            reservation.setStatus(ReservationStatus.CHECKED_OUT);
            updateReservation(reservation);

            // Get all rooms associated with this reservation
            List<Room> rooms = getRoomsForReservation(reservationId);

            // Make all rooms available again
            for (Room room : rooms) {
                roomService.setRoomAvailability(room.getRoomID(), true);
            }

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
                    ReservationStatus.CONFIRMED.getDisplayName(),
                    ReservationStatus.CHECKED_IN.getDisplayName()
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
    public List<Reservation> getReservationsByStatus(ReservationStatus status) throws DatabaseException {
        try {
            return reservationDAO.findByStatus(new String[]{status.getDisplayName()});
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving reservations by status: " + status, e);
            throw new DatabaseException("Error retrieving reservations: " + e.getMessage(), e);
        }
    }

    public int save(Reservation reservation) throws DatabaseException {
        return reservationDAO.save(reservation);
    }

}