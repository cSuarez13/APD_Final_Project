package ca.senecacollege.apd_final_project.util;

import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class ScreenSizeManager {
    // Screen Breakpoints
    public static final int BREAKPOINT_XS = 480;
    public static final int BREAKPOINT_SM = 640;
    public static final int BREAKPOINT_MD = 768;
    public static final int BREAKPOINT_LG = 1024;
    public static final int BREAKPOINT_XL = 1280;
    public static final int BREAKPOINT_2XL = 1536;

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

    /**
     * Get current screen's breakpoint
     * @param width Screen width
     * @return Breakpoint as a string
     */
    public static String getCurrentBreakpoint(double width) {
        if (width < BREAKPOINT_XS) return "xs";
        if (width < BREAKPOINT_SM) return "sm";
        if (width < BREAKPOINT_MD) return "md";
        if (width < BREAKPOINT_LG) return "lg";
        if (width < BREAKPOINT_XL) return "xl";
        return "2xl";
    }

    /**
     * Adjust font size based on screen width
     * @param width Screen width
     * @return Font size as a string
     */
    public static String adjustFontSize(double width) {
        if (width < BREAKPOINT_SM) return "14px";
        if (width < BREAKPOINT_MD) return "15px";
        if (width < BREAKPOINT_LG) return "16px";
        return "17px";
    }
}