package ca.senecacollege.apd_final_project.util;

import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class ScreenSizeManager {

    /**
     * Get the primary screen's visual bounds
     * @return Rectangle2D representing the screen's usable area
     */
    public static Rectangle2D getPrimaryScreenBounds() {
        return Screen.getPrimary().getVisualBounds();
    }

    /**
     * Calculate optimal stage width based on screen size
     * @param maxWidth Maximum desired width
     * @return Calculated width
     */
    public static double calculateStageWidth(double maxWidth) {
        Rectangle2D screenBounds = getPrimaryScreenBounds();
        return Math.min(maxWidth, screenBounds.getWidth() * 0.8);
    }

    /**
     * Calculate optimal stage height based on screen size
     * @param maxHeight Maximum desired height
     * @return Calculated height
     */
    public static double calculateStageHeight(double maxHeight) {
        Rectangle2D screenBounds = getPrimaryScreenBounds();
        return Math.min(maxHeight, screenBounds.getHeight() * 0.95);
    }

    /**
     * Center a stage on the screen
     * @param stageWidth Width of the stage
     * @param stageHeight Height of the stage
     * @return Double array with [x, y] coordinates
     */
    public static double[] centerStageOnScreen(double stageWidth, double stageHeight) {
        Rectangle2D screenBounds = getPrimaryScreenBounds();
        double x = (screenBounds.getWidth() - stageWidth) / 2;
        double y = (screenBounds.getHeight() - stageHeight) / 2;
        return new double[]{x, y};
    }

}