package ca.senecacollege.apd_final_project.controller.kiosk;

import java.time.LocalDate;

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

    // Default constructor
    public BookingData() {
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
                '}';
    }
}