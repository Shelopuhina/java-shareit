package ru.practicum.shareit.user.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DuplicationEmailException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        try {
            User savedUser = userRepository.save(UserMapper.toUser(userDto));
            return UserMapper.toUserDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicationEmailException("пользователь " + userDto.getEmail() + " уже зарегестрирован.");
        }
    }

    @Transactional
    public void deleteUser(int id) {
        userRepository.deleteById(id);

    }

    @Transactional
    public UserDto updateUser(int id, UserDto userDto) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty())
                throw new NotFoundException("Пользователь с id=" + id + " не существует");
            User oldUser = userOpt.get();
            User userToUpdate = UserMapper.toUser(userDto);
            userToUpdate.setId(id);
            if (userToUpdate.getName() == null) userToUpdate.setName(oldUser.getName());
            if (userToUpdate.getEmail() == null) userToUpdate.setEmail(oldUser.getEmail());
            User user = userRepository.save(userToUpdate);
            return UserMapper.toUserDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicationEmailException("пользователь " + userDto.getEmail() + " уже зарегестрирован.");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDto getUserById(int id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + id + " не найден");
        return UserMapper.toUserDto(userOpt.get());

    }
}

