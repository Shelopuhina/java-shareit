package ru.practicum.shareit.booking;

import lombok.Getter;

import java.util.Arrays;

public enum BookingStatus {
    WAITING(1),
    APPROVED(2),
    REJECTED(3),
    CANCELED(4);
    @Getter
    private int id;

    BookingStatus(int id) {
        this.id = id;
    }

    public BookingStatus getBookingStatusById(int id) {
        return Arrays.stream(BookingStatus.values())
                .filter(bookingStatus -> bookingStatus.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
