package model;

import model.base.Identifiable;
import model.enums.UserRole;

public class User implements Identifiable {
    private int id; // Unique identifier for the user
    private String username; // Login/display name
    private String password; // User password (plaintext for now – consider hashing later)
    private UserRole role; // Role of the user (ADMIN or MEMBER)

    // Constructor to initialize a User object
    public User(int id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    // Convenience method: check if the user is an Admin
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    // For printing user details in a friendly format
    @Override
    public String toString() {
        return username + " (" + role.toString() + ")";
    }

    // Required by BaseRepository: unique key representation
    @Override
    public String getKey() {
        return String.valueOf(id); // Use id as the unique key
    }
}
