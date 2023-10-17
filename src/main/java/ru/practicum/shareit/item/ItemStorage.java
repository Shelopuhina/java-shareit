package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    ItemDto addItem(int idUser, Item item);


    ItemDto updateItem(int idItem, Item item, int idOfUserBeingEdited);


    ItemDto getItemById(int itemId);


    List<Item> getItemByUser(int userId);


    List<Item> searchItem(String textQuery);
}