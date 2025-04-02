package ca.senecacollege.apd_final_project.dao;

import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.util.DatabaseConnection;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.sql.*;
import java.time.LocalDateTime;

public class AdminDAO {

    /**
     * Save a new admin
     *
     * @param admin The admin to save
     * @return The generated admin ID
     * @throws DatabaseException If there's an error saving the admin
     */
    public int save(Admin admin) throws DatabaseException {
        String sql = "INSERT INTO admins (username, password, name, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getName());
            stmt.setString(4, admin.getRole());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Creating admin failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int adminId = generatedKeys.getInt(1);
                    admin.setAdminID(adminId);
                    return adminId;
                } else {
                    throw new DatabaseException("Creating admin failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while saving admin", e);
            throw new DatabaseException("Error saving admin: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing admin
     *
     * @param admin The admin to update
     * @throws DatabaseException If there's an error updating the admin
     */
    public void update(Admin admin) throws DatabaseException {
        String sql = "UPDATE admins SET username = ?, password = ?, name = ?, role = ? WHERE admin_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getName());
            stmt.setString(4, admin.getRole());
            stmt.setInt(5, admin.getAdminID());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating admin failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating admin", e);
            throw new DatabaseException("Error updating admin: " + e.getMessage(), e);
        }
    }

    /**
     * Update the last login time for an admin
     *
     * @param adminId The admin ID
     * @throws DatabaseException If there's an error updating the login time
     */
    public void updateLastLogin(int adminId) throws DatabaseException {
        String sql = "UPDATE admins SET last_login = ? WHERE admin_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, adminId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Updating admin last login failed, no rows affected.");
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error while updating admin last login", e);
            throw new DatabaseException("Error updating admin last login: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticate an admin
     *
     * @param username The username
     * @param password The password
     * @return The authenticated admin, or null if authentication fails
     * @throws DatabaseException If there's an error during authentication
     */
    public Admin authenticate(String username, String password) throws DatabaseException {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Admin admin = mapResultSetToAdmin(rs);
                    // Update last login time
                    updateLastLogin(admin.getAdminID());
                    return admin;
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            LoggingManager.logException("Database error during admin authentication", e);
            throw new DatabaseException("Error during authentication: " + e.getMessage(), e);
        }
    }

    /**
     * Map a result set row to an Admin object
     *
     * @param rs The result set
     * @return An Admin object
     * @throws SQLException If there's an error accessing the result set
     */
    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminID(rs.getInt("admin_id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setName(rs.getString("name"));
        admin.setRole(rs.getString("role"));
        return admin;
    }
}