package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.model.RoomType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold booking data as it passes through the booking flow screens
 */
public class BookingData {
    private int guestCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int singleRoomCount;
    private int doubleRoomCount;
    private int deluxeRoomCount;
    private int pentHouseCount;

    // Map to store how many guests are assigned to each room type
    private Map<RoomType, Integer> guestsPerRoomType = new HashMap<>();

    // Map to store specific room IDs assigned to this booking
    private Map<Integer, Integer> roomIdToGuestCount = new HashMap<>();

    // Default constructor
    public BookingData() {
        // Initialize the guests per room type map with zeros
        guestsPerRoomType.put(RoomType.SINGLE, 0);
        guestsPerRoomType.put(RoomType.DOUBLE, 0);
        guestsPerRoomType.put(RoomType.DELUXE, 0);
        guestsPerRoomType.put(RoomType.PENT_HOUSE, 0);
    }

    // Getters and setters
    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getSingleRoomCount() {
        return singleRoomCount;
    }

    public void setSingleRoomCount(int singleRoomCount) {
        this.singleRoomCount = singleRoomCount;
    }

    public int getDoubleRoomCount() {
        return doubleRoomCount;
    }

    public void setDoubleRoomCount(int doubleRoomCount) {
        this.doubleRoomCount = doubleRoomCount;
    }

    public int getDeluxeRoomCount() {
        return deluxeRoomCount;
    }

    public void setDeluxeRoomCount(int deluxeRoomCount) {
        this.deluxeRoomCount = deluxeRoomCount;
    }

    public int getPentHouseCount() {
        return pentHouseCount;
    }

    public void setPentHouseCount(int pentHouseCount) {
        this.pentHouseCount = pentHouseCount;
    }

    public Map<RoomType, Integer> getGuestsPerRoomType() {
        return guestsPerRoomType;
    }

    public void setGuestsPerRoomType(Map<RoomType, Integer> guestsPerRoomType) {
        this.guestsPerRoomType = guestsPerRoomType;
    }

    public int getGuestsForRoomType(RoomType roomType) {
        return guestsPerRoomType.getOrDefault(roomType, 0);
    }

    public Map<Integer, Integer> getRoomIdToGuestCount() {
        return roomIdToGuestCount;
    }

    public void setRoomIdToGuestCount(Map<Integer, Integer> roomIdToGuestCount) {
        this.roomIdToGuestCount = roomIdToGuestCount;
    }

    @Override
    public String toString() {
        return "BookingData{" +
                "guestCount=" + guestCount +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", singleRoomCount=" + singleRoomCount +
                ", doubleRoomCount=" + doubleRoomCount +
                ", deluxeRoomCount=" + deluxeRoomCount +
                ", pentHouseCount=" + pentHouseCount +
                ", guestsPerRoomType=" + guestsPerRoomType +
                ", roomIdToGuestCount=" + roomIdToGuestCount +
                '}';
    }
}