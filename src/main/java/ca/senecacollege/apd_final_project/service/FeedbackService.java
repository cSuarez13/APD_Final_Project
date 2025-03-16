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

    private FeedbackDAO feedbackDAO;

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
     * @return The generated feedback ID
     * @throws DatabaseException If there's an error saving the feedback
     */
    public int saveFeedback(Feedback feedback) throws DatabaseException {
        try {
            validateFeedback(feedback);
            int feedbackId = feedbackDAO.save(feedback);
            LoggingManager.logSystemInfo("Saved feedback for reservation #" + feedback.getReservationID() +
                    " with rating " + feedback.getRating());
            return feedbackId;
        } catch (Exception e) {
            LoggingManager.logException("Error saving feedback for reservation #" + feedback.getReservationID(), e);
            throw new DatabaseException("Error saving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Get a feedback by ID
     *
     * @param feedbackId The feedback ID
     * @return The feedback
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public Feedback getFeedbackById(int feedbackId) throws DatabaseException {
        try {
            return feedbackDAO.findById(feedbackId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving feedback #" + feedbackId, e);
            throw new DatabaseException("Error retrieving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Get feedback by guest ID
     *
     * @param guestId The guest ID
     * @return List of feedback from this guest
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public List<Feedback> getFeedbackByGuest(int guestId) throws DatabaseException {
        try {
            return feedbackDAO.findByGuest(guestId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving feedback for guest #" + guestId, e);
            throw new DatabaseException("Error retrieving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Get feedback by reservation ID
     *
     * @param reservationId The reservation ID
     * @return List of feedback for this reservation (usually just one)
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public List<Feedback> getFeedbackByReservation(int reservationId) throws DatabaseException {
        try {
            return feedbackDAO.findByReservation(reservationId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving feedback for reservation #" + reservationId, e);
            throw new DatabaseException("Error retrieving feedback: " + e.getMessage(), e);
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
     * Get all feedback with a specific rating
     *
     * @param rating The rating (1-5)
     * @return List of feedback with this rating
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public List<Feedback> getFeedbackByRating(int rating) throws DatabaseException {
        try {
            return feedbackDAO.findByRating(rating);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving feedback with rating " + rating, e);
            throw new DatabaseException("Error retrieving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Get all feedback
     *
     * @return List of all feedback
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public List<Feedback> getAllFeedback() throws DatabaseException {
        try {
            return feedbackDAO.findAll();
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving all feedback", e);
            throw new DatabaseException("Error retrieving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Update existing feedback
     *
     * @param feedback The feedback to update
     * @throws DatabaseException If there's an error updating the feedback
     */
    public void updateFeedback(Feedback feedback) throws DatabaseException {
        try {
            validateFeedback(feedback);
            feedbackDAO.update(feedback);
            LoggingManager.logSystemInfo("Updated feedback #" + feedback.getFeedbackID());
        } catch (Exception e) {
            LoggingManager.logException("Error updating feedback #" + feedback.getFeedbackID(), e);
            throw new DatabaseException("Error updating feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Delete feedback
     *
     * @param feedbackId The feedback ID to delete
     * @throws DatabaseException If there's an error deleting the feedback
     */
    public void deleteFeedback(int feedbackId) throws DatabaseException {
        try {
            feedbackDAO.delete(feedbackId);
            LoggingManager.logSystemInfo("Deleted feedback #" + feedbackId);
        } catch (Exception e) {
            LoggingManager.logException("Error deleting feedback #" + feedbackId, e);
            throw new DatabaseException("Error deleting feedback: " + e.getMessage(), e);
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
     * Get average rating across all feedback
     *
     * @return The average rating
     * @throws DatabaseException If there's an error calculating the average
     */
    public double getAverageRating() throws DatabaseException {
        try {
            return feedbackDAO.getAverageRating();
        } catch (Exception e) {
            LoggingManager.logException("Error calculating average rating", e);
            throw new DatabaseException("Error calculating average rating: " + e.getMessage(), e);
        }
    }

    /**
     * Get average rating for a specific date range
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return The average rating for the date range
     * @throws DatabaseException If there's an error calculating the average
     */
    public double getAverageRatingByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        try {
            return feedbackDAO.getAverageRatingByDateRange(startDate, endDate);
        } catch (Exception e) {
            LoggingManager.logException("Error calculating average rating for date range", e);
            throw new DatabaseException("Error calculating average rating: " + e.getMessage(), e);
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