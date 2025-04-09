package ca.senecacollege.apd_final_project.dao;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Feedback;
import ca.senecacollege.apd_final_project.util.DatabaseConnection;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    /**
     * Save new feedback
     *
     * @param feedback The feedback to save
     * @return The generated feedback ID
     * @throws DatabaseException If there's an error saving the feedback
     */
    public int save(Feedback feedback) throws DatabaseException {
        String sql = "INSERT INTO feedback (guest_id, reservation_id, rating, comments, submission_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, feedback.getGuestID());
            stmt.setInt(2, feedback.getReservationID());
            stmt.setInt(3, feedback.getRating());
            stmt.setString(4, feedback.getComments());
            stmt.setTimestamp(5, Timestamp.valueOf(feedback.getSubmissionDateTime()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating feedback failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int feedbackId = generatedKeys.getInt(1);
                    feedback.setFeedbackID(feedbackId);
                    return feedbackId;
                } else {
                    throw new DatabaseException("Creating feedback failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while saving feedback", e);
            throw new DatabaseException("Error saving feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Find feedback by date range
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of feedback in the date range
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public List<Feedback> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT * FROM feedback WHERE DATE(submission_date) BETWEEN ? AND ? ORDER BY submission_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(startDate));
            stmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                List<Feedback> feedbackList = new ArrayList<>();

                while (rs.next()) {
                    feedbackList.add(mapResultSetToFeedback(rs));
                }

                return feedbackList;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding feedback by date range", e);
            throw new DatabaseException("Error finding feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Find feedback by reservation ID
     *
     * @param reservationId The reservation ID
     * @return List of feedback for the specified reservation
     * @throws DatabaseException If there's an error retrieving the feedback
     */
    public List<Feedback> findByReservationId(int reservationId) throws DatabaseException {
        String sql = "SELECT * FROM feedback WHERE reservation_id = ? ORDER BY submission_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Feedback> feedbackList = new ArrayList<>();

                while (rs.next()) {
                    feedbackList.add(mapResultSetToFeedback(rs));
                }

                return feedbackList;
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while finding feedback by reservation ID", e);
            throw new DatabaseException("Error finding feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Update feedback
     *
     * @param feedback The feedback to update
     * @throws DatabaseException If there's an error updating the feedback
     */
    public void update(Feedback feedback) throws DatabaseException {
        String sql = "UPDATE feedback SET rating = ?, comments = ? WHERE feedback_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feedback.getRating());
            stmt.setString(2, feedback.getComments());
            stmt.setInt(3, feedback.getFeedbackID());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating feedback failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating feedback", e);
            throw new DatabaseException("Error updating feedback: " + e.getMessage(), e);
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
        String sql = "SELECT COUNT(*) FROM feedback WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while checking if feedback exists", e);
            throw new DatabaseException("Error checking for feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Map ResultSet to Feedback object
     *
     * @param rs The ResultSet
     * @return The mapped Feedback object
     * @throws SQLException If there's an error accessing the ResultSet
     */
    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setFeedbackID(rs.getInt("feedback_id"));
        feedback.setGuestID(rs.getInt("guest_id"));
        feedback.setReservationID(rs.getInt("reservation_id"));
        feedback.setRating(rs.getInt("rating"));
        feedback.setComments(rs.getString("comments"));
        feedback.setSubmissionDateTime(rs.getTimestamp("submission_date").toLocalDateTime());
        return feedback;
    }
}