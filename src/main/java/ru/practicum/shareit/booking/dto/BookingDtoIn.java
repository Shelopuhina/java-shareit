package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
@Builder
public class BookingDtoIn {
    private int id;
    @NotNull
    @Future
    private final LocalDateTime start;
    @NotNull
    @Future
    private final LocalDateTime end;
    @NotNull
    private final int itemId;
    private int bookerId;
    private final BookingStatus status = BookingStatus.WAITING;
}
