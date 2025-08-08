package controller;

import model.User;
import model.enums.UserRole;
import service.UserService;
import view.AuthView;

import java.util.Optional;
import java.util.Scanner;
import java.io.Console;

public class AuthController {
    private final Scanner sc;
    private final UserService userService;
    private final AuthView authView;
    private User currentUser;

    public AuthController(Scanner sc) {
        this.sc = sc;
        this.userService = new UserService();
        this.authView = new AuthView(sc);
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public Optional<User> handleSignup() {
        authView.showSignupHeader();
        String username = authView.promptUsername();
        String password = authView.promptPassword("Enter password: ");
        String confirmPassword = authView.promptPassword("Confirm password: ");

        if (!password.equals(confirmPassword)) {
            authView.showPasswordMismatch();
            return Optional.empty();
        }

        if (userService.usernameExists(username)) {
            authView.displayRegisterFailed(authView.showUserAlreadyExists());
            return Optional.empty();
        }

        boolean success = userService.signup(username, password, confirmPassword);

        if (success) {
            // Auto-Login after Register
            Optional<User> userOpt = userService.login(username, password);
            if(userOpt.isPresent()){
                currentUser = userOpt.get();
                authView.showSignupAndAutoLoginSuccess(currentUser);
                return userOpt;
            } else {
                authView.displayRegisterFailed("Signup succeeded, but auto-login failed. Please login manually.");
            }
        } else {
            authView.displayRegisterFailed("Unknown error during registration.");
        }

        return Optional.empty();
    }

    public Optional<User> handleLogin() {
        authView.showLoginHeader();
        String username = authView.promptUsername();
        String password = authView.promptPassword();

        // Input validation
        if (username.isBlank() || password.isBlank()) {
            authView.displayMessage("Username and password cannot be empty.");
            return Optional.empty();
        }

        // Attempt login via UserService
        Optional<User> user = userService.login(username, password);

        // Login result handling
        if (user.isPresent()) {
            authView.showLoginSuccess(user.get());
            return user;
        } else {
            authView.showUserNotFound();
            return Optional.empty();
        }
    }

    public boolean createUser(String username, String password, String confirmPassword, String roleStr) {
        try {
            UserRole role = UserRole.valueOf(roleStr.toUpperCase());
            return userService.createUser(username, password, confirmPassword, role, true);
        } catch (IllegalArgumentException e) {
            authView.displayMessage("Invalid role: " + roleStr);
            return false;
        }
    }
}
