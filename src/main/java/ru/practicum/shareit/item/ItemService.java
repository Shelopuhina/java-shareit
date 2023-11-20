package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;

import java.util.List;


@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;


    public ItemService(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    public ItemDto createItem(int userId, ItemDto itemDto) {
        userStorage.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        itemStorage.addItem(userId, item);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        userStorage.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemStorage.updateItem(itemId, item, userId));
    }

    public ItemDto getItemById(int itemId) {
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    public List<Item> getItemsByUser(int userId) {
        return itemStorage.getItemByUser(userId);
    }

    public List<Item> searchItem(String text) {
        return itemStorage.searchItem(text);
    }
}
