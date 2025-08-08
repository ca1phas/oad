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

    // Overloaded method:
    public String promptPassword(String message) {
        System.out.print(message);
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

    public void displayLoginFailed() {
        System.out.println("Login failed. Invalid username or password.");
    }

    // === Signup Section ===
    public void showSignupHeader() {
        System.out.println("\n=== Sign Up ===");
    }

    public String promptConfirmPassword() {
        return promptPassword();
    }

    // Overloaded method:
    public String promptConfirmPassword(String message) {
        System.out.print(message);
        return sc.nextLine();
    }

    public void showPasswordMismatch() {
        System.out.println("Password and confirmation do not match.");
    }

    public void showSignupSuccess() {
        System.out.println("Sign-up successful! You can now log in.");
    }

    public void showSignupAndAutoLoginSuccess(User user){
        System.out.println("Sign-up successful! \nAuto-login successful!\n");
        System.out.println("Welcome, " + user.getUsername() + "!");
    }

    // === Admin Create User ===
    public void showCreateUserHeader() {
        System.out.println("\n=== Create New User (Admin) ===");
    }

    public String promptRole() {
        while (true) {
            System.out.print("Enter role (ADMIN/MEMBER): ");
            String role = sc.nextLine().trim().toUpperCase();
            if (role.equals("ADMIN") || role.equals("MEMBER")) {
                return role;
            } else {
                System.out.println("Invalid role. Please enter 'ADMIN' or 'MEMBER'.");
            }
        }
    }
    
    public void showUserCreated() {
        System.out.println("User created successfully.");
    }

    // === Common Success/Failure Display ===
    public void displayRegisterSuccess() {
        System.out.println("Registration successful. You can now log in.");
    }

    public void displayRegisterFailed(String reason) {
        System.out.println("Registration failed: " + reason);
    }

    public String showUserAlreadyExists() {
        return "User already exists.";
    }
}
