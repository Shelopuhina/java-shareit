package ru.practicum.shareit.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String mes) {
        super(mes);
    }
}
