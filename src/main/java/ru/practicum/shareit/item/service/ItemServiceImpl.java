package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.BookingTimeException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;


    public ItemDto createItem(int userId, ItemDto itemDto) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        User user = userOpt.get();
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        itemStorage.save(item);
        return ItemMapper.toItemDto(item);
    }


    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        Optional<Item> itemOpt = itemStorage.findById(itemId);
        if (itemOpt.isEmpty()) throw new NotFoundException("Item с id=" + itemId + " не найден");
        Item oldItem = itemOpt.get();
        Item itemToUpdate = ItemMapper.toItem(itemDto);
        if (oldItem.getOwner().getId() != userId)
            throw new NotFoundException(String.format("У предмета с id=" + itemId + " не совпадает id владельца =" + userId));
        itemToUpdate.setId(itemId);
        if (itemToUpdate.getName() == null) itemToUpdate.setName(oldItem.getName());
        if (itemToUpdate.getDescription() == null) itemToUpdate.setDescription(oldItem.getDescription());
        if (itemToUpdate.getAvailable() == null) itemToUpdate.setAvailable(oldItem.getAvailable());
        itemToUpdate.setOwner(oldItem.getOwner());
        itemToUpdate.setRequestId(oldItem.getRequestId());
        return ItemMapper.toItemDto(itemStorage.save(itemToUpdate));
    }

    public ItemDto getItemById(int itemId, int userId) {
        Optional<Item> itemOpt = itemStorage.findById(itemId);
        if (itemOpt.isEmpty()) throw new NotFoundException("Item с id=" + itemId + " не найден");
        Item item = itemOpt.get();
        Booking last = null;
        Booking next = null;
        if (item.getOwner().getId() == userId) {
            LocalDateTime now = LocalDateTime.now();
            last = bookingStorage.findFirstByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(
                    item.getId(),
                    BookingStatus.REJECTED,
                    now);
            next = bookingStorage.findFirstByItemIdAndStatusNotAndStartAfterOrderByStartAsc(
                    item.getId(),
                    BookingStatus.REJECTED,
                    now);
        }
        List<CommentDto> comms = commentStorage.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::commentToDto)
                .collect(Collectors.toList());
        return ItemMapper.toItemDtoComplex(item, last, next, comms);
    }

    public List<ItemDto> getItemsByUser(int userId) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        return itemStorage.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> {
                            LocalDateTime now = LocalDateTime.now();
                            return ItemMapper.toItemDtoComplex(
                                    item,
                                    bookingStorage.findFirstByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(
                                            item.getId(),
                                            BookingStatus.REJECTED,
                                            now
                                    ),
                                    bookingStorage.findFirstByItemIdAndStatusNotAndStartAfterOrderByStartAsc(
                                            item.getId(),
                                            BookingStatus.REJECTED,
                                            now
                                    ),
                                    commentStorage.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                                            .map(CommentMapper::commentToDto)
                                            .collect(Collectors.toList())
                            );
                        }
                )
                .collect(Collectors.toList());
    }


    public List<Item> searchItem(String text) {
        if (text.isBlank()) return new ArrayList<>();
        List<ItemDto> itemsFound = itemStorage.search(text).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        return itemsFound.stream().map(ItemMapper::toItem).collect(Collectors.toList());


    }

    @Override
    public CommentDto addComment(int userId, int itemId, CommentDto commentDto) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        User author = userOpt.get();
        Optional<Item> itemOptional = itemStorage.findById(itemId);
        if (itemOptional.isEmpty()) throw new NotFoundException("Вещь с id=" + itemId + " не найдена");
        Item item = itemOptional.get();
        if (!bookingStorage.existsByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now()))
            throw new BookingTimeException("Бронирование еще не завершилось");
        Comment comment = commentStorage.save(CommentMapper.commentFromDto(commentDto, item, author));
        return CommentMapper.commentToDto(comment);
    }
}
