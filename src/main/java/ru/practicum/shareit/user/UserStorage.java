package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User user);

    List<User> getAllUser();

    void deleteUser(int id);

    User updateUser(int id, User user);

    User getUserById(int id);
}
