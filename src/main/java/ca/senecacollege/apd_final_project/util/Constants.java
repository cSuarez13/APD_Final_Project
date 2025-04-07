package ca.senecacollege.apd_final_project.util;

public class Constants {
    // General application constants
    public static final String APP_NAME = "Hotel ABC Reservation System";

    // FXML files
    public static final String FXML_MAIN = "/ca.senecacollege.apd_final_project/Main.fxml";
    public static final String FXML_LOGIN = "/ca.senecacollege.apd_final_project/LoginScreen.fxml";
    public static final String FXML_ADMIN_DASHBOARD = "/ca.senecacollege.apd_final_project/AdminDashboard.fxml";
    public static final String FXML_SEARCH_GUEST = "/ca.senecacollege.apd_final_project/SearchGuestScreen.fxml";
    public static final String FXML_CHECKOUT = "/ca.senecacollege.apd_final_project/CheckoutScreen.fxml";
    public static final String FXML_REPORT = "/ca.senecacollege.apd_final_project/ReportScreen.fxml";
    public static final String FXML_WELCOME = "/ca.senecacollege.apd_final_project/WelcomeScreen.fxml";
    // New booking flow screens
    public static final String FXML_GUEST_COUNT = "/ca.senecacollege.apd_final_project/GuestCountScreen.fxml";
    public static final String FXML_DATE_SELECTION = "/ca.senecacollege.apd_final_project/DateSelectionScreen.fxml";
    public static final String FXML_ROOM_SELECTION = "/ca.senecacollege.apd_final_project/RoomSelectionScreen.fxml";
    // Original screens still used in the flow
    public static final String FXML_BOOKING = "/ca.senecacollege.apd_final_project/BookingScreen.fxml";
    public static final String FXML_GUEST_DETAILS = "/ca.senecacollege.apd_final_project/GuestDetailsScreen.fxml";
    public static final String FXML_CONFIRMATION = "/ca.senecacollege.apd_final_project/ConfirmationScreen.fxml";
    public static final String FXML_FEEDBACK = "/ca.senecacollege.apd_final_project/FeedbackScreen.fxml";
    public static final String FXML_CHECKIN = "/ca.senecacollege.apd_final_project/CheckInScreen.fxml";
    public static final String FXML_DASHBOARD_CONTENT = "/ca.senecacollege.apd_final_project/DashboardContent.fxml";
    public static final String FXML_RESERVATIONS = "/ca.senecacollege.apd_final_project/ReservationScreen.fxml";

    // CSS files
    public static final String CSS_MAIN = "/ca.senecacollege.apd_final_project/application.css";
    public static final String CSS_ADMIN = "/ca.senecacollege.apd_final_project/admin.css";
    public static final String CSS_KIOSK = "/ca.senecacollege.apd_final_project/kiosk.css";

    // Server configuration
    public static final int SERVER_PORT = 5555;
    public static final int MAX_CLIENTS = 10;

    // Room related constants
    public static final int MAX_GUESTS_SINGLE_ROOM = 2;
    public static final int MAX_GUESTS_DOUBLE_ROOM = 4;

    // Tax rate
    public static final double TAX_RATE = 0.13; // 13% tax

    // Welcome message for kiosk
    public static final String WELCOME_MESSAGE = "Welcome to Hotel ABC! We're delighted to have you here. " +
            "Our self-service kiosk makes booking quick and easy. " +
            "Follow the simple steps to secure your perfect room.";

    // Rules and regulations message
    public static final String RULES_REGULATIONS = """
            • Check-in time: 3:00 PM
            • Check-out time: 11:00 AM
            • Single rooms accommodate maximum 2 adults
            • Double rooms accommodate maximum 4 adults
            • Deluxe and Pent House rooms accommodate maximum 2 adults
            • Quiet hours: 10:00 PM - 7:00 AM
            • No smoking in rooms
            • Pets allowed only in designated rooms (additional fee applies)
            • Damage to hotel property will be charged
            • The hotel is not responsible for valuables left in rooms""";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}