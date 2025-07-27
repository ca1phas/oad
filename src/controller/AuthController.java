package controller;

import model.User;
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

    public void handleAdminCreateUser() {
        authView.showCreateUserHeader();
        String username = authView.promptUsername();
        String password = authView.promptPassword();
        String confirmPassword = authView.promptConfirmPassword();
        String role = authView.promptRole();

        if (!password.equals(confirmPassword)) {
            authView.showPasswordMismatch();
            return;
        }

        if (userService.findByUsername(username).isEmpty()) {
            userService.createUser(username, password, role);
            authView.showUserCreated();
        } else {
            authView.showUserAlreadyExists();
        }
    }
}
