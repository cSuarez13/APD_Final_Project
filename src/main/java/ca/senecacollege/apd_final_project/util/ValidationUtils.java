package ca.senecacollege.apd_final_project.util;

import ca.senecacollege.apd_final_project.exception.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtils {
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Phone number validation pattern (accepts formats like: 123-456-7890, (123) 456-7890, 1234567890)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
    );

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void validateEmail(String email) throws ValidationException {
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email format. Please enter a valid email address.");
        }
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static void validatePhoneNumber(String phoneNumber) throws ValidationException {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new ValidationException("Invalid phone number format. Please enter a valid phone number.");
        }
    }

    public static boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static void validateNotNullOrEmpty(String value, String fieldName) throws ValidationException {
        if (!isNotNullOrEmpty(value)) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static boolean isPositiveInteger(String value) {
        if (!isNotNullOrEmpty(value)) {
            return false;
        }
        try {
            int intValue = Integer.parseInt(value);
            return intValue > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void validatePositiveInteger(String value, String fieldName) throws ValidationException {
        if (!isPositiveInteger(value)) {
            throw new ValidationException(fieldName + " must be a positive number.");
        }
    }

    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(endDate);
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate, String startFieldName, String endFieldName) throws ValidationException {
        if (startDate == null) {
            throw new ValidationException(startFieldName + " cannot be null.");
        }
        if (endDate == null) {
            throw new ValidationException(endFieldName + " cannot be null.");
        }
        if (!isValidDateRange(startDate, endDate)) {
            throw new ValidationException(startFieldName + " must be before or equal to " + endFieldName + ".");
        }
    }

    public static boolean isValidFutureDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(LocalDate.now());
    }

    public static void validateFutureDate(LocalDate date, String fieldName) throws ValidationException {
        if (!isValidFutureDate(date)) {
            throw new ValidationException(fieldName + " must be today or a future date.");
        }
    }

    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public static void validateRating(int rating) throws ValidationException {
        if (!isValidRating(rating)) {
            throw new ValidationException("Rating must be between 1 and 5.");
        }
    }
}