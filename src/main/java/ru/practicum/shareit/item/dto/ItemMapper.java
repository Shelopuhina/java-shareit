package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public class ItemMapper {
    public static ItemDto toItemDtoComplex(Item item, Booking lastBooking, Booking nextbooking, List<CommentDto> comments) {
        ItemDto itemDto = ItemDto.builder()
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .id(item.getId())
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
                .build();
    }

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .id(item.getId())
                .requestId(item.getRequest() == null ? null : item.getRequest().getId())
                .build();
    }

    public static Item itemFromDto(ItemDto itemDto, User user, ItemRequest itemRequest) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .owner(user)
                .request(itemRequest)
                .available(itemDto.getAvailable())
                .build();
    }

    public static Item toItemReq(ItemDto itemDto, ItemRequest itemRequest) {
        return Item.builder()
                .name(itemDto.getName())
                .available(itemDto.getAvailable())
                .description(itemDto.getDescription())
                .id(itemDto.getId())
                .request(itemRequest)
                .build();
    }

    public static ItemDto toItemDtoComplexReqId(Item item, Booking lastBooking, Booking nextbooking, List<CommentDto> comments, ItemRequest itemRequest) {
        ItemDto itemDto = ItemDto.builder()
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .id(item.getId())
                .comments(comments)
                .requestId(itemRequest.getId())
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
}
