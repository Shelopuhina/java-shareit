package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;

import ru.practicum.shareit.item.dto.ItemDto;
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

    public ItemDto createItem(int userId, Item item) {
        userStorage.getUserById(userId);
        return itemStorage.addItem(userId, item);
    }

    public ItemDto updateItem(int itemId, Item item, int userId) {
        userStorage.getUserById(userId);
        return itemStorage.updateItem(itemId, item, userId);
    }

    public ItemDto getItemById(int itemId) {
        return itemStorage.getItemById(itemId);
    }

    public List<Item> getItemsByUser(int userId) {
        return itemStorage.getItemByUser(userId);
    }

    public List<Item> searchItem(String text) {
        return itemStorage.searchItem(text);
    }
}
