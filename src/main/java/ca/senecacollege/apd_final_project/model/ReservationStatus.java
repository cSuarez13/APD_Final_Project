package ca.senecacollege.apd_final_project.model;

/**
 * Enum representing the possible states of a reservation
 */
public enum ReservationStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked In"),
    CHECKED_OUT("Checked Out"),
    CANCELLED("Cancelled");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the display name of the status
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the status from its display name
     * @param displayName The display name
     * @return The status, or null if not found
     */
    public static ReservationStatus fromDisplayName(String displayName) {
        for (ReservationStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}