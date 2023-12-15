package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut createBooking(BookingDtoIn bookingDtoIn);

    BookingDtoOut approveBooking(int userId, int bookingId, Boolean approved);

    BookingDtoOut getBookingById(int bookingId, int userId);

    List<BookingDtoOut> getUserBookings(int userId, String state, int from, int size);

    List<BookingDtoOut> getOwnerBookings(int userId, String state, int from, int size);
}