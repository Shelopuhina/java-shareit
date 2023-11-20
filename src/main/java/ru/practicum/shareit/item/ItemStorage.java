package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item addItem(int idUser, Item item);


    Item updateItem(int idItem, Item item, int idOfUserBeingEdited);


    Item getItemById(int itemId);


    List<Item> getItemByUser(int userId);


    List<Item> searchItem(String textQuery);
}