package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;


import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.createUser(user));
    }

    public void deleteUser(int id) {
        userStorage.deleteUser(id);
    }

    public UserDto updateUser(int id, UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.updateUser(id, user));
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUser();
    }

    public UserDto getUserById(int id) {
        return UserMapper.toUserDto(userStorage.getUserById(id));
    }
}

