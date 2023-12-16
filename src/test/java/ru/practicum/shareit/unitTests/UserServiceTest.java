package ru.practicum.shareit.unitTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exceptions.DuplicationEmailException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void beforeEach() {
        user = new User(1, "user", "user@email.com");
    }

    @Test
    public void shouldCreateUser() {
        Mockito
                .when(userRepository.save(any(User.class)))
                .then(returnsFirstArg());

        UserDto userDto = userService.createUser(UserMapper.toUserDto(user));
        Assertions.assertAll("Проверка создания пользователя: ",
                () -> assertEquals(userDto.getId(), user.getId()),
                () -> assertEquals(userDto.getName(), user.getName()),
                () -> assertEquals(userDto.getEmail(), user.getEmail()));

    }

    @Test
    public void shouldNotCreateUserWhenEmailNotUnique() {
        Mockito
                .when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        DuplicationEmailException e = Assertions.assertThrows(
                DuplicationEmailException.class,
                () -> userService.createUser(UserMapper.toUserDto(user)));

        Assertions.assertEquals(e.getMessage(), "пользователь user@email.com уже зарегестрирован.");
    }

    @Test
    public void shouldUpdateUser() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userRepository.save(any(User.class)))
                .then(returnsFirstArg());

        UserDto userDto = userService.updateUser(1, UserMapper.toUserDto(user));
        Assertions.assertAll("Проверка обновления пользователя: ",
                () -> assertEquals(userDto.getId(), user.getId()),
                () -> assertEquals(userDto.getName(), user.getName()),
                () -> assertEquals(userDto.getEmail(), user.getEmail()));
    }

    @Test
    public void shouldNotUpdateUserWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(1, UserMapper.toUserDto(user))
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не существует");
    }

    @Test
    public void shouldNotUpdateUserWhenEmailNotUnique() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        DuplicationEmailException e = Assertions.assertThrows(
                DuplicationEmailException.class,
                () -> userService.updateUser(1, UserMapper.toUserDto(user))
        );

        Assertions.assertEquals(e.getMessage(), "пользователь user@email.com уже зарегестрирован.");
    }

    @Test
    public void shouldGetUserById() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(1);
        Assertions.assertAll("Проверка обновления пользователя: ",
                () -> assertEquals(userDto.getId(), user.getId()),
                () -> assertEquals(userDto.getName(), user.getName()),
                () -> assertEquals(userDto.getEmail(), user.getEmail()));
    }

    @Test
    public void shouldNotGetUserByIdWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(1)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldGetAllUsers() {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();
        User user1 = users.get(0);
        Assertions.assertAll("Проверка получуния всех пользователей: ",
                () -> assertEquals(users.size(), 1),
                () -> assertEquals(user1.getId(), user.getId()),
                () -> assertEquals(user1.getName(), user.getName()),
                () -> assertEquals(user1.getEmail(), user.getEmail()));
    }
}

