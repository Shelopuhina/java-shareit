package ru.practicum.shareit.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ErrorResponse {
    private final String error;
    private String description;

}