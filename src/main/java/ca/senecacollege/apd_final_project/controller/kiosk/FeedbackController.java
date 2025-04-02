package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.exception.ValidationException;
import ca.senecacollege.apd_final_project.model.Feedback;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.service.*;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.ResourceBundle;

public class FeedbackController extends BaseController {

    public Button btnVerify;
    public Button btnCancel;
    @FXML
    private TextField txtReservationId;

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

    private FeedbackService feedbackService;
    private GuestService guestService;
    private ReservationService reservationService;
    private ValidationService validationService;

    private Reservation currentReservation;
    private Guest currentGuest;
    private int selectedRating = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get services from ServiceLocator
        feedbackService = ServiceLocator.getService(FeedbackService.class);
        guestService = ServiceLocator.getService(GuestService.class);
        reservationService = ServiceLocator.getService(ReservationService.class);
        validationService = ServiceLocator.getService(ValidationService.class);

        setupRatingStars();
        btnSubmit.setDisable(true);
        applyStyles();
        adjustStageSize();

        // Call parent initialize
        super.initialize(url, resourceBundle);
    }

    private void setupRatingStars() {
        // Iterate through star labels and set up event handling
        for (Node node : starsContainer.getChildren()) {
            Label starLabel = (Label) node;

            // Ensure label is clickable
            starLabel.setPickOnBounds(true);

            // Add mouse click event
            starLabel.setOnMouseClicked(this::handleStarClick);

            // Add hover effects
            starLabel.setOnMouseEntered(event -> {
                int starIndex = starsContainer.getChildren().indexOf(starLabel);
                highlightStarsUpTo(starIndex + 1);
            });

            starLabel.setOnMouseExited(event -> updateStarAppearance());
        }
    }

    private void handleStarClick(MouseEvent event) {
        Label clickedStar = (Label) event.getSource();
        int starIndex = starsContainer.getChildren().indexOf(clickedStar);

        // Set selected rating to the index + 1 (1-based indexing)
        selectedRating = starIndex + 1;

        // Update star appearance
        updateStarAppearance();

        // Update submit button state
        updateSubmitButtonState();
    }

    private void highlightStarsUpTo(int count) {
        Platform.runLater(() -> {
            for (int i = 0; i < starsContainer.getChildren().size(); i++) {
                Label star = (Label) starsContainer.getChildren().get(i);

                // Reset all stars
                star.getStyleClass().removeAll("rating-star-filled");

                // Fill stars up to the count
                if (i < count) {
                    if (!star.getStyleClass().contains("rating-star-filled")) {
                        star.getStyleClass().add("rating-star-filled");
                    }
                }
            }
        });
    }

    private void updateStarAppearance() {
        Platform.runLater(() -> {
            // Reset all stars first
            for (Node node : starsContainer.getChildren()) {
                Label star = (Label) node;
                star.getStyleClass().removeAll("rating-star-filled");
            }

            // Fill stars up to selected rating
            for (int i = 0; i < selectedRating; i++) {
                Label star = (Label) starsContainer.getChildren().get(i);
                if (!star.getStyleClass().contains("rating-star-filled")) {
                    star.getStyleClass().add("rating-star-filled");
                }
            }
        });
    }

    private void updateSubmitButtonState() {
        Platform.runLater(() -> {
            boolean canSubmit = currentReservation != null && selectedRating > 0;
            btnSubmit.setDisable(!canSubmit);
        });
    }

    private void resetStars() {
        selectedRating = 0;
        for (Node node : starsContainer.getChildren()) {
            Label star = (Label) node;
            star.getStyleClass().remove("rating-star-filled");
        }
        updateSubmitButtonState();
    }

    /**
     * Handle verify reservation button action
     */
    @FXML
    private void handleVerifyButton() {
        // Clear previous data
        clearFields();
        hideError();

        String reservationIdText = txtReservationId.getText().trim();

        try {
            // Validate ID format
            validationService.validateId(reservationIdText, "Reservation ID");

            int reservationId = Integer.parseInt(reservationIdText);

            // Fetch reservation
            currentReservation = reservationService.getReservationById(reservationId);

            if (currentReservation == null) {
                throw new ValidationException("Reservation not found");
            }

            // Only checked-out reservations can leave feedback
            if (currentReservation.getStatus() != ReservationStatus.CHECKED_OUT) {
                throw new ValidationException("Feedback can only be provided for completed stays");
            }

            // Fetch guest information
            currentGuest = guestService.getGuestById(currentReservation.getGuestID());

            // Check if feedback already exists
            boolean feedbackExists = feedbackService.checkFeedbackExists(reservationId);
            if (feedbackExists) {
                throw new ValidationException("Feedback has already been submitted for this reservation");
            }

            // Display reservation information
            displayReservationInfo();

            // Enable submit button if rating is selected
            updateSubmitButtonState();

            logSystemActivity("Reservation verified for feedback: #" + reservationId);

        } catch (ValidationException e) {
            showError(e.getMessage());
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
    private void handleSubmitButton() {
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

            logSystemActivity("Feedback submitted for reservation #" +
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
    private void handleCancelButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_MAIN));
            Parent mainRoot = loader.load();

            Stage stage = getStage();

            Scene mainScene = new Scene(mainRoot);
            mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());

            stage.setScene(mainScene);

            double stageWidth = ScreenSizeManager.calculateStageWidth(800);
            double stageHeight = ScreenSizeManager.calculateStageHeight(600);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            logSystemActivity("Returned to main screen from feedback");

        } catch (IOException e) {
            LoggingManager.logException("Error returning to main screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error returning to main screen: " + e.getMessage(), e);
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
    }

    /**
     * Clear reservation information
     */
    @Override
    protected void clearFields() {
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

    private void applyStyles() {
        // Style labels to ensure text is white and visible
        lblGuestName.setStyle("-fx-text-fill: white;");
        lblCheckInDate.setStyle("-fx-text-fill: white;");
        lblCheckOutDate.setStyle("-fx-text-fill: white;");
        lblRoomInfo.setStyle("-fx-text-fill: white;");

        // Style text fields and text area
        txtReservationId.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        txtComments.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        // Apply styles to all labels when scene is available
        txtReservationId.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Apply style to all labels
                for (Node node : newScene.getRoot().lookupAll(".label")) {
                    if (node instanceof Label && !node.getStyleClass().contains("label-header")) {
                        node.setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });
    }

    private void adjustStageSize() {
        // Add listener to wait for scene to be available
        txtReservationId.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                try {
                    Stage stage = (Stage) newScene.getWindow();

                    // Get screen dimensions - use 95% of screen height
                    double screenHeight = ScreenSizeManager.getPrimaryScreenBounds().getHeight();
                    double stageHeight = screenHeight * 0.95;

                    // Calculate width based on appropriate aspect ratio
                    double aspectRatio = 1024.0 / 768.0;
                    double stageWidth = stageHeight * aspectRatio;

                    // Limit width to 95% of screen width if necessary
                    double screenWidth = ScreenSizeManager.getPrimaryScreenBounds().getWidth();
                    if (stageWidth > screenWidth * 0.95) {
                        stageWidth = screenWidth * 0.95;
                    }

                    // Get center position
                    double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

                    // Set size and position
                    stage.setWidth(stageWidth);
                    stage.setHeight(stageHeight);
                    stage.setX(centerPos[0]);
                    stage.setY(centerPos[1]);

                    // Make sure it's not maximized
                    stage.setMaximized(false);

                    logSystemActivity("FeedbackScreen size adjusted to " + stageWidth + "x" + stageHeight);
                } catch (Exception e) {
                    LoggingManager.logException("Error adjusting stage size", e);
                }
            }
        });
    }

    /**
     * Show thank you message and reset form
     */
    private void showThankYouMessage() {
        Stage stage = getStage();

        DialogPane dialogPane = new DialogPane();
        dialogPane.setHeaderText("Thank You!");

        // Increase font size and add more padding
        Label contentLabel = new Label("Your feedback has been successfully submitted. We appreciate your input and hope to see you again soon.");
        contentLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-padding: 20px;");
        contentLabel.setWrapText(true);

        dialogPane.setContent(contentLabel);
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

        // Apply custom styling
        dialogPane.setStyle("-fx-background-color: #2a2a2a; -fx-min-width: 400px; -fx-min-height: 200px;");

        Label headerText = new Label("Thank You!");
        headerText.setStyle("-fx-text-fill: #b491c8; -fx-font-weight: bold; -fx-font-size: 24px; -fx-alignment: center;");
        dialogPane.setHeader(headerText);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Feedback Submitted");
        dialog.setDialogPane(dialogPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #7b1fa2; -fx-text-fill: white; -fx-font-size: 16px; -fx-min-width: 100px;");
        }

        dialog.showAndWait();

        // Return to main screen after dialog closes
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_MAIN));
            Parent mainRoot = loader.load();

            Scene mainScene = new Scene(mainRoot);
            mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());

            stage.setScene(mainScene);

            // Adjust stage size
            double stageWidth = ScreenSizeManager.calculateStageWidth(800);
            double stageHeight = ScreenSizeManager.calculateStageHeight(600);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

        } catch (IOException e) {
            LoggingManager.logException("Error returning to main screen", e);
            DialogService.showError(stage, "Navigation Error",
                    "Error returning to main screen: " + e.getMessage(), e);
        }
    }

    /**
     * Get the current stage
     */
    @Override
    protected Stage getStage() {
        if (btnSubmit != null && btnSubmit.getScene() != null) {
            return (Stage) btnSubmit.getScene().getWindow();
        }
        return null;
    }

    /**
     * Log a system activity
     */
    private void logSystemActivity(String activity) {
        LoggingManager.logSystemInfo(activity);
    }
}