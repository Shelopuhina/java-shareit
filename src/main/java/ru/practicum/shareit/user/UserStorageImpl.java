package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.DuplicationEmailException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserStorageImpl implements UserStorage {
    private Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public UserDto createUser(User user) {
        checkDuplicateUser(user);
        user.setId(nextId);
        nextId++;
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<User> getAllUser() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    @Override
    public UserDto updateUser(int id, User userToUpdate) {
        if (users.containsKey(id)) {
            userToUpdate.setId(id);
            User oldUser = users.get(userToUpdate.getId());
            if (userToUpdate.getName() == null) userToUpdate.setName(oldUser.getName());
            if (userToUpdate.getEmail() == null) userToUpdate.setEmail(oldUser.getEmail());
            checkDuplicateUser(userToUpdate);

            users.put(id, userToUpdate);
            return UserMapper.toUserDto(userToUpdate);
        }
        throw new NotFoundException(String.format("Пользователь с id = %s не существует", id));
    }

    @Override
    public UserDto getUserById(int id) {
        return users.values().stream().filter(user -> user.getId() == id)
                .findFirst()
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %s не существует", id)));
    }

    private void checkDuplicateUser(User checkUser) {
        boolean hasDuplicateEmail = users.values().stream()
                .anyMatch(user -> ((checkUser.getEmail().equals(user.getEmail())) && (checkUser.getId() != user.getId())));
        if (hasDuplicateEmail) {
            throw new DuplicationEmailException(String.format("Пользователь с email %s уже существует",
                    checkUser.getEmail()));
        }
    }
}