package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;


import javax.validation.Valid;


/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;


    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Выполняется POST-запрос. Создание пользователя.");
        return userClient.createUser(userDto);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable int id) {
        log.info("Выполняется DELETE-запрос. Удаление пользователя.");
        return userClient.deleteUser(id);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable int id, @RequestBody UserDto userDto) {
        log.info("Выполняется PATCH-запрос. Обновление пользователя.");
        return userClient.updateUser(id, userDto);
    }


    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Выполняется GET-запрос. Получение всех пользователей.");
        return userClient.getAllUsers();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable int id) {
        log.info("Выполняется GET-запрос. Получение пользователя по id.");
        return userClient.getUserById(id);
    }
}