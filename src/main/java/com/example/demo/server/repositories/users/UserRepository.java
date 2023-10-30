package com.example.demo.server.repositories.users;

import com.example.demo.common.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static UserRepository INSTANCE = null;
    private final List<User> users = List.of(
            new User(
                    1,
                    "kevin",
                    BCrypt.hashpw("kevin1234", BCrypt.gensalt(12)),
                    User.Role.ADMIN
            ),
            new User(
                    2,
                    "Bryan",
                    BCrypt.hashpw("Bryan1234", BCrypt.gensalt(12)),
                    User.Role.USER
            )
    );

    private UserRepository() {
    }

    public synchronized static UserRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserRepository();
        }
        return INSTANCE;
    }

    public Optional<User> findByByUsername(String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    public Optional<User> findByById(int id) {
        return users.stream()
                .filter(user -> user.id() == id)
                .findFirst();
    }
}
