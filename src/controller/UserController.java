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
                            System.out.println("You have been logged out because your account was deleted.");
                            System.exit(0);
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
                    handleViewByPage();
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
            userView.displayMessage("Password updated successfully.");
        } else {
            userView.displayMessage("Password update failed. Your old password might be wrong.");
        }
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
                    System.exit(0);
        } else {
            userView.displayMessage(deleted ? "User deleted successfully." : "Delete failed.");
        }

        return deleted && usernameToDelete.equals(currentUser.getUsername());
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
        userView.displayMessage(success ? "Role updated successfully." : "Role update failed. User might not exist.");
    }

    public void handleAdminCreateUser(User currentUser) {
        if (!currentUser.isAdmin()){
            userView.displayMessage("Only admins can create users.");
            return;
        }

        userView.prompt("Enter new username: ");
        String username = sc.nextLine();

        String password = userView.promptPassword("Enter password: ");
        String confirm = userView.promptPassword("Confirm password: ");
        
        UserRole userRole = null;
        while (true){
            userView.prompt("Enter role (ADMIN/MEMBER): ");
            String roleInput = sc.nextLine().trim().toUpperCase();

            if (roleInput.equals("ADMIN") || roleInput.equals("MEMBER")){
                userRole = UserRole.valueOf(roleInput);
                break;
            } else {
                userView.displayMessage("Invalid role entered. Please enter either ADMIN or MEMBER.");
            }
        }

        boolean created = userService.createUser(username, password, confirm, userRole, true);
        userView.displayMessage(created ? "User created successfully." : "User creation failed.");
    }

    public void handleViewByPage(){
        List<User> allUsers = userService.filterSortPaginateUsers(
            null, null, "username",
            true, 1, Integer.MAX_VALUE);
        handleUserPagination(allUsers);

    }

    public void handleFilterAndSort() {
        userView.prompt("Enter username filter (press enter to skip): ");
        String usernameFilter = sc.nextLine().trim();
        if (usernameFilter.isEmpty()) usernameFilter = null;

        userView.prompt("Enter role filter (ADMIN/USER, press enter to skip): ");
        String roleInput = sc.nextLine().trim();
        UserRole roleFilter = null;
        if (!roleInput.isEmpty()){
            try {
                roleFilter = UserRole.valueOf(roleInput.toUpperCase());
            } catch (IllegalArgumentException e){
                System.out.println("Invalid role. Skipping role filter.");
            }
        }

        userView.prompt("Sort by (username/role): ");
        String sortField = sc.nextLine().trim();
        if (sortField.isEmpty()) sortField = "username";

        userView.prompt("Order (asc/desc): ");
        String sortOrder = sc.nextLine().trim().toLowerCase();
        boolean ascending = !sortOrder.equals("desc");

        List<User> allUsers = userService.filterSortPaginateUsers(
                usernameFilter,roleFilter,sortField,ascending, 1,Integer.MAX_VALUE);
        
        handleUserPagination(allUsers);
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

    private void handleUserPagination(List<User> users){
        final int usersPerPage = 10;
        int totalPages = (int) Math.ceil((double) users.size() / usersPerPage);
        int currentPage = 1;

        if (users.isEmpty()){
            System.out.println("No users to display.");
            return;
        }

        while (true){
            List<User> pageUsers = users.stream().skip((currentPage - 1) * usersPerPage)
            .limit(usersPerPage).toList();
                        
            userView.displayUsers(pageUsers);
            System.out.printf("\nPage (%d/%d)\n", currentPage, totalPages);
            System.out.printf("Enter page number (1-%d) or 0 to go back: ", totalPages);

            String input = sc.nextLine().trim();
            if (input.equals("0")) break;

            try{
                int selectedPage = Integer.parseInt(input);
                if (selectedPage >= 1 && selectedPage <= totalPages){
                    currentPage = selectedPage;
                 } else {
                    System.out.println("Invalid page number.");
                 } 
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}
