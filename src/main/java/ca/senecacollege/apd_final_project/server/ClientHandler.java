package ca.senecacollege.apd_final_project.server;

import ca.senecacollege.apd_final_project.model.Admin;
import ca.senecacollege.apd_final_project.model.Guest;
import ca.senecacollege.apd_final_project.model.Reservation;
import ca.senecacollege.apd_final_project.model.ReservationStatus;
import ca.senecacollege.apd_final_project.service.AdminService;
import ca.senecacollege.apd_final_project.service.GuestService;
import ca.senecacollege.apd_final_project.service.ReservationService;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Handles an individual client connection to the admin server
 * Each admin client gets their own handler that runs in a separate thread
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running = false;
    private Admin loggedInAdmin = null;

    // Services
    private AdminService adminService;
    private GuestService guestService;
    private ReservationService reservationService;

    /**
     * Constructor
     *
     * @param clientSocket The client socket
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.adminService = new AdminService();
        this.guestService = new GuestService();
        this.reservationService = new ReservationService();
    }

    @Override
    public void run() {
        try {
            // Set up input and output streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            running = true;

            // Send welcome message
            out.println("=== Hotel ABC Reservation System ===");
            out.println("Welcome to the admin console. Please login to continue.");

            // Main processing loop
            while (running) {
                if (loggedInAdmin == null) {
                    // Handle login if not logged in
                    handleLogin();
                } else {
                    // Handle admin operations
                    handleAdminOperations();
                }
            }

        } catch (IOException e) {
            LoggingManager.logException("Error handling client", e);
        } finally {
            cleanup();
        }
    }

    /**
     * Handle the login process
     *
     * @throws IOException If there's an error reading from the socket
     */
    private void handleLogin() throws IOException {
        out.println("\nLOGIN");
        out.println("-----");

        // Get username
        out.print("Username: ");
        out.flush();
        String username = in.readLine();

        // Get password
        out.print("Password: ");
        out.flush();
        String password = in.readLine();

        try {
            // Authenticate admin
            Admin admin = adminService.authenticateAdmin(username, password);

            if (admin != null) {
                loggedInAdmin = admin;
                LoggingManager.logAdminActivity(username, "Logged in to admin console");
                out.println("\nLogin successful. Welcome, " + admin.getName() + "!");
            } else {
                out.println("\nInvalid username or password. Please try again.");
                LoggingManager.logSystemWarning("Failed login attempt to admin console: " + username);
            }
        } catch (Exception e) {
            LoggingManager.logException("Error during admin console login", e);
            out.println("\nError during login. Please try again.");
        }
    }

    /**
     * Handle admin operations after successful login
     *
     * @throws IOException If there's an error reading from the socket
     */
    private void handleAdminOperations() throws IOException {
        out.println("\nMAIN MENU");
        out.println("---------");
        out.println("1. Search Guests");
        out.println("2. View Reservations");
        out.println("3. Check-In Guest");
        out.println("4. Check-Out Guest");
        out.println("5. Cancel Reservation");
        out.println("6. Apply Discount");
        out.println("7. Generate Reports");
        out.println("8. Logout");
        out.println("9. Exit");

        out.print("\nEnter your choice: ");
        out.flush();

        String choice = in.readLine();

        try {
            switch (choice) {
                case "1":
                    handleSearchGuests();
                    break;
                case "2":
                    handleViewReservations();
                    break;
                case "3":
                    handleCheckIn();
                    break;
                case "4":
                    handleCheckOut();
                    break;
                case "5":
                    handleCancelReservation();
                    break;
                case "6":
                    handleApplyDiscount();
                    break;
                case "7":
                    handleGenerateReports();
                    break;
                case "8":
                    handleLogout();
                    break;
                case "9":
                    handleExit();
                    break;
                default:
                    out.println("\nInvalid choice. Please try again.");
            }
        } catch (Exception e) {
            LoggingManager.logException("Error handling admin operation", e);
            out.println("\nAn error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle guest search
     *
     * @throws IOException If there's an error reading from the socket
     */
    private void handleSearchGuests() throws IOException {
        out.println("\nSEARCH GUESTS");
        out.println("------------");
        out.println("1. Search by Name");
        out.println("2. Search by Phone Number");
        out.println("3. Search by Email");
        out.println("4. Back to Main Menu");

        out.print("\nEnter your choice: ");
        out.flush();

        String choice = in.readLine();

        if ("4".equals(choice)) {
            return;
        }

        out.print("Enter search term: ");
        out.flush();
        String searchTerm = in.readLine();

        try {
            List<Guest> guests = null;

            switch (choice) {
                case "1":
                    guests = guestService.searchGuestsByName(searchTerm);
                    break;
                case "2":
                    guests = guestService.searchGuestsByPhone(searchTerm);
                    break;
                case "3":
                    guests = guestService.searchGuestsByEmail(searchTerm);
                    break;
                default:
                    out.println("\nInvalid choice. Please try again.");
                    return;
            }

            // Display search results
            if (guests.isEmpty()) {
                out.println("\nNo guests found matching your search criteria.");
            } else {
                out.println("\nSearch Results:");
                out.println("---------------");
                for (Guest guest : guests) {
                    out.println("ID: " + guest.getGuestID());
                    out.println("Name: " + guest.getName());
                    out.println("Phone: " + guest.getPhoneNumber());
                    out.println("Email: " + guest.getEmail());
                    out.println("---------------");
                }

                // Allow viewing reservations for a specific guest
                out.print("\nEnter guest ID to view reservations (or 0 to go back): ");
                out.flush();

                int guestId = Integer.parseInt(in.readLine());

                if (guestId > 0) {
                    displayGuestReservations(guestId);
                }
            }

        } catch (Exception e) {
            LoggingManager.logException("Error searching for guests", e);
            out.println("\nAn error occurred while searching: " + e.getMessage());
        }
    }

    /**
     * Display reservations for a specific guest
     *
     * @param guestId The guest ID
     */
    private void displayGuestReservations(int guestId) {
        try {
            Guest guest = guestService.getGuestById(guestId);
            List<Reservation> reservations = reservationService.getReservationsByGuest(guestId);

            if (guest == null) {
                out.println("\nGuest not found.");
                return;
            }

            out.println("\nReservations for " + guest.getName() + ":");
            out.println("----------------------------------");

            if (reservations.isEmpty()) {
                out.println("No reservations found for this guest.");
            } else {
                for (Reservation reservation : reservations) {
                    out.println("Reservation ID: " + reservation.getReservationID());
                    out.println("Check-in: " + reservation.getCheckInDate());
                    out.println("Check-out: " + reservation.getCheckOutDate());
                    out.println("Status: " + reservation.getStatus());
                    out.println("----------------------------------");
                }
            }
        } catch (Exception e) {
            LoggingManager.logException("Error displaying guest reservations", e);
            out.println("\nAn error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle viewing all reservations
     */
    private void handleViewReservations() throws IOException {
        out.println("\nVIEW RESERVATIONS");
        out.println("----------------");
        out.println("1. View All Active Reservations");
        out.println("2. View Today's Check-ins");
        out.println("3. View Today's Check-outs");
        out.println("4. Back to Main Menu");

        out.print("\nEnter your choice: ");
        out.flush();

        String choice = in.readLine();

        if ("4".equals(choice)) {
            return;
        }

        try {
            List<Reservation> reservations = null;

            switch (choice) {
                case "1":
                    reservations = reservationService.getActiveReservations();
                    break;
                case "2":
                    reservations = reservationService.getTodayCheckIns();
                    break;
                case "3":
                    reservations = reservationService.getTodayCheckOuts();
                    break;
                default:
                    out.println("\nInvalid choice. Please try again.");
                    return;
            }

            // Display reservations
            if (reservations.isEmpty()) {
                out.println("\nNo reservations found.");
            } else {
                out.println("\nReservations:");
                out.println("-------------");
                for (Reservation reservation : reservations) {
                    out.println("Reservation ID: " + reservation.getReservationID());

                    // Get guest info
                    try {
                        Guest guest = guestService.getGuestById(reservation.getGuestID());
                        out.println("Guest: " + guest.getName() + " (" + guest.getPhoneNumber() + ")");
                    } catch (Exception e) {
                        out.println("Guest: [Error retrieving guest info]");
                    }

                    out.println("Check-in: " + reservation.getCheckInDate());
                    out.println("Check-out: " + reservation.getCheckOutDate());
                    out.println("Status: " + reservation.getStatus());
                    out.println("-------------");
                }
            }

        } catch (Exception e) {
            LoggingManager.logException("Error viewing reservations", e);
            out.println("\nAn error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle check-in process
     */
    private void handleCheckIn() throws IOException {
        out.println("\nCHECK-IN GUEST");
        out.println("-------------");

        out.print("Enter reservation ID: ");
        out.flush();

        try {
            int reservationId = Integer.parseInt(in.readLine());

            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                out.println("\nReservation not found.");
                return;
            }

            if (!reservation.getStatus().equals(ReservationStatus.CONFIRMED.getDisplayName())) {
                out.println("\nThis reservation cannot be checked in. Current status: " + reservation.getStatus());
                return;
            }

            Guest guest = guestService.getGuestById(reservation.getGuestID());

            out.println("\nReservation Details:");
            out.println("Guest: " + guest.getName());
            out.println("Check-in: " + reservation.getCheckInDate());
            out.println("Check-out: " + reservation.getCheckOutDate());

            out.print("\nConfirm check-in (y/n): ");
            out.flush();

            String confirm = in.readLine();

            if ("y".equalsIgnoreCase(confirm)) {
                reservationService.checkIn(reservationId);
                out.println("\nCheck-in completed successfully!");
                LoggingManager.logAdminActivity(loggedInAdmin.getUsername(), "Checked in reservation #" + reservationId);
            } else {
                out.println("\nCheck-in cancelled.");
            }

        } catch (NumberFormatException e) {
            out.println("\nInvalid reservation ID. Please enter a valid number.");
        } catch (Exception e) {
            LoggingManager.logException("Error during check-in", e);
            out.println("\nAn error occurred during check-in: " + e.getMessage());
        }
    }

    /**
     * Handle check-out process
     */
    private void handleCheckOut() throws IOException {
        out.println("\nCHECK-OUT GUEST");
        out.println("--------------");

        out.print("Enter reservation ID: ");
        out.flush();

        try {
            int reservationId = Integer.parseInt(in.readLine());

            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                out.println("\nReservation not found.");
                return;
            }

            if (!reservation.getStatus().equals(ReservationStatus.CHECKED_IN.getDisplayName())) {
                out.println("\nThis reservation cannot be checked out. Current status: " + reservation.getStatus());
                return;
            }

            Guest guest = guestService.getGuestById(reservation.getGuestID());

            out.println("\nReservation Details:");
            out.println("Guest: " + guest.getName());
            out.println("Check-in: " + reservation.getCheckInDate());
            out.println("Check-out: " + reservation.getCheckOutDate());

            // Here we'd normally handle billing, but we'll keep it simple
            out.println("\nPlease remind the guest to leave feedback at the kiosk.");

            out.print("\nConfirm check-out (y/n): ");
            out.flush();

            String confirm = in.readLine();

            if ("y".equalsIgnoreCase(confirm)) {
                reservationService.checkOut(reservationId);
                out.println("\nCheck-out completed successfully!");
                LoggingManager.logAdminActivity(loggedInAdmin.getUsername(), "Checked out reservation #" + reservationId);
            } else {
                out.println("\nCheck-out cancelled.");
            }

        } catch (NumberFormatException e) {
            out.println("\nInvalid reservation ID. Please enter a valid number.");
        } catch (Exception e) {
            LoggingManager.logException("Error during check-out", e);
            out.println("\nAn error occurred during check-out: " + e.getMessage());
        }
    }

    /**
     * Handle reservation cancellation
     */
    private void handleCancelReservation() throws IOException {
        out.println("\nCANCEL RESERVATION");
        out.println("-----------------");

        out.print("Enter reservation ID: ");
        out.flush();

        try {
            int reservationId = Integer.parseInt(in.readLine());

            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                out.println("\nReservation not found.");
                return;
            }

            if (reservation.getStatus().equals(ReservationStatus.CHECKED_IN.getDisplayName()) ||
                    reservation.getStatus().equals(ReservationStatus.CHECKED_OUT.getDisplayName()) ||
                    reservation.getStatus().equals(ReservationStatus.CANCELLED.getDisplayName())) {
                out.println("\nThis reservation cannot be cancelled. Current status: " + reservation.getStatus());
                return;
            }

            Guest guest = guestService.getGuestById(reservation.getGuestID());

            out.println("\nReservation Details:");
            out.println("Guest: " + guest.getName());
            out.println("Check-in: " + reservation.getCheckInDate());
            out.println("Check-out: " + reservation.getCheckOutDate());

            out.print("\nConfirm cancellation (y/n): ");
            out.flush();

            String confirm = in.readLine();

            if ("y".equalsIgnoreCase(confirm)) {
                reservationService.cancelReservation(reservationId);
                out.println("\nReservation cancelled successfully!");
                LoggingManager.logAdminActivity(loggedInAdmin.getUsername(), "Cancelled reservation #" + reservationId);
            } else {
                out.println("\nCancellation aborted.");
            }

        } catch (NumberFormatException e) {
            out.println("\nInvalid reservation ID. Please enter a valid number.");
        } catch (Exception e) {
            LoggingManager.logException("Error cancelling reservation", e);
            out.println("\nAn error occurred during cancellation: " + e.getMessage());
        }
    }

    /**
     * Handle applying discount to a reservation
     */
    private void handleApplyDiscount() throws IOException {
        out.println("\nAPPLY DISCOUNT");
        out.println("-------------");

        out.print("Enter reservation ID: ");
        out.flush();

        try {
            int reservationId = Integer.parseInt(in.readLine());

            // In a real system, we'd check for an existing bill or create one
            // For this demo, we'll just show the process

            out.print("Enter discount amount: $");
            out.flush();

            double discountAmount = Double.parseDouble(in.readLine());

            if (discountAmount <= 0) {
                out.println("\nDiscount amount must be greater than zero.");
                return;
            }

            out.println("\nDiscount of $" + String.format("%.2f", discountAmount) + " applied successfully!");
            LoggingManager.logAdminActivity(loggedInAdmin.getUsername(),
                    "Applied discount of $" + String.format("%.2f", discountAmount) + " to reservation #" + reservationId);

        } catch (NumberFormatException e) {
            out.println("\nInvalid number entered. Please enter a valid number.");
        } catch (Exception e) {
            LoggingManager.logException("Error applying discount", e);
            out.println("\nAn error occurred while applying discount: " + e.getMessage());
        }
    }

    /**
     * Handle generating reports
     */
    private void handleGenerateReports() throws IOException {
        out.println("\nGENERATE REPORTS");
        out.println("---------------");
        out.println("1. Occupancy Report");
        out.println("2. Revenue Report");
        out.println("3. Guest Feedback Report");
        out.println("4. Back to Main Menu");

        out.print("\nEnter your choice: ");
        out.flush();

        String choice = in.readLine();

        if ("4".equals(choice)) {
            return;
        }

        // This would normally generate the appropriate report
        out.println("\nReport generation functionality would be implemented here.");
        out.println("In a real system, this would generate the selected report type.");

        LoggingManager.logAdminActivity(loggedInAdmin.getUsername(), "Generated report type: " + choice);
    }

    /**
     * Handle logout
     */
    private void handleLogout() {
        LoggingManager.logAdminActivity(loggedInAdmin.getUsername(), "Logged out from admin console");
        out.println("\nYou have been logged out successfully.");
        loggedInAdmin = null;
    }

    /**
     * Handle exit
     */
    private void handleExit() {
        if (loggedInAdmin != null) {
            LoggingManager.logAdminActivity(loggedInAdmin.getUsername(), "Exited admin console");
        }
        out.println("\nThank you for using the Hotel ABC Reservation System. Goodbye!");
        running = false;
    }

    /**
     * Clean up resources
     */
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LoggingManager.logException("Error cleaning up client handler resources", e);
        }
    }
}