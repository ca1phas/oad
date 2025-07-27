package view;

import model.User;
import model.enums.UserRole;

import java.util.List;
import java.util.Scanner;

public class UserView {
    private final Scanner sc;

    public UserView(Scanner sc) {
        this.sc = sc;
    }

    public void displayAccountMenu() {
        System.out.println("\n=== My Account ===");
        System.out.println("1. View My Details");
        System.out.println("2. Update Username");
        System.out.println("3. Update Password");
        System.out.println("4. Update Role (Admin only)");
        System.out.println("5. Delete My Account");
        System.out.println("6. Back");
        System.out.print("Enter your choice: ");
    }

    public void displayUserManagementMenu() {
        System.out.println("\n=== User Management (Admin) ===");
        System.out.println("1. View Users by Page");
        System.out.println("2. Filter & Sort Users");
        System.out.println("3. View User Details");
        System.out.println("4. Register New User");
        System.out.println("5. Back");
        System.out.print("Enter your choice: ");
    }

    public void viewUserDetails(User user) {
        System.out.println("\n=== User Details ===");
        System.out.println("My Username: " + user.getUsername());
        System.out.println("My Role: " + (user.getRole() == UserRole.ADMIN ? "Admin" : "Member"));
    }

    public void displayUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println("\n=== User List ===");
        System.out.printf("%-20s %-10s\n", "Username", "Role");
        System.out.println("-------------------- ----------");

        for (User user : users) {
            System.out.printf("%-20s %-10s\n", user.getUsername(), user.getRole());
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void prompt(String message) {
        System.out.print(message);
    }
}
