package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ItemStorageImpl implements ItemStorage {
    private final Map<Integer, Item> items = new HashMap<>();
    private int nextId = 1;


    @Override
    public ItemDto addItem(int idUser, Item item) {
        item.setOwner(idUser);
        item.setId(nextId);
        nextId++;
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(int id, Item itemToUpdate, int userId) {
        if (items.get(id).getOwner() != userId) {
            throw new NotFoundException(String.format("У предмета с id = %d не совпадает id владельца = %d",
                    id, userId));
        }
        itemToUpdate.setId(id);
        Item oldItem = items.get(itemToUpdate.getId());
        if (itemToUpdate.getName() == null) itemToUpdate.setName(oldItem.getName());
        if (itemToUpdate.getDescription() == null) itemToUpdate.setDescription(oldItem.getDescription());
        if (itemToUpdate.getAvailable() == null) itemToUpdate.setAvailable(oldItem.getAvailable());
        itemToUpdate.setOwner(oldItem.getOwner());
        itemToUpdate.setRequest(oldItem.getRequest());

        items.put(itemToUpdate.getId(), itemToUpdate);
        return ItemMapper.toItemDto(itemToUpdate);
    }

    @Override
    public ItemDto getItemById(int itemId) {
        if (!items.containsKey(itemId))
            throw new NotFoundException(String.format("Предмета с id = %s не существует", itemId));
        return ItemMapper.toItemDto(items.get(itemId));
    }

    @Override
    public List<Item> getItemByUser(int userId) {
        return items.values().stream()
                .filter(item -> item.getOwner() == userId)
                .collect(Collectors.collectingAndThen(Collectors.toList(), itemList -> {
                    if (itemList.isEmpty()) {
                        throw new NotFoundException(String.format("У пользователся id = %s нету предметов", userId));
                    }
                    return itemList;
                }));
    }

    @Override
    public List<Item> searchItem(String text) {
        if (text.isBlank()) return new ArrayList<>();
        return items.values().stream().filter(item ->
                        ((item.getName().toLowerCase().contains(text.toLowerCase())
                                || (item.getDescription().toLowerCase().contains(text.toLowerCase())))
                                && item.getAvailable()))
                .collect(Collectors.toList());
    }

}