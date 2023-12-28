package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(int userId, ItemDto itemDto);

    ItemDto updateItem(int itemId, ItemDto itemDto, int userId);

    ItemDto getItemById(int itemId, int userId);

    List<ItemDto> getItemsByUser(int userId, int from, int size);

    List<Item> searchItem(String text, int from, int size,int userId);

    CommentDto addComment(int userId, int itemId, CommentDto commentDto);

}
