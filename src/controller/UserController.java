package controller;

import model.User;
import model.enums.UserRole;
import service.UserService;
import view.UserView;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.io.Console;

public class UserController {
    private final Scanner sc;
    private final UserService userService;
    private final UserView userView;

    public UserController(Scanner sc) {
        this.sc = sc;
        this.userService = new UserService();
        this.userView = new UserView(sc);
    }

    public boolean handleAccountMenu(User currentUser) {
        boolean inMenu = true;
        while (inMenu) {
            userView.displayAccountMenu(currentUser);
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    userView.viewUserDetails(currentUser);
                    break;
                case "2":
                    handleUpdateUsername(currentUser);
                    break;
                case "3":
                    handleUpdatePassword(currentUser);
                    break;
                case "4":
                    if (currentUser.isAdmin()) {
                        handleUpdateRole(currentUser);
                    } else {
                        boolean deletedOwnAccount = handleDeleteUser(currentUser);
                        if (deletedOwnAccount)
                            return true;
                        inMenu = false;
                    }
                    break;
                case "5":
                    if (currentUser.isAdmin()) {
                        boolean deletedOther = handleDeleteUser(currentUser);
                    } else {
                        inMenu = false;
                    }
                    break;
                case "6":
                    if (currentUser.isAdmin()) {
                        inMenu = false;
                    } else {
                        System.out.print("Invalid option. Try again.");
                    }
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }
        }
        return false;
    }

    public void handleUpdateUsername(User currentUser) {
        userView.prompt("Enter new username: ");
        String newUsername = sc.nextLine();

        // Reason checks before calling service
        if (newUsername == null || newUsername.isBlank()) {
            userView.displayMessage("Username update failed: Username cannot be empty.");
            return;
        }

        if (userService.usernameExists(newUsername)) {
            userView.displayMessage("Username update failed: Username already exists.");
            return;
        }

        if (!currentUser.isAdmin() && !currentUser.getUsername().equals(currentUser.getUsername())) {
            userView.displayMessage("Username update failed: Permission denied.");
            return;
        }

        // Proceed with service call
        boolean success = userService.updateUsername(currentUser.getUsername(), newUsername,
                currentUser.isAdmin(), currentUser.getUsername());

        if (success) {
            userView.displayMessage("Username updated successfully.");
            currentUser.setUsername(newUsername);
        } else {
            userView.displayMessage("Username update failed: Unexpected error occurred.");

        }
    }

    public void handleUpdatePassword(User currentUser) {
        String oldPassword = userView.promptPassword("Enter old password: ");
        String newPassword = userView.promptPassword("Enter new password: ");
        String confirm = userView.promptPassword("Confirm new password: ");

        if (newPassword == null || newPassword.isBlank() || confirm == null) {
            userView.displayMessage("Password update failed. New password cannot be empty or spaces.");
            return;
        }

        if (!newPassword.equals(confirm)) {
            userView.displayMessage("Password update failed. New password and confirmation do not match.");
            return;
        }

        boolean success = userService.updatePassword(
                currentUser.getUsername(), oldPassword, newPassword, confirm,
                currentUser.isAdmin(), currentUser.getUsername());

        if (success) {
            userView.displayMessage(success ? "Password updated successfully."
                    : "Password update failed. Your old password might be wrong.");
        }
    }

    public void handleUpdateRole(User currentUser) {
        if (!currentUser.isAdmin()) {
            userView.displayMessage("Only admins can update roles.");
            return;
        }
        userView.prompt("Enter username to update role: ");
        String username = sc.nextLine();
        userView.prompt("Enter new role (ADMIN/USER): ");
        String role = sc.nextLine();
        boolean success = userService.updateRole(username, model.enums.UserRole.valueOf(role.toUpperCase()),
                currentUser.isAdmin());
        userView.displayMessage(success ? "Role updated successfully." : "Role update failed.");
    }

    public boolean handleDeleteUser(User currentUser) {
        String usernameToDelete;

        if (!currentUser.isAdmin()) {
            userView.prompt("Are you sure you want to delete your account? (yes/no): ");
            String confirm = sc.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes")) {
                userView.displayMessage("Account deletion cancelled.");
                return false;
            }

            usernameToDelete = currentUser.getUsername(); // auto-set
        } else {
            // Admin can choose any username to delete
            userView.prompt("Enter username to delete: ");
            usernameToDelete = sc.nextLine();
        }

        boolean deleted = userService.deleteUser(usernameToDelete, currentUser.isAdmin(), currentUser.getUsername());

        if (deleted && usernameToDelete.equals(currentUser.getUsername())) {
            userView.displayMessage(
                    "Account deleted successfully.\n\nYou have been logged out because your account was deleted.");
        } else {
            userView.displayMessage(deleted ? "User deleted successfully." : "Delete failed.");
        }

        return deleted && usernameToDelete.equals(currentUser.getUsername());
    }

    public void handleUserManagementMenu(User currentUser) {
        if (!currentUser.isAdmin()) {
            userView.displayMessage("Access denied: Admin only.");
            return;
        }

        boolean inMenu = true;
        while (inMenu) {
            userView.displayUserManagementMenu();
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    List<User> firstPage = userService.filterSortPaginateUsers(
                            null, null, "username", true, 1, 10);
                    userView.displayUsers(firstPage);
                    break;
                case "2":
                    handleFilterAndSort();
                    break;
                case "3":
                    handleViewOtherUser(currentUser);
                    break;
                case "4":
                    handleAdminCreateUser(currentUser);
                    break;
                case "5":
                    inMenu = false;
                    break;
                default:
                    userView.displayMessage("Invalid option. Try again.");
                    break;
            }
        }
    }

    public void handleFilterAndSort() {
        userView.prompt("Enter username filter (press enter to skip): ");
        String usernameFilter = sc.nextLine();

        userView.prompt("Enter role filter (ADMIN/USER, press enter to skip): ");
        String roleInput = sc.nextLine();
        var roleFilter = roleInput.isBlank() ? null : model.enums.UserRole.valueOf(roleInput.toUpperCase());

        userView.prompt("Sort by (username/role): ");
        String sortField = sc.nextLine();
        userView.prompt("Order (asc/desc): ");
        String sortOrder = sc.nextLine();

        var sorted = userService.filterSortPaginateUsers(
                usernameFilter,
                roleFilter,
                sortField,
                sortOrder.equalsIgnoreCase("asc"),
                1,
                10);

        userView.displayUsers(sorted);
    }

    public void handleViewOtherUser(User currentUser) {
        userView.prompt("Enter username to view: ");
        String username = sc.nextLine();
        Optional<User> selectedUser = userService.findByUsername(username);
        if (selectedUser.isEmpty()) {
            userView.displayMessage("User not found.");
        } else {
            userView.viewUserDetails(selectedUser.get());
        }
    }

    public void handleAdminCreateUser(User currentUser) {
        userView.prompt("Enter new username: ");
        String username = sc.nextLine();
        userView.prompt("Enter password: ");
        String password = sc.nextLine();
        userView.prompt("Confirm password: ");
        String confirm = sc.nextLine();
        userView.prompt("Enter role (ADMIN/USER): ");
        String role = sc.nextLine();

        boolean created = userService.createUser(
                username, password, confirm,
                model.enums.UserRole.valueOf(role.toUpperCase()),
                currentUser.isAdmin());
        userView.displayMessage(created ? "User created successfully." : "User creation failed.");
    }
}
