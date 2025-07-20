package repository;

import model.User;
import model.enums.Role;
import util.FileHandlerUtil;

import java.util.*;

public class UserRepository {
    private final String filePath = "data/users.txt";
    private final String header = "username|password|role";

    // Convert List<String> to User
    public User mapToModel(List<String> row) {
        return new User(
                row.get(0),
                row.get(1),
                Role.fromString(row.get(2)));
    }

    // Convert User to List<String>
    public List<String> mapFromModel(User user) {
        return Arrays.asList(
                user.getUsername(),
                user.getPassword(),
                user.getRole().toString());
    }

    // Read all users from file
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        List<List<String>> rows = FileHandlerUtil.readData(filePath);
        for (List<String> row : rows) {
            users.add(mapToModel(row));
        }
        return users;
    }

    // Save entire list to file
    public void saveAll(List<User> users) {
        List<List<String>> rows = new ArrayList<>();
        for (User user : users) {
            rows.add(mapFromModel(user));
        }
        FileHandlerUtil.writeData(filePath, header, rows);
    }

    // Add a new user
    public void append(User user) {
        FileHandlerUtil.appendDataRow(filePath, mapFromModel(user));
    }

    // Find exact match by username
    public Optional<User> findByUsername(String username) {
        for (User user : findAll()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // Update user with matching username
    public boolean update(User updatedUser) {
        List<User> users = findAll();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                saveAll(users);
                return true;
            }
        }
        return false;
    }

    // Delete user by username
    public boolean delete(String username) {
        List<User> users = findAll();
        boolean removed = users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        if (removed) {
            saveAll(users);
        }
        return removed;
    }
}
