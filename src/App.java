import model.User;
import controller.AuthController;
import controller.UserController;

import java.util.Optional;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        // Initialize the system
        System.out.println("Welcome to the Library E-Book Lending & Reservation System!");

        Scanner sc = new Scanner(System.in);
        AuthController authController = new AuthController(sc);
        UserController userController = new UserController(sc);

        boolean running = true;
        while (running) {
            // Authenticate user
            Optional<User> optionalUser = authController.handleLogin();
            if (optionalUser.isEmpty()) {
                System.out.println("Login failed. Try again.\n");
                continue;
            }

            User currentUser = optionalUser.get();
            boolean loggedIn = true;

            while (loggedIn) {
                System.out.println("\nPlease select an option:");
                System.out.println("1. My Account");
                System.out.println("2. My Reservations");
                System.out.println("3. Books");

                if (currentUser.isAdmin()) {
                    System.out.println("4. Reservations");
                    System.out.println("5. Users");
                    System.out.println("6. Logout");
                } else {
                    System.out.println("4. Logout");
                }

                System.out.print("Enter your choice: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1":
                        userController.handleAccountMenu(currentUser);
                        break;

                    case "2":
                        System.out.println("[My Reservations] feature not yet implemented.");
                        break;

                    case "3":
                        System.out.println("[Books] feature not yet implemented.");
                        break;

                    case "4":
                        if (currentUser.isAdmin()) {
                            System.out.println("[Reservations] feature not yet implemented.");
                        } else {
                            loggedIn = false; // Logout for member
                        }
                        break;

                    case "5":
                        if (currentUser.isAdmin()) {
                            userController.handleUserManagementMenu(currentUser);
                        } else {
                            System.out.println("Invalid option.");
                        }
                        break;

                    case "6":
                        if (currentUser.isAdmin()) {
                            loggedIn = false; // Logout for admin
                        } else {
                            System.out.println("Invalid option.");
                        }
                        break;

                    default:
                        System.out.println("Invalid option. Please try again.");
                        break;
                }
            }

            System.out.println("You have been logged out.\n");
        }

        System.out.println("Thank you for using the Library E-Book Lending & Reservation System. Goodbye!");
        sc.close();
    }
}