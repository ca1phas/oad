package view;

import model.User;
import model.enums.UserRole;

import java.util.Optional;
import java.util.Scanner;

public class AuthView {
    private final Scanner sc;

    public AuthView(Scanner sc) {
        this.sc = sc;
    }

    // === Main Menu ===
    public void displayWelcome() {
        System.out.println("\n=== Welcome ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }

    // === General Prompts and Messages ===
    public void prompt(String message) {
        System.out.print(message);
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    // === Login Section ===
    public void showLoginHeader() {
        System.out.println("\n=== Login ===");
    }

    public String promptUsername() {
        System.out.print("Enter username: ");
        return sc.nextLine();
    }

    public String promptPassword() {
        System.out.print("Enter password: ");
        return sc.nextLine();
    }

    public void showUserNotFound() {
        System.out.println("User not found.");
    }

    public void showIncorrectPassword() {
        System.out.println("Incorrect password.");
    }

    public void showLoginSuccess(User user) {
        System.out.println("Login successful.\n\nWelcome, " + user.getUsername() + "!");
    }

    public void displayLoginSuccess(User user) {
        System.out.println("Login successful. Welcome, " + user.getUsername() + "!");
    }

    public void displayLoginFailed() {
        System.out.println("Login failed. Invalid username or password.");
    }

    // === Signup Section ===
    public void showSignupHeader() {
        System.out.println("\n=== Sign Up ===");
    }

    public String promptConfirmPassword() {
        String confirm = userView.promptPassword("Confirm password: ");

        return sc.nextLine();
    }

    public void showPasswordMismatch() {
        System.out.println("Password and confirmation do not match.");
    }

    public void showUserAlreadyExists() {
        System.out.println("User already exists.");
    }

    public void showSignupSuccess() {
        System.out.println("Sign-up successful! You can now log in.");
    }

    public void displayRegisterSuccess() {
        System.out.println("Registration successful. You can now log in.");
    }

    public void displayRegisterFailed(String reason) {
        System.out.println("Registration failed: " + reason);
    }

    // === Admin Create User ===
    public void showCreateUserHeader() {
        System.out.println("\n=== Create New User (Admin) ===");
    }

    public String promptRole() {
        System.out.print("Enter role (ADMIN/MEMBER): ");
        return sc.nextLine().trim().toUpperCase();
    }

    public void showUserCreated() {
        System.out.println("User created successfully.");
    }

    public Optional<User> promptLogin() {
        System.out.print("Enter username: ");
        String username = sc.nextLine();

        String password = userView.promptPassword("Enter password: ");

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password cannot be empty.");
            return Optional.empty();
        }

        if (username.equals("admin") && password.equals("admin")) {
            return Optional.of(new User("admin", "admin", UserRole.fromString("ADMIN")));
        } else if (username.equals("user") && password.equals("user")) {
            return Optional.of(new User("user", "user", UserRole.fromString("MEMBER")));
        } else {
            System.out.println("Login failed. Invalid username or password.");
            return Optional.empty();
        }
    }
}
