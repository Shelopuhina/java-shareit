package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;


import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;


    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("Выполняется POST-запрос. Создание пользователя.");
        return service.createUser(userDto);
    }


    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        log.info("Выполняется DELETE-запрос. Удаление пользователя.");
        service.deleteUser(id);
    }


    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable int id, @RequestBody UserDto userDto) {
        log.info("Выполняется PATCH-запрос. Обновление пользователя.");
        return service.updateUser(id, userDto);
    }


    @GetMapping
    public List<User> getAllUser() {
        log.info("Выполняется GET-запрос. Получение всех пользователей.");
        return service.getAllUsers();
    }


    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable int id) {
        log.info("Выполняется GET-запрос. Получение пользователя по id.");
        return service.getUserById(id);
    }
}