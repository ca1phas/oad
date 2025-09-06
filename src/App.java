
// Importing model, services, views, and controllers
import model.User;
import service.BookService;
import service.ReservationService;
import view.AuthView;
import view.BookView;
import view.ReservationsView;
import controller.AuthController;
import controller.UserController;
import controller.BookController;
import controller.ReservationController;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        // Starting point of the application
        System.out.println("Welcome to the Library E-Book Lending & Reservation System!");

        // Initialize scanner for user input
        Scanner sc = new Scanner(System.in);

        // Initialize views
        AuthView authView = new AuthView(sc);
        BookView bookView = new BookView(sc);
        ReservationsView reservationsView = new ReservationsView();

        // Initialize controllers
        AuthController authController = new AuthController(sc);
        UserController userController = new UserController(sc);
        BookController bookController = new BookController(sc);

        // Initialize services
        BookService bookService = new BookService();
        ReservationService reservationService = new ReservationService();

        // Reservation controller needs both services and views
        ReservationController reservationController = new ReservationController(
                reservationService, bookService, reservationsView, bookView);

        // Main loop of the application
        boolean running = true;
        while (running) {
            // Display login/register/exit menu
            authView.displayWelcome();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": // Login option
                    Optional<User> optionalUser = authController.handleLogin();
                    if (optionalUser.isEmpty()) {
                        authView.displayLoginFailed();
                    } else {
                        // Start session if login successful
                        User currentUser = optionalUser.get();
                        startUserSession(currentUser, sc, userController, bookController, reservationController);
                    }
                    break;

                case "2": // Register option
                    Optional<User> registeredUser = authController.handleSignup();
                    if (registeredUser.isPresent()) {
                        // Start session immediately after signup
                        User currentUser = registeredUser.get();
                        startUserSession(currentUser, sc, userController, bookController, reservationController);
                    }
                    break;

                case "3": // Exit option
                    running = false;
                    break;

                default: // Invalid input
                    System.out.println("Invalid option. Please enter 1, 2, or 3.");
            }
        }

        // End of program
        System.out.println("Thank you for using the Library E-Book Lending & Reservation System. Goodbye!");
        sc.close();
    }

    // Handles user interactions once logged in
    private static void startUserSession(User currentUser, Scanner sc,
            UserController userController, BookController bookController,
            ReservationController reservationController) {

        boolean loggedIn = true;

        // Ensure console output uses UTF-8 encoding
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("UTF-8 encoding not supported. Using default encoding.");
        }

        // Session loop while user is logged in
        while (loggedIn) {
            // Display current user info and menu
            System.out.println(
                    "\n[Logged in as: " + currentUser.getUsername() + " | Role: " + currentUser.getRole() + "]");
            System.out.println("\nPlease select an option:");
            System.out.println("1. My Account");
            System.out.println("2. My Reservations");
            System.out.println("3. Books");

            // Admins see more options
            if (currentUser.isAdmin()) {
                System.out.println("4. Reservations");
                System.out.println("5. Users");
                System.out.println("6. Logout");
            } else {
                System.out.println("4. Logout");
            }

            System.out.print("Enter your choice: ");
            String userChoice = sc.nextLine().trim();

            switch (userChoice) {
                case "1": // Account management
                    userController.handleAccountMenu(currentUser);
                    break;
                case "2": // User's reservations
                    reservationController.handleReservationsMenu(currentUser);
                    break;
                case "3": // Browse/manage books
                    bookController.handleMenu(currentUser.isAdmin(), false, currentUser);
                    break;

                case "4": // Admin: Reservations / User: Logout
                    if (currentUser.isAdmin()) {
                        reservationController.handleReservationsMenu(currentUser);
                    } else {
                        loggedIn = false;
                    }
                    break;

                case "5": // Admin: Manage users
                    if (currentUser.isAdmin()) {
                        userController.handleUserManagementMenu(currentUser);
                    } else {
                        System.out.println("Invalid option.");
                    }
                    break;

                case "6": // Admin: Logout
                    if (currentUser.isAdmin()) {
                        loggedIn = false;
                    } else {
                        System.out.println("Invalid option.");
                    }
                    break;

                default: // Invalid choice
                    System.out.println("Invalid option. Please try again.");
            }
        }
        // Print logout message after session ends
        System.out.println("You have been logged out.\n");
        System.exit(0);
    }
}
