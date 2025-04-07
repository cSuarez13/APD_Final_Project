package ca.senecacollege.apd_final_project.controller.kiosk;

import ca.senecacollege.apd_final_project.controller.BaseController;
import ca.senecacollege.apd_final_project.service.DialogService;
import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;
import ca.senecacollege.apd_final_project.util.RulesDialogUtility;
import ca.senecacollege.apd_final_project.util.ScreenSizeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class GuestCountController extends BaseController {

    public BorderPane mainPane;
    @FXML
    private Button btnBack;

    @FXML
    private Button btnNext;

    @FXML
    private Button btnRules;

    @FXML
    private Button btnDecrease;

    @FXML
    private Button btnIncrease;

    @FXML
    private Label lblGuestCount;

    private int guestCount = 1; // Default guest count

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Set initial guest count
        updateGuestCountDisplay();

        // Apply styles to ensure text is visible
        applyStyles();

        // Adjust window size
        adjustStageSize();

        LoggingManager.logSystemInfo("GuestCountController initialized");
    }

    /**
     * Apply styles to ensure text elements are properly colored
     */
    private void applyStyles() {
        lblGuestCount.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        lblGuestCount.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getRoot().lookupAll(".label").forEach(node -> {
                    if (node instanceof Label label &&
                            !label.getStyleClass().contains("label-header") &&
                            !label.equals(lblGuestCount)) {
                        label.setStyle("-fx-text-fill: white;");
                    }
                });
            }
        });
    }

    /**
     * Adjust the stage size to ensure it fits properly on screen
     */
    private void adjustStageSize() {
        lblGuestCount.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() != null) {
                Stage stage = (Stage) newScene.getWindow();

                // Calculate dimensions
                double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
                double stageHeight = ScreenSizeManager.calculateStageHeight(768);

                // Get center position
                double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

                // Set the stage's size and position
                stage.setWidth(stageWidth);
                stage.setHeight(stageHeight);
                stage.setX(centerPos[0]);
                stage.setY(centerPos[1]);

                // Make sure it's not maximized
                stage.setMaximized(false);

                LoggingManager.logSystemInfo("GuestCountScreen size adjusted to " + stageWidth + "x" + stageHeight);
            }
        });
    }

    @FXML
    private void handleDecrease() {
        if (guestCount > 1) {
            guestCount--;
            updateGuestCountDisplay();
        }
    }

    @FXML
    private void handleIncrease() {
        if (guestCount < 20) { // Set a reasonable maximum
            guestCount++;
            updateGuestCountDisplay();
        }
    }

    private void updateGuestCountDisplay() {
        lblGuestCount.setText(String.valueOf(guestCount));

        // Disable decrease button if at minimum
        btnDecrease.setDisable(guestCount <= 1);

        // Disable increase button if at maximum
        btnIncrease.setDisable(guestCount >= 20);
    }

    @FXML
    private void handleNextButton() {
        try {
            // Load the date selection screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_DATE_SELECTION));
            Parent dateSelectionRoot = loader.load();

            // Get the controller and pass the guest count
            DateSelectionController dateSelectionController = loader.getController();
            dateSelectionController.initGuestCount(guestCount);

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene dateSelectionScene = new Scene(dateSelectionRoot);
            dateSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            dateSelectionScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the new scene
            stage.setScene(dateSelectionScene);

            // Size the stage properly
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            LoggingManager.logSystemInfo("Navigated to date selection screen with " + guestCount + " guests");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating to date selection screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error loading date selection screen: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            // Load the welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_WELCOME));
            Parent welcomeRoot = loader.load();

            // Get the current stage
            Stage stage = getStage();

            // Create new scene
            Scene welcomeScene = new Scene(welcomeRoot);
            welcomeScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            welcomeScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_KIOSK)).toExternalForm());

            // Set the scene
            stage.setScene(welcomeScene);

            // Size the stage properly
            double stageWidth = ScreenSizeManager.calculateStageWidth(1024);
            double stageHeight = ScreenSizeManager.calculateStageHeight(768);
            double[] centerPos = ScreenSizeManager.centerStageOnScreen(stageWidth, stageHeight);

            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            stage.setX(centerPos[0]);
            stage.setY(centerPos[1]);

            LoggingManager.logSystemInfo("Returned to welcome screen from guest count screen");

        } catch (IOException e) {
            LoggingManager.logException("Error navigating back to welcome screen", e);
            DialogService.showError(getStage(), "Navigation Error",
                    "Error returning to welcome screen: " + e.getMessage(), e);
        }
    }

    @FXML
    private void handleRulesButton() {
        RulesDialogUtility.showRulesDialog(btnRules);
    }

    @Override
    protected Stage getStage() {
        if (btnNext != null && btnNext.getScene() != null) {
            return (Stage) btnNext.getScene().getWindow();
        }
        return null;
    }
}