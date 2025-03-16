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

    // FeedbackID property
    public IntegerProperty feedbackIDProperty() {
        return feedbackID;
    }

    public int getFeedbackID() {
        return feedbackID.get();
    }

    public void setFeedbackID(int feedbackID) {
        this.feedbackID.set(feedbackID);
    }

    // GuestID property
    public IntegerProperty guestIDProperty() {
        return guestID;
    }

    public int getGuestID() {
        return guestID.get();
    }

    public void setGuestID(int guestID) {
        this.guestID.set(guestID);
    }

    // ReservationID property
    public IntegerProperty reservationIDProperty() {
        return reservationID;
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public void setReservationID(int reservationID) {
        this.reservationID.set(reservationID);
    }

    // Comments property
    public StringProperty commentsProperty() {
        return comments;
    }

    public String getComments() {
        return comments.get();
    }

    public void setComments(String comments) {
        this.comments.set(comments);
    }

    // Rating property
    public IntegerProperty ratingProperty() {
        return rating;
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

    // SubmissionDateTime property
    public ObjectProperty<LocalDateTime> submissionDateTimeProperty() {
        return submissionDateTime;
    }

    public LocalDateTime getSubmissionDateTime() {
        return submissionDateTime.get();
    }

    public void setSubmissionDateTime(LocalDateTime submissionDateTime) {
        this.submissionDateTime.set(submissionDateTime);
    }

    // Methods
    public void submitFeedback(int guestID, int reservationID, String comments, int rating) {
        setGuestID(guestID);
        setReservationID(reservationID);
        setComments(comments);
        setRating(rating);
        setSubmissionDateTime(LocalDateTime.now());
    }

    public String getFeedbackDetails() {
        return "Feedback #" + getFeedbackID() +
                "\nGuest ID: " + getGuestID() +
                "\nReservation ID: " + getReservationID() +
                "\nRating: " + getRating() + "/5" +
                "\nComments: " + getComments() +
                "\nSubmitted: " + getSubmissionDateTime();
    }

    @Override
    public String toString() {
        return "Feedback #" + getFeedbackID() + " - Rating: " + getRating() + "/5";
    }
}