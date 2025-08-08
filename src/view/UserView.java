package view;

import model.User;
import model.enums.UserRole;

import java.util.List;
import java.util.Scanner;
import java.io.Console;

public class UserView {
    private final Scanner sc;

    public UserView(Scanner sc) {
        this.sc = sc;
    }

    public void displayAccountMenu(User user) {
        System.out.println("\n=== My Account ===");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Role: " + (user.getRole() == UserRole.ADMIN ? "Admin" : "Member"));
        System.out.println("1. Update My Username");
        System.out.println("2. Update My Password");
        System.out.println("3. Delete My Account");
        System.out.println("4. Back");
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

    public void displayUserDetailMenu(User user) {
        System.out.println("\n=== User Details ===");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Role: " + (user.getRole() == UserRole.ADMIN ? "Admin" : "Member"));
        System.out.println("1. Update User Name");
        System.out.println("2. Update User Password");
        System.out.println("3. Update User Role");
        System.out.println("4. Delete User Account");
        System.out.println("5. Back");
        System.out.print("Enter your choice: ");
    }

    public String promptPassword(String label){
        Console console = System.console();
        System.out.print(label + " (press [u] to unhide or just press enter to hide): ");
        String choice = sc.nextLine().trim();

        if (choice.toLowerCase().startsWith("u") && choice.length() > 1){
            return choice.substring(1).trim();
        }

        if (choice.equalsIgnoreCase("u")){
            System.out.print(label + ": ");
            return sc.nextLine().trim();
        }

        if (console != null){
            char[] passwordChars = console.readPassword(label + ": ");
                return passwordChars != null ? new String(passwordChars).trim() : "";
        } else {
            System.out.print(label + ": ");
            return sc.nextLine().trim();
        }
    }

    public String promptString(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    public int promptInt(String message){
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e){
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void prompt(String message){
        System.out.print(message);
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

    public int viewUserDetails(User user) {
        System.out.println("\n=== User Details ===");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Role: " + (user.getRole() == UserRole.ADMIN ? "Admin" : "Member"));
        System.out.println("1. Update Username");
        System.out.println("2. Update User Password");
        System.out.println("3. Update User Role");
        System.out.println("4. Delete User Account");
        System.out.println("5. Back");
        return promptInt("Enter your choice: ");
    }
}
