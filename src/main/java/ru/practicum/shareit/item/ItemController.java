package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") int userId, @RequestBody @Valid ItemDto itemDto) {
        log.info("Выполняется POST-запрос. Создание нового предмета.");
        return service.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable int itemId, @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Выполняется PATCH-запрос. Обновление уже существующего предмета.");
        return service.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable int itemId) {
        log.info("Выполняется GET-запрос. Получение уже существующего предмета.");
        return service.getItemById(itemId);
    }

    @GetMapping
    public List<Item> getItemByUser(@RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Выполняется GET-запрос. Получение предметов пользователя.");
        return service.getItemsByUser(userId);
    }

    @GetMapping("/search")
    public List<Item> searchItem(@RequestParam("text") String description) {
        log.info("Выполняется GET-запрос. Получение предмета по ключевым словам.");
        return service.searchItem(description);
    }
}
