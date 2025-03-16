package ca.senecacollege.apd_final_project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_reservation";
    private static final String DB_USER = "hotel_user";
    private static final String DB_PASSWORD = "hotel_password";

    private static Connection connection;

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Create connection
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.log(Level.INFO, "Database connection established successfully");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
                throw new RuntimeException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
                throw new RuntimeException("Failed to establish database connection", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                LOGGER.log(Level.INFO, "Database connection closed successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to close database connection", e);
            }
        }
    }
}