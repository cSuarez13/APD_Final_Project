package ca.senecacollege.apd_final_project.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Feedback {
    private final IntegerProperty feedbackID = new SimpleIntegerProperty(this, "feedbackID");
    private final IntegerProperty guestID = new SimpleIntegerProperty(this, "guestID");
    private final IntegerProperty reservationID = new SimpleIntegerProperty(this, "reservationID");
    private final StringProperty comments = new SimpleStringProperty(this, "comments");
    private final IntegerProperty rating = new SimpleIntegerProperty(this, "rating");
    private final ObjectProperty<LocalDateTime> submissionDateTime = new SimpleObjectProperty<>(this, "submissionDateTime");

    public Feedback() {
        // Default constructor
    }

    public Feedback(int feedbackID, int guestID, int reservationID, String comments, int rating, LocalDateTime submissionDateTime) {
        this.feedbackID.set(feedbackID);
        this.guestID.set(guestID);
        this.reservationID.set(reservationID);
        this.comments.set(comments);
        this.rating.set(rating);
        this.submissionDateTime.set(submissionDateTime);
    }

    public int getFeedbackID() {
        return feedbackID.get();
    }

    public void setFeedbackID(int feedbackID) {
        this.feedbackID.set(feedbackID);
    }

    public int getGuestID() {
        return guestID.get();
    }

    public void setGuestID(int guestID) {
        this.guestID.set(guestID);
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }

    public String getComments() {
        return comments.get();
    }

    public void setComments(String comments) {
        this.comments.set(comments);
    }

    public int getRating() {
        return rating.get();
    }

    public void setRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating.set(rating);
        } else {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
    }

    public LocalDateTime getSubmissionDateTime() {
        return submissionDateTime.get();
    }

    public void setSubmissionDateTime(LocalDateTime submissionDateTime) {
        this.submissionDateTime.set(submissionDateTime);
    }

    @Override
    public String toString() {
        return "Feedback #" + getFeedbackID() + " - Rating: " + getRating() + "/5 - " +
                (getComments() != null && !getComments().isEmpty()
                        ? "With comments" : "No comments");
    }
}