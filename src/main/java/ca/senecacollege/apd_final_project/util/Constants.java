package ca.senecacollege.apd_final_project.util;

public class Constants {
    // General application constants
    public static final String APP_NAME = "Hotel ABC Reservation System";
    public static final String APP_VERSION = "1.0.0";

    // FXML files
    public static final String FXML_MAIN = "/ca.senecacollege.apd_final_project/Main.fxml";
    public static final String FXML_LOGIN = "/ca.senecacollege.apd_final_project/LoginScreen.fxml";
    public static final String FXML_ADMIN_DASHBOARD = "/ca.senecacollege.apd_final_project/AdminDashboard.fxml";
    public static final String FXML_SEARCH_GUEST = "/ca.senecacollege.apd_final_project/SearchGuestScreen.fxml";
    public static final String FXML_CHECKOUT = "/ca.senecacollege.apd_final_project/CheckoutScreen.fxml";
    public static final String FXML_REPORT = "/ca.senecacollege.apd_final_project/ReportScreen.fxml";
    public static final String FXML_WELCOME = "/ca.senecacollege.apd_final_project/WelcomeScreen.fxml";
    public static final String FXML_BOOKING = "/ca.senecacollege.apd_final_project/BookingScreen.fxml";
    public static final String FXML_GUEST_DETAILS = "/ca.senecacollege.apd_final_project/GuestDetailsScreen.fxml";
    public static final String FXML_CONFIRMATION = "/ca.senecacollege.apd_final_project/ConfirmationScreen.fxml";
    public static final String FXML_FEEDBACK = "/ca.senecacollege.apd_final_project/FeedbackScreen.fxml";
    public static final String FXML_CHECKIN = "/ca.senecacollege.apd_final_project/CheckInScreen.fxml";

    // CSS files
    public static final String CSS_MAIN = "/ca.senecacollege.apd_final_project/application.css";
    public static final String CSS_ADMIN = "/ca.senecacollege.apd_final_project/admin.css";
    public static final String CSS_KIOSK = "/ca.senecacollege.apd_final_project/kiosk.css";

    // Server configuration
    public static final int SERVER_PORT = 5555;
    public static final int MAX_CLIENTS = 10;

    // Reservation status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_CHECKED_IN = "Checked In";
    public static final String STATUS_CHECKED_OUT = "Checked Out";
    public static final String STATUS_CANCELLED = "Cancelled";

    // Room related constants
    public static final int MAX_GUESTS_SINGLE_ROOM = 2;
    public static final int MAX_GUESTS_DOUBLE_ROOM = 4;
    public static final int MAX_GUESTS_DELUXE_ROOM = 2;
    public static final int MAX_GUESTS_PENT_HOUSE = 2;

    // Tax rate
    public static final double TAX_RATE = 0.13; // 13% tax

    // Welcome message for kiosk
    public static final String WELCOME_MESSAGE = "Welcome to Hotel ABC! We're delighted to have you here. " +
            "Our self-service kiosk makes booking quick and easy. " +
            "Follow the simple steps to secure your perfect room.";

    // Rules and regulations message
    public static final String RULES_REGULATIONS = "• Check-in time: 3:00 PM\n" +
            "• Check-out time: 11:00 AM\n" +
            "• Single rooms accommodate maximum 2 adults\n" +
            "• Double rooms accommodate maximum 4 adults\n" +
            "• Deluxe and Pent House rooms accommodate maximum 2 adults\n" +
            "• Quiet hours: 10:00 PM - 7:00 AM\n" +
            "• No smoking in rooms\n" +
            "• Pets allowed only in designated rooms (additional fee applies)\n" +
            "• Damage to hotel property will be charged\n" +
            "• The hotel is not responsible for valuables left in rooms";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}