package ca.senecacollege.apd_final_project.model;

/**
 * Enum representing the possible states of a reservation
 */
public enum ReservationStatus {
    PENDING("Pending", "A reservation that has been created but not yet confirmed"),
    CONFIRMED("Confirmed", "A reservation that has been confirmed but guest has not yet checked in"),
    CHECKED_IN("Checked In", "Guest has checked in and is currently staying at the hotel"),
    CHECKED_OUT("Checked Out", "Guest has completed their stay and checked out"),
    CANCELLED("Cancelled", "Reservation has been cancelled");

    private final String displayName;
    private final String description;

    ReservationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get the display name of the status
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the description of the status
     * @return The description
     */
    public String getDescription() {
        return description;
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

    /**
     * Check if a transition from this status to the target status is valid
     * @param targetStatus The target status
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(ReservationStatus targetStatus) {
        switch (this) {
            case PENDING:
                // Pending can transition to confirmed or cancelled
                return targetStatus == CONFIRMED || targetStatus == CANCELLED;
            case CONFIRMED:
                // Confirmed can transition to checked in or cancelled
                return targetStatus == CHECKED_IN || targetStatus == CANCELLED;
            case CHECKED_IN:
                // Checked in can only transition to checked out
                return targetStatus == CHECKED_OUT;
            case CHECKED_OUT:
            case CANCELLED:
                // Terminal states cannot transition
                return false;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}