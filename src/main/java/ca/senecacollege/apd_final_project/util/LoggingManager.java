package ca.senecacollege.apd_final_project.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggingManager {
    private static final Logger SYSTEM_LOGGER = Logger.getLogger("SystemLogger");
    private static final Logger ADMIN_LOGGER = Logger.getLogger("AdminLogger");
    private static final Logger EXCEPTION_LOGGER = Logger.getLogger("ExceptionLogger");

    private static boolean initialized = false;

    private LoggingManager() {
        // Private constructor to prevent instantiation
    }

    public static void initialize() {
        if (!initialized) {
            try {
                // Configure system logs
                FileHandler systemHandler = new FileHandler("logs/system_logs.%g.log", 1024 * 1024, 10, true);
                SYSTEM_LOGGER.addHandler(systemHandler);
                SimpleFormatter systemFormatter = new SimpleFormatter();
                systemHandler.setFormatter(systemFormatter);

                // Configure admin activity logs
                FileHandler adminHandler = new FileHandler("logs/admin_logs.%g.log", 1024 * 1024, 10, true);
                ADMIN_LOGGER.addHandler(adminHandler);
                SimpleFormatter adminFormatter = new SimpleFormatter();
                adminHandler.setFormatter(adminFormatter);

                // Configure exception logs
                FileHandler exceptionHandler = new FileHandler("logs/exception_logs.%g.log", 1024 * 1024, 10, true);
                EXCEPTION_LOGGER.addHandler(exceptionHandler);
                SimpleFormatter exceptionFormatter = new SimpleFormatter();
                exceptionHandler.setFormatter(exceptionFormatter);

                initialized = true;
                logSystemInfo("Logging initialized successfully");
            } catch (IOException e) {
                System.err.println("Failed to initialize logger: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void logSystemInfo(String message) {
        SYSTEM_LOGGER.log(Level.INFO, message);
    }

    public static void logSystemWarning(String message) {
        SYSTEM_LOGGER.log(Level.WARNING, message);
    }

    public static void logSystemError(String message, Throwable throwable) {
        SYSTEM_LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void logAdminActivity(String adminUsername, String activity) {
        ADMIN_LOGGER.log(Level.INFO, "Admin[" + adminUsername + "]: " + activity);
    }

    public static void logException(String message, Throwable throwable) {
        EXCEPTION_LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void logException(String message) {
        EXCEPTION_LOGGER.log(Level.SEVERE, message);
    }

    public static Logger getSystemLogger() {
        return SYSTEM_LOGGER;
    }

    public static Logger getAdminLogger() {
        return ADMIN_LOGGER;
    }

    public static Logger getExceptionLogger() {
        return EXCEPTION_LOGGER;
    }
}