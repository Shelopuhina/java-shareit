package ru.practicum.shareit.request.dto;

import net.bytebuddy.matcher.FilterableList;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto ToDto(ItemRequest itemRequest) {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
        List<ItemDto> items = (itemRequest.getItems() == null) ?
                Collections.emptyList() :
                itemRequest.getItems().stream()
                        .map(item -> ItemMapper.toItemDtoComplex(item, null, null, null))
                        .collect(Collectors.toList());
        itemRequestDto.setItems(items);
        return itemRequestDto;
    }

    public static ItemRequest FromDto(ItemRequestDto itemRequestDto, User requestor) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .items(null)
                .build();
    }
}
