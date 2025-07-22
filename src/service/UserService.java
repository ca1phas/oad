package service;

import java.util.*;
import java.util.stream.Collectors;

import model.User;
import model.enums.UserRole;
import repository.UserRepository;
import util.PaginationUtil;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    // Utility
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // FR02: Sign up
    public boolean signup(String username, String password, String confirmPassword) {
        return createUser(username, password, confirmPassword, UserRole.MEMBER, true);
    }

    // FR03: Log in
    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    // FR04 & FR37: View & select user details
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // FR05: Update username
    public boolean updateUsername(String targetUsername, String newUsername, boolean isAdmin, String currentUsername) {
        if (!isAdmin && !targetUsername.equalsIgnoreCase(currentUsername))
            return false;
        if (usernameExists(newUsername))
            return false;

        Optional<User> optionalUser = findByUsername(targetUsername);
        if (optionalUser.isEmpty())
            return false;

        User user = optionalUser.get();
        user.setUsername(newUsername);
        userRepository.update(user);
        return true;
    }

    // FR06: Update password
    public boolean updatePassword(String targetUsername, String oldPassword,
            String newPassword, String confirmNewPassword,
            boolean isAdmin, String currentUsername) {
        if (!isAdmin && !targetUsername.equalsIgnoreCase(currentUsername))
            return false;
        if (!newPassword.equals(confirmNewPassword))
            return false;

        Optional<User> optionalUser = findByUsername(targetUsername);
        if (optionalUser.isEmpty())
            return false;

        User user = optionalUser.get();
        if (!user.getPassword().equals(oldPassword)
                || !newPassword.equals(confirmNewPassword))
            return false;

        user.setPassword(newPassword);
        userRepository.update(user);
        return true;
    }

    // FR07: Update role
    public boolean updateRole(String targetUsername, UserRole newRole, boolean isAdmin) {
        if (!isAdmin)
            return false;

        Optional<User> optionalUser = findByUsername(targetUsername);
        if (optionalUser.isEmpty())
            return false;

        User user = optionalUser.get();
        if (user.getRole() == newRole)
            return false;

        user.setRole(newRole);
        userRepository.update(user);
        return true;
    }

    // FR08: Delete user (self or admin)
    public boolean deleteUser(String targetUsername, boolean isAdmin, String currentUsername) {
        if (!isAdmin && !targetUsername.equalsIgnoreCase(currentUsername))
            return false;

        Optional<User> optionalUser = findByUsername(targetUsername);
        if (optionalUser.isEmpty())
            return false;

        userRepository.deleteByKey(optionalUser.get().getKey());
        return true;
    }

    // FR38: Admin create user with role
    public boolean createUser(String username, String password, String confirmPassword, UserRole role,
            boolean isAdmin) {
        if (!isAdmin)
            return false;
        if (!password.equals(confirmPassword))
            return false;
        if (usernameExists(username))
            return false;

        User user = new User(username, password, role);
        userRepository.append(user);
        return true;
    }

    // FR35 & FR36 & FR34: Filter + Sort + Paginate users
    public List<User> filterSortPaginateUsers(String usernameFilter, UserRole roleFilter,
            String sortField, boolean ascending, int pageNumber, int pageSize) {
        List<User> users = userRepository.readAll();

        // Filtering
        if (usernameFilter != null && !usernameFilter.isBlank())
            users = users.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(usernameFilter.toLowerCase()))
                    .collect(Collectors.toList());

        if (roleFilter != null)
            users = users.stream()
                    .filter(u -> u.getRole() == roleFilter)
                    .collect(Collectors.toList());

        // Sorting
        Comparator<User> comparator = Comparator.comparing(User::getUsername);
        if ("role".equalsIgnoreCase(sortField))
            comparator = Comparator.comparing(user -> user.getRole().name());

        if (!ascending)
            comparator = comparator.reversed();

        users.sort(comparator);

        return PaginationUtil.paginate(users, pageNumber, pageSize);
    }
}
