package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .id(item.getId())
                .request(item.getRequest())
                .build();
    }
}
