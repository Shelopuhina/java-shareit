package ru.practicum.shareit.exceptions;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String mes) {
        super(mes);
    }
}
