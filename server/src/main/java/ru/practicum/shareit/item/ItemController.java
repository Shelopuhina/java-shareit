package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

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
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") int userId,
                              @RequestBody ItemDto itemDto) {
        log.info("Выполняется POST-запрос. Создание нового предмета.");
        return service.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable int itemId, @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Выполняется PATCH-запрос. Обновление уже существующего предмета.");
        return service.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader("X-Sharer-User-Id") int userId,
                           @PathVariable int itemId) {
        log.info("Выполняется GET-запрос. Получение уже существующего предмета с айди."+itemId+ " и юзерайди"+userId);
        return service.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItemByUser(@RequestHeader("X-Sharer-User-Id") int userId,
                                       @RequestParam(name = "from", defaultValue = "0")  Integer from,
                                       @RequestParam(defaultValue = "10") int size) {
        log.info("Выполняется GET-запрос. Получение предметов пользователя.");
        return service.getItemsByUser(userId, from, size);
    }

    @GetMapping("/search")
    public List<Item> searchItem(@RequestHeader("X-Sharer-User-Id") int userId,
                                 @RequestParam("text") String description,
                                 @RequestParam(name = "from", defaultValue = "0")  Integer from,
                                 @RequestParam(defaultValue = "10") int size) {
        log.info("Выполняется GET-запрос. Получение предмета по ключевым словам.");
        return service.searchItem(description, from, size,userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") int userId,
                                 @PathVariable int itemId,
                                 @RequestBody CommentDto commentDto) {
        log.info("Выполняется POST-запрос. Создание комментария.");
        return service.addComment(userId, itemId, commentDto);
    }
}
