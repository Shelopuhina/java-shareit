package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public class ItemMapper {
    public static ItemDto toItemDtoComplex(Item item, Booking lastBooking, Booking nextbooking, List<CommentDto> comments) {
        ItemDto itemDto = ItemDto.builder()
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .id(item.getId())
                .requestId(item.getRequestId())
                .comments(comments)
                .build();
        if (!(lastBooking == null)) {
            itemDto.setLastBooking(BookingMapper.bookingToDtoOutLong(lastBooking));
        } else {
            lastBooking = null;
        }
        if (!(nextbooking == null)) {
            itemDto.setNextBooking(BookingMapper.bookingToDtoOutLong(nextbooking));
        } else {
            nextbooking = null;
        }
        return itemDto;
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .available(itemDto.getAvailable())
                .description(itemDto.getDescription())
                .id(itemDto.getId())
                .requestId(itemDto.getRequestId())
                .build();
    }

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .id(item.getId())
                .requestId(item.getRequestId())
                .build();
    }
}
