package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    @NotNull
    private String name;
    private int id;
    @NotNull
    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @")
    private String email;
}
