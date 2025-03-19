package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Feedback;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.service.FeedbackService;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.scene.Node;

public class FeedbackController implements Initializable {

    @FXML
    private TextField txtReservationId;

    @FXML
    private Button btnVerify;

    @FXML
    private Label lblGuestName;

    @FXML
    private Label lblCheckInDate;

    @FXML
    private Label lblCheckOutDate;

    @FXML
    private Label lblRoomInfo;

    @FXML
    private HBox starsContainer;

    @FXML
    private TextArea txtComments;

    @FXML
    private Button btnSubmit;

    @FXML
    private Button btnCancel;

    @FXML
    private Label lblError;

    @FXML
    private Label lblThankYou;

    private FeedbackService feedbackService;
    private GuestService guestService;
    private ReservationService reservationService;

    private Reservation currentReservation;
    private Guest currentGuest;
    private int selectedRating = 0;
    private Node[] starControls = new Node[5];

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        feedbackService = new FeedbackService();
        guestService = new GuestService();
        reservationService = new ReservationService();

        // Initialize rating stars
        setupRatingStars();

        // Hide the thank you message initially
        lblThankYou.setVisible(false);

        // Hide error message initially
        lblError.setVisible(false);

        // Disable submit button initially
        btnSubmit.setDisable(true);

        LoggingManager.logSystemInfo("FeedbackController initialized");
    }

    private void setupRatingStars() {
        // Modify the method to use Node instead of Control
        for (int i = 0; i < 5 && i < starsContainer.getChildren().size(); i++) {
            final int rating = i + 1;
            Node star = starsContainer.getChildren().get(i);
            starControls[i] = star;

            // Add style class for initial state
            star.getStyleClass().add("rating-star");

            // Add click event
            star.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> setRating(rating));

            // Add hover effects
            star.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> showTemporaryRating(rating));
            star.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                resetStars();
                updateStarsForRating(selectedRating);
            });
        }
    }

    /**
     * Set the rating value
     *
     * @param rating The rating (1-5)
     */
    private void setRating(int rating) {
        selectedRating = rating;
        resetStars();
        updateStarsForRating(rating);

        // Enable submit button if reservation is verified and rating is selected
        updateSubmitButtonState();
    }

    /**
     * Show temporary rating when hovering
     *
     * @param rating The temporary rating to display
     */
    private void showTemporaryRating(int rating) {
        resetStars();
        updateStarsForRating(rating);
    }

    private void resetStars() {
        for (Node star : starControls) {
            if (star != null) {
                star.getStyleClass().remove("rating-star-filled");
            }
        }
    }

    private void updateStarsForRating(int rating) {
        for (int i = 0; i < rating && i < starControls.length; i++) {
            if (starControls[i] != null) {
                starControls[i].getStyleClass().add("rating-star-filled");
            }
        }
    }

    /**
     * Update the state of the submit button
     */
    private void updateSubmitButtonState() {
        btnSubmit.setDisable(currentReservation == null || selectedRating == 0);
    }

    /**
     * Handle verify reservation button action
     */
    @FXML
    private void handleVerifyButton(ActionEvent event) {
        // Clear previous data
        clearReservationInfo();

        String reservationIdText = txtReservationId.getText().trim();

        // Validate input
        if (reservationIdText.isEmpty()) {
            showError("Please enter a reservation ID");
            return;
        }

        try {
            int reservationId = Integer.parseInt(reservationIdText);

            // Fetch reservation
            currentReservation = reservationService.getReservationById(reservationId);

            if (currentReservation == null) {
                showError("Reservation not found");
                return;
            }

            // Only checked-out reservations can leave feedback
            if (!currentReservation.getStatus().equals(Reservation.STATUS_CHECKED_OUT)) {
                showError("Feedback can only be provided for completed stays");
                return;
            }

            // Fetch guest information
            currentGuest = guestService.getGuestById(currentReservation.getGuestID());

            // Check if feedback already exists
            boolean feedbackExists = feedbackService.checkFeedbackExists(reservationId);
            if (feedbackExists) {
                showError("Feedback has already been submitted for this reservation");
                return;
            }

            // Display reservation information
            displayReservationInfo();

            // Enable submit button if rating is selected
            updateSubmitButtonState();

            LoggingManager.logSystemInfo("Reservation verified for feedback: #" + reservationId);

        } catch (NumberFormatException e) {
            showError("Please enter a valid reservation ID");
        } catch (DatabaseException e) {
            LoggingManager.logException("Database error verifying reservation", e);
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Error verifying reservation", e);
            showError("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle submit feedback button action
     */
    @FXML
    private void handleSubmitButton(ActionEvent event) {
        if (currentReservation == null || currentGuest == null || selectedRating == 0) {
            return;
        }

        try {
            // Create feedback object
            Feedback feedback = new Feedback();
            feedback.setGuestID(currentGuest.getGuestID());
            feedback.setReservationID(currentReservation.getReservationID());
            feedback.setRating(selectedRating);
            feedback.setComments(txtComments.getText());
            feedback.setSubmissionDateTime(LocalDateTime.now());

            // Save feedback
            feedbackService.saveFeedback(feedback);

            // Update guest's feedback in the database if needed
            if (txtComments.getText() != null && !txtComments.getText().isEmpty()) {
                guestService.updateGuestFeedback(currentGuest.getGuestID(), txtComments.getText());
            }

            // Show thank you message
            showThankYouMessage();

            LoggingManager.logSystemInfo("Feedback submitted for reservation #" +
                    currentReservation.getReservationID() + " with rating " + selectedRating);

        } catch (DatabaseException e) {
            LoggingManager.logException("Database error saving feedback", e);
            showError("Error saving feedback: " + e.getMessage());
        } catch (Exception e) {
            LoggingManager.logException("Error submitting feedback", e);
            showError("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle cancel button action
     */
    @FXML
    private void handleCancelButton(ActionEvent event) {
        try {
            // Return to welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent welcomeRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) btnCancel.getScene().getWindow();

            // Create new scene
            Scene welcomeScene = new Scene(welcomeRoot);
            welcomeScene.getStylesheets().add(getClass().getResource(Constants.CSS_KIOSK).toExternalForm());

            // Set the new scene
            stage.setScene(welcomeScene);

            LoggingManager.logSystemInfo("Returned to welcome screen from feedback");

        } catch (IOException e) {
            LoggingManager.logException("Error returning to welcome screen", e);
            showError("Error returning to welcome screen: " + e.getMessage());
        }
    }

    /**
     * Display reservation information
     */
    private void displayReservationInfo() {
        if (currentReservation == null || currentGuest == null) {
            return;
        }

        lblGuestName.setText(currentGuest.getName());
        lblCheckInDate.setText(currentReservation.getCheckInDate().toString());
        lblCheckOutDate.setText(currentReservation.getCheckOutDate().toString());

        // In a real application, you would fetch the room information
        lblRoomInfo.setText("Room #" + currentReservation.getRoomID());

        // Clear any previous error
        lblError.setVisible(false);
    }

    /**
     * Clear reservation information
     */
    private void clearReservationInfo() {
        lblGuestName.setText("");
        lblCheckInDate.setText("");
        lblCheckOutDate.setText("");
        lblRoomInfo.setText("");
        currentReservation = null;
        currentGuest = null;
        selectedRating = 0;
        resetStars();
        txtComments.clear();

        // Disable submit button
        btnSubmit.setDisable(true);
    }

    /**
     * Show error message
     *
     * @param message Error message to display
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

    /**
     * Show thank you message and reset form
     */
    private void showThankYouMessage() {
        // Hide the form
        txtReservationId.setDisable(true);
        btnVerify.setDisable(true);
        starsContainer.setDisable(true);
        txtComments.setDisable(true);
        btnSubmit.setDisable(true);

        // Show thank you message
        lblThankYou.setVisible(true);

        // Add a delay and then return to welcome screen
        // In a real application, you would use a Timeline or similar
        // For now, we'll just enable the cancel button as "Done"
        btnCancel.setText("Done");
    }
}