package model;

import model.base.Identifiable;
import model.enums.UserRole;

public class User implements Identifiable {
    private String username;
    private String password;
    private UserRole role;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isAdmin(){
        return UserRole.ADMIN.equals(this.role);
    }

    @Override
    public String toString() {
        return username + " (" + role.toString() + ")";
    }

    @Override
    public String getKey() {
        return username;
    }
}
