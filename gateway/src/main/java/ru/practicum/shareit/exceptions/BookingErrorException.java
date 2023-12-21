package ru.practicum.shareit.exceptions;

public class BookingErrorException extends RuntimeException {
    public BookingErrorException(String mes) {
        super(mes);
    }
}
