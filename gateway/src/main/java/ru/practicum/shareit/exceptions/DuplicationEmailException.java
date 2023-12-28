package ru.practicum.shareit.exceptions;

public class DuplicationEmailException extends RuntimeException {
    public DuplicationEmailException(String mes) {
        super(mes);
    }
}
