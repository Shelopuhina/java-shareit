package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    UserDto createUser(User user);

    List<User> getAllUser();

    void deleteUser(int id);

    UserDto updateUser(int id, User user);

    UserDto getUserById(int id);
}
