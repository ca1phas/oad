import model.User;

public class App {
    public static void main(String[] args) throws Exception {
        // Initialize the system
        System.out.println("Welcome to the Library E-Book Lending & Reservation System!");

        User currentUser = null;
        boolean running = true;
        while (running) {
            // Authenticate user

            // Home menu
            System.out.println("Please select an option:");
            System.out.println("1. My Account");
            System.out.println("2. My Reservations");
            System.out.println("3. Books");

            // Admin
            System.out.println("4. Reservations");
            System.out.println("5. Users");
            System.out.println("4/6. Logout");

            currentUser = null;
            continue;
        }

        System.out.println("Thank you for using the Library E-Book Lending & Reservation System. Goodbye!");
    }
}
