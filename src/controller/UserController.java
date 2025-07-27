package controller;

import model.User;
import service.UserService;
import view.UserView;

import java.util.Optional;
import java.util.Scanner;

public class UserController {
    private final Scanner sc;
    private final UserService userService;
    private final UserView userView;

    public UserController(Scanner sc) {
        this.sc = sc;
        this.userService = new UserService();
        this.userView = new UserView(sc);
    }

    public void handleAccountMenu(User currentUser) {
        boolean inMenu = true;
        while (inMenu) {
            userView.displayAccountMenu();
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
                    handleUpdateRole(currentUser);
                    break;
                case "5":
                    inMenu = !handleDeleteUser(currentUser);
                    break;
                case "6":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }
        }
    }

    public void handleUpdateUsername(User currentUser) {
        userView.prompt("Enter new username: ");
        String newUsername = sc.nextLine();
        boolean success = userService.updateUsername(currentUser, newUsername);
        userView.displayMessage(success ? "Username updated successfully." : "Username update failed.");
    }

    public void handleUpdatePassword(User currentUser) {
        userView.prompt("Enter old password: ");
        String oldPassword = sc.nextLine();
        userView.prompt("Enter new password: ");
        String newPassword = sc.nextLine();
        userView.prompt("Confirm new password: ");
        String confirm = sc.nextLine();

        boolean success = userService.updatePassword(currentUser, oldPassword, newPassword, confirm);
        userView.displayMessage(success ? "Password updated successfully." : "Password update failed.");
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
        boolean success = userService.updateUserRole(currentUser, username, role);
        userView.displayMessage(success ? "Role updated successfully." : "Role update failed.");
    }

    public boolean handleDeleteUser(User currentUser) {
        userView.prompt("Enter username to delete: ");
        String username = sc.nextLine();
        boolean deleted = userService.deleteUser(currentUser, username);
        userView.displayMessage(deleted ? "User deleted successfully." : "Delete failed.");
        return deleted && username.equals(currentUser.getUsername());
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
                    userView.displayUsers(userService.getUsersByPage(1));
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
        userView.prompt("Filter by (username/role): ");
        String filterField = sc.nextLine();
        userView.prompt("Enter filter value: ");
        String filterValue = sc.nextLine();
        userView.prompt("Sort by (username/role): ");
        String sortField = sc.nextLine();
        userView.prompt("Order (asc/desc): ");
        String sortOrder = sc.nextLine();

        var filtered = userService.getFilteredSortedUsers(filterField, filterValue, sortField, sortOrder);
        userView.displayUsers(filtered);
    }

    public void handleViewOtherUser(User currentUser) {
        userView.prompt("Enter username to view: ");
        String username = sc.nextLine();
        Optional<User> selectedUser = userService.getUserByUsername(username);
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

        boolean created = userService.adminCreateUser(currentUser, username, password, confirm, role);
        userView.displayMessage(created ? "User created successfully." : "User creation failed.");
    }
}
