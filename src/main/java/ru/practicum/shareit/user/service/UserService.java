package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    void deleteUser(int userId);

    List<User> getAllUsers();

    UserDto updateUser(int id, UserDto userDto);

    UserDto getUserById(int id);


}
