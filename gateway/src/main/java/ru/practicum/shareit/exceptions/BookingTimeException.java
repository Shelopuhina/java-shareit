package ru.practicum.shareit.exceptions;

public class BookingTimeException extends RuntimeException {
    public BookingTimeException(String mes) {
        super(mes);
    }
}
