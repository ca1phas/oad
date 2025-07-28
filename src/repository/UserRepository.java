package repository;

import model.User;
import model.enums.UserRole;

import java.util.*;

public class UserRepository extends BaseRepository<User> {

    public UserRepository() {
        super("data/users.txt", "username|password|role");
    }

    @Override
    protected User mapToModel(List<String> row) {
        return new User(
                Integer.parseInt(row.get(0)),
                row.get(1),
                row.get(2),
                UserRole.fromString(row.get(2)));
    }

    @Override
    protected List<String> mapFromModel(User user) {
        return Arrays.asList(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getPassword(),
                user.getRole().toString());
    }

    public Optional<User> findByUsername(String username) {
        return findByKey(username);
    }

    public boolean update(User updated) {
        return updateByKey(updated);
    }

    public boolean delete(String username) {
        return deleteByKey(username);
    }
}
