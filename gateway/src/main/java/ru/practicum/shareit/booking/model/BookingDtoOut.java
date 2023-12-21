package ru.practicum.shareit.booking.model;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.user.model.UserDto;


import java.time.LocalDateTime;

@Data
@Builder
public class BookingDtoOut {
    private final int id;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final ItemDto item;
    private final UserDto booker;
    private final int bookerId;
    private final BookingStatus status;
}