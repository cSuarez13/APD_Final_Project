package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;

import java.time.LocalDate;

/**
 * Centralized validation service for validating entities and input data
 */
public class ValidationService {

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

        if (!reservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
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

        if (!reservation.getStatus().equals(ReservationStatus.CHECKED_IN)) {
            throw new ValidationException("Cannot check out: guest is not checked in");
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