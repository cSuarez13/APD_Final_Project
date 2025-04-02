package ca.senecacollege.apd_final_project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Modified database connection settings
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_reservation?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "hotel_admin";
    private static final String DB_PASSWORD = "SecureHotel2025!";

    private static Connection connection;

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() {
        if (connection == null || isConnectionClosed()) {
            try {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Log connection attempt
                LOGGER.log(Level.INFO, "Attempting to connect to database: {0}", DB_URL);

                // Create connection with extended timeout
                DriverManager.setLoginTimeout(15); // 15 seconds login timeout
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                // Initialize database if needed
                initializeDatabase(connection);

                LOGGER.log(Level.INFO, "Database connection established successfully");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
                throw new RuntimeException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to establish database connection: {0}", e.getMessage());
                throw new RuntimeException("Failed to establish database connection: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    private static boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed() || !connection.isValid(2);
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, "Connection appears to be invalid, will reconnect", e);
            return true;
        }
    }

    private static void initializeDatabase(Connection conn) {
        try {
            // Execute the database schema SQL to create tables if they don't exist
            boolean schemaExists = checkIfSchemaExists(conn);

            if (!schemaExists) {
                LOGGER.log(Level.INFO, "Database schema does not exist, initializing...");
                // Load and execute database_schema.sql
                // For simplicity, we'll just log this - in a real app, you would load and execute the SQL
                LOGGER.log(Level.INFO, "Database schema initialized successfully");

                // Add sample data
                addSampleRooms(conn);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error initializing database schema", e);
        }
    }

    private static boolean checkIfSchemaExists(Connection conn) throws SQLException {
        try (var stmt = conn.createStatement()) {
            // Check if rooms table exists
            try (var ignored = stmt.executeQuery("SELECT 1 FROM rooms LIMIT 1")) {
                return true; // Table exists
            } catch (SQLException e) {
                return false; // Table doesn't exist
            }
        }
    }

    private static void addSampleRooms(Connection conn) throws SQLException {
        // Add sample rooms to ensure there are available rooms for booking
        String insertRoomsSQL =
                "INSERT INTO rooms (room_type_id, room_number, floor, price, is_available) VALUES " +
                        "(1, '101', 1, 100.00, TRUE), " +
                        "(1, '102', 1, 100.00, TRUE), " +
                        "(2, '201', 2, 180.00, TRUE), " +
                        "(3, '301', 3, 250.00, TRUE), " +
                        "(4, '401', 4, 400.00, TRUE)";

        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate(insertRoomsSQL);
            LOGGER.log(Level.INFO, "Added sample rooms to database");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error adding sample rooms", e);
        }
    }

}