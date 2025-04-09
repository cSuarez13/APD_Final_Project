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
            return true;
        }
        return !EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true;
        }
        return !PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static boolean isNotNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return true;
        }
        return startDate.isAfter(endDate);
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate, String startFieldName, String endFieldName) throws ValidationException {
        if (startDate == null) {
            throw new ValidationException(startFieldName + " cannot be null.");
        }
        if (endDate == null) {
            throw new ValidationException(endFieldName + " cannot be null.");
        }
        if (isValidDateRange(startDate, endDate)) {
            throw new ValidationException(startFieldName + " must be before or equal to " + endFieldName + ".");
        }
    }

    public static boolean isValidFutureDate(LocalDate date) {
        if (date == null) {
            return true;
        }
        return date.isBefore(LocalDate.now());
    }

}