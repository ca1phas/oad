package controller;

import model.User;
import model.enums.UserRole;
import service.UserService;
import view.AuthView;

import java.util.Optional;
import java.util.Scanner;

public class AuthController {
    private final Scanner sc;
    private final UserService userService;
    private final AuthView authView;

    public AuthController(Scanner sc) {
        this.sc = sc;
        this.userService = new UserService();
        this.authView = new AuthView(sc);
    }

    public Optional<User> handleLogin() {
        authView.showLoginHeader();
        String username = authView.promptUsername();
        String password = authView.promptPassword();

        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) {
            authView.showUserNotFound();
            return Optional.empty();
        }

        if (!user.get().getPassword().equals(password)) {
            authView.showIncorrectPassword();
            return Optional.empty();
        }

        authView.showLoginSuccess(user.get());
        return user;
    }

    public void handleSignup() {
        authView.showSignupHeader();
        String username = authView.promptUsername();
        String password = authView.promptPassword();
        String confirmPassword = authView.promptConfirmPassword();

        if (!password.equals(confirmPassword)) {
            authView.showPasswordMismatch();
            return;
        }

        if (userService.findByUsername(username).isEmpty()) {
            userService.createUser(username, password, "MEMBER", UserRole.MEMBER, true);
            authView.showSignupSuccess();
        } else {
            authView.showUserAlreadyExists();
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
