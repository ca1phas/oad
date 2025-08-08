package controller;

import model.User;
import model.enums.UserRole;
import service.UserService;
import view.UserView;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserController {
    private final Scanner sc;
    private final UserService userService;
    private final UserView userView;
    private User currentUser;

    public UserController(Scanner sc) {
        this.sc = sc;
        this.userService = new UserService();
        this.userView = new UserView(sc);
    }

    public void setCurrentUser(User currentUser){
        this.currentUser = currentUser;
    }

    public void handleAccountMenu(User currentUser) {
        while (true) {
            userView.displayAccountMenu(currentUser);
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    handleUpdateUsername(currentUser);
                    break;
                case "2":
                    handleUpdatePassword(currentUser);
                    break;
                case "3":
                    if (handleDeleteUser(currentUser, currentUser)) {
                        System.out.println("Your account has been deleted.");
                        System.exit(0);
                    }
                    break;
                case "4":
                    System.out.println("Returning to previous menu...");
                    return;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        } 
    }

    public void handleUserManagementMenu(User currentUser) {
        if (!currentUser.isAdmin()){
            System.out.print("Access denied.");
            return;
        }
        
        while(true) {
            userView.displayUserManagementMenu();
            int choice = userView.promptInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    handleViewByPage();
                    break;
                case 2:
                    handleFilterAndSort();
                    break;
                case 3:
                    handleViewUserDetails(currentUser);
                    break;
                case 4:
                    handleAdminCreateUser(currentUser);
                    break;
                case 5:
                    System.out.println("Returning to admin dashboard...");
                    return;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    public void handleUserDetailView(User currentUser) {
        String username = userView.promptString("Enter username to view details: ");
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            System.out.println("User not found.");
            return;
        }
        User selectedUser = userOpt.get();
        while (true) {
            userView.displayUserDetailMenu(selectedUser);
            int choice = userView.promptInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    handleUpdateUsername(selectedUser);
                    break;
                case 2: 
                    handleUpdatePassword(selectedUser);
                    break;
                case 3: 
                    handleUpdateRole(selectedUser);
                    break;
                case 4:
                    if (handleDeleteUser(currentUser,selectedUser)) {
                        System.out.println("User deleted successfully.");
                        return;
                    }
                    break;
                case 5:
                    System.out.println("Returning to user menu...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        } 
    }

    public void handleUpdateUsername(User currentUser) {
        String newUsername = userView.promptString("Enter new username: ");

        // Reason checks before calling service
        if (newUsername == null || newUsername.isBlank()) {
            userView.displayMessage("Username update failed: Username cannot be empty.");
            return;
        }

        if (userService.usernameExists(newUsername)) {
            userView.displayMessage("Username update failed: Username already exists.");
            return;
        }

        // Proceed with service call
        boolean success = userService.updateUsername(currentUser.getUsername(), newUsername,
                currentUser.isAdmin(), currentUser.getUsername());

        if (success) {
            currentUser.setUsername(newUsername);
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

    public boolean handleDeleteUser(User currentUser, User userToDelete) {
        boolean isSelfDelete = currentUser.getUsername().equals(userToDelete.getUsername());
        
        if (isSelfDelete) {
            userView.prompt("Are you sure you want to delete your account? (yes/no): ");
            String confirm = sc.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes")) {
                userView.displayMessage("Account deletion cancelled.");
                return false;
            }
        } else {
            if (currentUser.isAdmin() && userToDelete.getUsername().equals(currentUser.getUsername())){
                userView.displayMessage("Admin cannot delete own account here.");
                return false;
            }
        }

        boolean deleted = userService.deleteUser(
            userToDelete.getUsername(),currentUser.isAdmin(), currentUser.getUsername());
            
        if (deleted){
            if (isSelfDelete){
                userView.displayMessage("Account deleted successfully.\nYou have been logged out.");
                System.exit(0); 
            } else {
                userView.displayMessage("User deleted successfully.");
            }
            return true;
        } else {
            userView.displayMessage("Failed to delete account.");
            return false;
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

        try {
            UserRole newRole = UserRole.valueOf(role.toUpperCase());
            boolean success = userService.updateRole(username, newRole, currentUser.isAdmin());
            if (success){
                if (username.equals(currentUser.getUsername())){
                    currentUser.setRole(newRole);
                }
                userView.displayMessage("Role updated successfully.");
            } else {
                userView.displayMessage("Role update failed. User might not exist.");
            }
        } catch (IllegalArgumentException e) {
            userView.displayMessage("Invalid role entered.");
        }
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

    private void handleViewUserDetails(User currentUser) {
        userView.prompt("Enter username to view: ");
        String username = sc.nextLine();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            userView.displayMessage("User not found.");
            return;
        } 

        User user = userOpt.get();

        while (true){
            int choice = userView.viewUserDetails(user);

            switch (choice){
                case 1:
                    handleUpdateUsername(user);
                    break;
                case 2:
                    handleUpdatePassword(user);
                    break;
                case 3:
                    handleUpdateRole(user);
                    break;
                case 4:
                    if (handleDeleteUser(currentUser, user)){
                        return;
                    }
                    break;
                case 5:
                    System.out.println("Returning to user management menu...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
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
