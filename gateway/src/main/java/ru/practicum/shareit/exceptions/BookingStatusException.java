package ru.practicum.shareit.exceptions;

public class BookingStatusException extends RuntimeException {
    public BookingStatusException(String mes) {
        super(mes);
    }
}
