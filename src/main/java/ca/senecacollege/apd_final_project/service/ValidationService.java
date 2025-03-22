package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.util.ValidationUtils;

import java.time.LocalDate;

/**
 * Centralized validation service for validating entities and input data
 */
public class ValidationService {

    /**
     * Validate guest data
     *
     * @param guest The guest to validate
     * @throws ValidationException If the guest data is invalid
     */
    public void validateGuest(Guest guest) throws ValidationException {
        // Name validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getName())) {
            throw new ValidationException("Guest name cannot be empty");
        }

        // Phone validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getPhoneNumber())) {
            throw new ValidationException("Guest phone number cannot be empty");
        }

        if (!ValidationUtils.isValidPhoneNumber(guest.getPhoneNumber())) {
            throw new ValidationException("Invalid phone number format");
        }

        // Email validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getEmail())) {
            throw new ValidationException("Guest email cannot be empty");
        }

        if (!ValidationUtils.isValidEmail(guest.getEmail())) {
            throw new ValidationException("Invalid email format");
        }

        // Address validation
        if (!ValidationUtils.isNotNullOrEmpty(guest.getAddress())) {
            throw new ValidationException("Guest address cannot be empty");
        }
    }

    /**
     * Validate reservation data
     *
     * @param reservation The reservation to validate
     * @throws ValidationException If the reservation data is invalid
     */
    public void validateReservation(Reservation reservation) throws ValidationException {
        // Guest ID validation
        if (reservation.getGuestID() <= 0) {
            throw new ValidationException("Invalid guest ID");
        }

        // Room ID validation
        if (reservation.getRoomID() <= 0) {
            throw new ValidationException("Invalid room ID");
        }

        // Check-in date validation
        if (reservation.getCheckInDate() == null) {
            throw new ValidationException("Check-in date cannot be null");
        }

        // Check-out date validation
        if (reservation.getCheckOutDate() == null) {
            throw new ValidationException("Check-out date cannot be null");
        }

        // Date range validation
        if (!ValidationUtils.isValidDateRange(reservation.getCheckInDate(), reservation.getCheckOutDate())) {
            throw new ValidationException("Check-out date must be after check-in date");
        }

        // Number of guests validation
        if (reservation.getNumberOfGuests() <= 0) {
            throw new ValidationException("Number of guests must be greater than zero");
        }
    }

    /**
     * Validate check-in action
     *
     * @param reservation The reservation to check in
     * @throws ValidationException If the reservation cannot be checked in
     */
    public void validateCheckIn(Reservation reservation) throws ValidationException {
        if (reservation == null) {
            throw new ValidationException("Reservation not found");
        }

        if (!reservation.getStatus().equals(Reservation.STATUS_CONFIRMED)) {
            throw new ValidationException("Cannot check in: reservation is not confirmed");
        }

        // Check if check-in date is today or in the past
        if (reservation.getCheckInDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Cannot check in: check-in date is in the future");
        }
    }

    /**
     * Validate check-out action
     *
     * @param reservation The reservation to check out
     * @throws ValidationException If the reservation cannot be checked out
     */
    public void validateCheckOut(Reservation reservation) throws ValidationException {
        if (reservation == null) {
            throw new ValidationException("Reservation not found");
        }

        if (!reservation.getStatus().equals(Reservation.STATUS_CHECKED_IN)) {
            throw new ValidationException("Cannot check out: guest is not checked in");
        }
    }

    /**
     * Validate ID format
     *
     * @param id The ID to validate
     * @param fieldName The name of the field
     * @throws ValidationException If the ID is invalid
     */
    public void validateId(String id, String fieldName) throws ValidationException {
        if (!ValidationUtils.isNotNullOrEmpty(id)) {
            throw new ValidationException(fieldName + " cannot be empty");
        }

        if (!ValidationUtils.isPositiveInteger(id)) {
            throw new ValidationException(fieldName + " must be a positive number");
        }
    }

    /**
     * Validate billing data
     *
     * @param amount The amount
     * @param discount The discount
     * @throws ValidationException If the billing data is invalid
     */
    public void validateBilling(double amount, double discount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }

        if (discount < 0) {
            throw new ValidationException("Discount cannot be negative");
        }

        if (discount > amount) {
            throw new ValidationException("Discount cannot be greater than amount");
        }
    }
}