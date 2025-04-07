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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            if (!roomsWithGuests.isEmpty()) {
                // Set the first room as the "primary" room for backward compatibility
                reservation.setRoomID(roomsWithGuests.keySet().iterator().next());
            }
            int reservationId = reservationDAO.save(reservation);

            // Now save each room assignment
            for (Map.Entry<Integer, Integer> entry : roomsWithGuests.entrySet()) {
                int roomId = entry.getKey();
                int guestsInRoom = entry.getValue();

                // Get the room to access its price
                Room room = roomService.getRoomById(roomId);
                double pricePerNight = room.getPrice();

                // Create and save reservation-room relationship
                ReservationRoom reservationRoom = new ReservationRoom();
                reservationRoom.setReservationID(reservationId);
                reservationRoom.setRoomID(roomId);
                reservationRoom.setGuestsInRoom(guestsInRoom);
                reservationRoom.setPricePerNight(pricePerNight);

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
            ReservationRoom reservationRoom = new ReservationRoom();
            reservationRoom.setReservationID(reservationId);
            reservationRoom.setRoomID(room.getRoomID());
            reservationRoom.setGuestsInRoom(reservation.getNumberOfGuests());
            reservationRoom.setPricePerNight(room.getPrice());

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

    /**
     * Creates a reservation with multiple rooms
     * @param reservation Reservation details
     * @param rooms List of ReservationRoom objects
     * @return reservation ID
     * @throws ReservationException if validation fails
     */
    public int createReservationWithRooms(Reservation reservation, List<ReservationRoom> rooms)
            throws ReservationException {
        try {
            // Validate rooms first
            Set<Integer> roomIds = new HashSet<>();
            for (ReservationRoom room : rooms) {
                if (!roomIds.add(room.getRoomID())) {
                    throw new ReservationException("Duplicate room ID: " + room.getRoomID());
                }

                Room roomData = roomService.getRoomById(room.getRoomID());
                if (roomData == null) {
                    throw new ReservationException("Room #" + room.getRoomID() + " doesn't exist");
                }
                if (!roomData.isAvailable()) {
                    throw new ReservationException("Room #" + room.getRoomID() + " is not available");
                }
            }

            // Save reservation
            reservation.setStatus(ReservationStatus.CONFIRMED);
            if (!rooms.isEmpty()) {
                reservation.setRoomID(rooms.get(0).getRoomID()); // Set primary room
            }
            int reservationId = reservationDAO.save(reservation);

            // Save rooms
            for (ReservationRoom room : rooms) {
                room.setReservationID(reservationId);
                reservationRoomDAO.save(room);
                roomService.setRoomAvailability(room.getRoomID(), false);
            }

            return reservationId;
        } catch (Exception e) {
            throw new ReservationException("Failed to create reservation: " + e.getMessage(), e);
        }
    }

}