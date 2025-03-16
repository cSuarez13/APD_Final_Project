package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.AdminDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.util.List;

/**
 * Service class for admin-related operations
 */
public class AdminService {

    private AdminDAO adminDAO;

    /**
     * Constructor
     */
    public AdminService() {
        this.adminDAO = new AdminDAO();
    }

    /**
     * Authenticate an admin
     *
     * @param username The username
     * @param password The password
     * @return The authenticated admin or null if authentication failed
     * @throws DatabaseException If there's an error during authentication
     */
    public Admin authenticateAdmin(String username, String password) throws DatabaseException {
        try {
            Admin admin = adminDAO.authenticate(username, password);
            if (admin != null) {
                LoggingManager.logAdminActivity(username, "Successfully authenticated");
            } else {
                LoggingManager.logSystemWarning("Failed authentication attempt for username: " + username);
            }
            return admin;
        } catch (Exception e) {
            LoggingManager.logException("Error during admin authentication", e);
            throw new DatabaseException("Authentication error: " + e.getMessage(), e);
        }
    }

    /**
     * Get an admin by ID
     *
     * @param adminId The admin ID
     * @return The admin
     * @throws DatabaseException If there's an error retrieving the admin
     */
    public Admin getAdminById(int adminId) throws DatabaseException {
        try {
            return adminDAO.findById(adminId);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving admin with ID: " + adminId, e);
            throw new DatabaseException("Error retrieving admin: " + e.getMessage(), e);
        }
    }

    /**
     * Get an admin by username
     *
     * @param username The username
     * @return The admin
     * @throws DatabaseException If there's an error retrieving the admin
     */
    public Admin getAdminByUsername(String username) throws DatabaseException {
        try {
            return adminDAO.findByUsername(username);
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving admin with username: " + username, e);
            throw new DatabaseException("Error retrieving admin: " + e.getMessage(), e);
        }
    }

    /**
     * Get all admins
     *
     * @return List of all admins
     * @throws DatabaseException If there's an error retrieving admins
     */
    public List<Admin> getAllAdmins() throws DatabaseException {
        try {
            return adminDAO.findAll();
        } catch (Exception e) {
            LoggingManager.logException("Error retrieving all admins", e);
            throw new DatabaseException("Error retrieving admins: " + e.getMessage(), e);
        }
    }

    /**
     * Save a new admin
     *
     * @param admin The admin to save
     * @return The generated admin ID
     * @throws DatabaseException If there's an error saving the admin
     */
    public int saveAdmin(Admin admin) throws DatabaseException {
        try {
            // Check if username already exists
            Admin existingAdmin = adminDAO.findByUsername(admin.getUsername());
            if (existingAdmin != null) {
                throw new DatabaseException("Username already exists");
            }

            int adminId = adminDAO.save(admin);
            LoggingManager.logSystemInfo("Admin saved: " + admin.getUsername() + " (ID: " + adminId + ")");
            return adminId;
        } catch (Exception e) {
            LoggingManager.logException("Error saving admin", e);
            throw new DatabaseException("Error saving admin: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing admin
     *
     * @param admin The admin to update
     * @throws DatabaseException If there's an error updating the admin
     */
    public void updateAdmin(Admin admin) throws DatabaseException {
        try {
            // Check if username exists and belongs to another admin
            Admin existingAdmin = adminDAO.findByUsername(admin.getUsername());
            if (existingAdmin != null && existingAdmin.getAdminID() != admin.getAdminID()) {
                throw new DatabaseException("Username already exists");
            }

            adminDAO.update(admin);
            LoggingManager.logSystemInfo("Admin updated: " + admin.getUsername() + " (ID: " + admin.getAdminID() + ")");
        } catch (Exception e) {
            LoggingManager.logException("Error updating admin", e);
            throw new DatabaseException("Error updating admin: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an admin
     *
     * @param adminId The admin ID to delete
     * @throws DatabaseException If there's an error deleting the admin
     */
    public void deleteAdmin(int adminId) throws DatabaseException {
        try {
            Admin admin = adminDAO.findById(adminId);
            if (admin != null) {
                adminDAO.delete(adminId);
                LoggingManager.logSystemInfo("Admin deleted: " + admin.getUsername() + " (ID: " + adminId + ")");
            } else {
                throw new DatabaseException("Admin not found");
            }
        } catch (Exception e) {
            LoggingManager.logException("Error deleting admin", e);
            throw new DatabaseException("Error deleting admin: " + e.getMessage(), e);
        }
    }

    /**
     * Update the last login time for an admin
     *
     * @param adminId The admin ID
     * @throws DatabaseException If there's an error updating the last login time
     */
    public void updateLastLogin(int adminId) throws DatabaseException {
        try {
            adminDAO.updateLastLogin(adminId);
        } catch (Exception e) {
            LoggingManager.logException("Error updating last login time for admin ID: " + adminId, e);
            throw new DatabaseException("Error updating last login time: " + e.getMessage(), e);
        }
    }

    /**
     * Change an admin's password
     *
     * @param adminId The admin ID
     * @param oldPassword The old password
     * @param newPassword The new password
     * @return true if password was successfully changed, false otherwise
     * @throws DatabaseException If there's an error changing the password
     */
    public boolean changePassword(int adminId, String oldPassword, String newPassword) throws DatabaseException {
        try {
            // Get the admin
            Admin admin = adminDAO.findById(adminId);
            if (admin == null) {
                throw new DatabaseException("Admin not found");
            }

            // Verify old password
            if (!admin.getPassword().equals(oldPassword)) {
                return false;
            }

            // Update password
            admin.setPassword(newPassword);
            adminDAO.update(admin);

            LoggingManager.logSystemInfo("Password changed for admin: " + admin.getUsername());
            return true;
        } catch (Exception e) {
            LoggingManager.logException("Error changing password for admin ID: " + adminId, e);
            throw new DatabaseException("Error changing password: " + e.getMessage(), e);
        }
    }
}