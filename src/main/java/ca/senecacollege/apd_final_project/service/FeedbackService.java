package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.FeedbackDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Feedback;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class to handle all feedback-related operations
 */
public class FeedbackService {

    private final FeedbackDAO feedbackDAO;

    /**
     * Constructor
     */
    public FeedbackService() {
        this.feedbackDAO = new FeedbackDAO();
    }

    /**
     * Save a new feedback
     *
     * @param feedback The feedback to save
     * @throws DatabaseException If there's an error saving the feedback
     */
    public void saveFeedback(Feedback feedback) throws DatabaseException {
        try {
            validateFeedback(feedback);
            feedbackDAO.save(feedback);
            LoggingManager.logSystemInfo("Saved feedback for reservation #" + feedback.getReservationID() +
                    " with rating " + feedback.getRating());
        } catch (Exception e) {
            LoggingManager.logException("Error saving feedback for reservation #" + feedback.getReservationID(), e);
            throw new DatabaseException("Error saving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Get feedback by date range
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of feedback submitted in the date range
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public List<Feedback> getFeedbackByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        try {
            return feedbackDAO.findByDateRange(startDate, endDate);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving feedback for date range", e);
            throw new DatabaseException("Error retrieving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Check if feedback exists for a reservation
     *
     * @param reservationId The reservation ID
     * @return true if feedback exists, false otherwise
     * @throws DatabaseException If there's an error checking for feedback
     */
    public boolean checkFeedbackExists(int reservationId) throws DatabaseException {
        try {
            return feedbackDAO.checkFeedbackExists(reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error checking if feedback exists for reservation #" + reservationId, e);
            throw new DatabaseException("Error checking for feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Validate feedback data
     *
     * @param feedback The feedback to validate
     * @throws IllegalArgumentException If the feedback data is invalid
     */
    private void validateFeedback(Feedback feedback) throws IllegalArgumentException {
        // Validate rating (must be between 1 and 5)
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Validate guest ID
        if (feedback.getGuestID() <= 0) {
            throw new IllegalArgumentException("Invalid guest ID");
        }

        // Validate reservation ID
        if (feedback.getReservationID() <= 0) {
            throw new IllegalArgumentException("Invalid reservation ID");
        }

        // Validate submission date/time (not in the future)
        if (feedback.getSubmissionDateTime() != null &&
                feedback.getSubmissionDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Submission date/time cannot be in the future");
        }
    }
}