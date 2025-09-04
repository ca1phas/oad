package repository;

import model.User;
import model.enums.UserRole;

import java.util.*;

public class UserRepository extends BaseRepository<User> {

    // Constructor sets file path and header schema for users.txt
    public UserRepository() {
        super("data/users.txt", "username|password|role");
    }

    // Convert a row from the text file into a User object
    @Override
    protected User mapToModel(List<String> row) {
        return new User(
                Integer.parseInt(row.get(0)), // ID
                row.get(1), // Username
                row.get(2), // Password
                UserRole.fromString(row.get(3)) // Role (ADMIN / MEMBER)
        );
    }

    // Convert a User object into a list of strings for file storage
    @Override
    protected List<String> mapFromModel(User user) {
        return Arrays.asList(
                String.valueOf(user.getId()), // ID
                user.getUsername(), // Username
                user.getPassword(), // Password
                user.getRole().toString() // Role
        );
    }

    // Find a user by username (case-insensitive)
    public Optional<User> findByUsername(String username) {
        return readAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    // Update an existing user in the file
    public boolean update(User updated) {
        return updateByKey(updated);
    }

    // Delete a user by username
    public boolean delete(String username) {
        return deleteByKey(username);
    }
}
