package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.dao.AdminDAO;
import ca.senecacollege.apd_final_project.exception.DatabaseException;
import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.util.LoggingManager;

/**
 * Service class for admin-related operations
 */
public class AdminService {

    private final AdminDAO adminDAO;

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

}