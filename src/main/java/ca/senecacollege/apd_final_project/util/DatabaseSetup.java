package ca.senecacollege.apd_final_project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper utility to manually set up required database tables and initial data
 * Run this class directly to ensure the database is properly set up
 */
public class DatabaseSetup {
    private static final Logger LOGGER = Logger.getLogger(DatabaseSetup.class.getName());
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_reservation?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "hotel_admin";
    private static final String DB_PASSWORD = "SecureHotel2025!";

    public static void main(String[] args) {
        LoggingManager.initialize();
        initializeDatabase();
    }

    public static void initializeDatabase() {
        LOGGER.info("Starting database initialization");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Existing code remains the same
            createTables(conn);
            insertRoomTypes(conn);
            insertSampleRooms(conn);
            insertAdminAccounts(conn);

            LOGGER.info("Database initialization completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        LOGGER.info("Creating database tables if they don't exist");

        try (Statement stmt = conn.createStatement()) {
            // Create room_types table
            stmt.execute("CREATE TABLE IF NOT EXISTS room_types (" +
                    "room_type_id TINYINT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "base_price DECIMAL(10, 2) NOT NULL," +
                    "max_occupancy INT NOT NULL)");

            // Create rooms table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "room_type_id TINYINT NOT NULL," +
                    "room_number VARCHAR(10) NOT NULL UNIQUE," +
                    "floor INT NOT NULL," +
                    "price DECIMAL(10, 2) NOT NULL," +
                    "is_available BOOLEAN DEFAULT TRUE)");

            // Create guests table
            stmt.execute("CREATE TABLE IF NOT EXISTS guests (" +
                    "guest_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "phone_number VARCHAR(20) NOT NULL," +
                    "email VARCHAR(100) NOT NULL," +
                    "address VARCHAR(255) NOT NULL," +
                    "feedback TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            // Create reservations table
            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "reservation_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "guest_id INT NOT NULL," +
                    "room_id INT NOT NULL," +
                    "check_in_date DATE NOT NULL," +
                    "check_out_date DATE NOT NULL," +
                    "number_of_guests INT NOT NULL," +
                    "status VARCHAR(20) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            // Create bills table
            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "bill_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "reservation_id INT NOT NULL," +
                    "amount DECIMAL(10, 2) NOT NULL," +
                    "tax DECIMAL(10, 2) NOT NULL," +
                    "discount DECIMAL(10, 2) DEFAULT 0.00," +
                    "total_amount DECIMAL(10, 2) NOT NULL," +
                    "billing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "is_paid BOOLEAN DEFAULT FALSE)");

            // Create feedback table
            stmt.execute("CREATE TABLE IF NOT EXISTS feedback (" +
                    "feedback_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "guest_id INT NOT NULL," +
                    "reservation_id INT NOT NULL," +
                    "rating INT NOT NULL," +
                    "comments TEXT," +
                    "submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Create admins table
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "admin_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "name VARCHAR(100) NOT NULL," +
                    "role VARCHAR(50) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_login TIMESTAMP NULL)");
        }
    }

    private static void insertRoomTypes(Connection conn) throws SQLException {
        LOGGER.info("Inserting room types if they don't exist");

        try (Statement stmt = conn.createStatement()) {
            // Check if room types already exist
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM room_types");
            rs.next();
            if (rs.getInt(1) == 0) {
                // Insert room types
                stmt.execute("INSERT INTO room_types (room_type_id, name, base_price, max_occupancy) VALUES " +
                        "(1, 'SINGLE', 100.00, 2)," +
                        "(2, 'DOUBLE', 180.00, 4)," +
                        "(3, 'DELUXE', 250.00, 2)," +
                        "(4, 'PENT_HOUSE', 400.00, 2)");
                LOGGER.info("Room types inserted successfully");
            } else {
                LOGGER.info("Room types already exist, skipping insertion");
            }
        }
    }

    private static void insertSampleRooms(Connection conn) throws SQLException {
        LOGGER.info("Inserting sample rooms if they don't exist");

        try (Statement stmt = conn.createStatement()) {
            // Check if rooms already exist
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            rs.next();
            if (rs.getInt(1) == 0) {
                // Insert sample rooms for each type
                stmt.execute("INSERT INTO rooms (room_type_id, room_number, floor, price, is_available) VALUES " +
                        "(1, '101', 1, 100.00, TRUE)," +
                        "(1, '102', 1, 100.00, TRUE)," +
                        "(1, '103', 1, 100.00, TRUE)," +
                        "(1, '104', 1, 100.00, TRUE)," +
                        "(2, '201', 2, 180.00, TRUE)," +
                        "(2, '202', 2, 180.00, TRUE)," +
                        "(2, '203', 2, 180.00, TRUE)," +
                        "(3, '301', 3, 250.00, TRUE)," +
                        "(3, '302', 3, 250.00, TRUE)," +
                        "(4, '401', 4, 400.00, TRUE)");
                LOGGER.info("Sample rooms inserted successfully");
            } else {
                LOGGER.info("Rooms already exist, skipping insertion");
            }
        }
    }

    private static void insertAdminAccounts(Connection conn) throws SQLException {
        LOGGER.info("Inserting admin accounts if they don't exist");

        try (Statement stmt = conn.createStatement()) {
            // Check if admin accounts already exist
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM admins");
            rs.next();
            if (rs.getInt(1) == 0) {
                // Insert default admin accounts
                stmt.execute("INSERT INTO admins (username, password, name, role) VALUES " +
                        "('admin', 'admin123', 'System Administrator', 'ADMIN')," +
                        "('manager', 'manager123', 'Hotel Manager', 'MANAGER')");
                LOGGER.info("Admin accounts inserted successfully");
            } else {
                LOGGER.info("Admin accounts already exist, skipping insertion");
            }
        }
    }
}