package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Builder
@Data
public class User {
    private int id;
    @NotNull
    private String name;
    @NotNull
    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @")
    private String email;
}
