package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.BookingTimeException;
import ru.practicum.shareit.exceptions.InvalidDataException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;


    public ItemDto createItem(int userId, ItemDto itemDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        User user = userOpt.get();

        ItemRequest itemRequest = null;
        Integer requestId = itemDto.getRequestId();
        if (requestId != null) {
            Optional<ItemRequest> itemRequestOpt = itemRequestRepository.findById(requestId);
            if (itemRequestOpt.isEmpty())
                throw new NotFoundException("Запрос с id=" + requestId + " не найден");
            itemRequest = itemRequestOpt.get();
        }
        Item item = ItemMapper.toItemReq(itemDto, itemRequest);
        item.setOwner(user);
        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }


    public ItemDto updateItem(int itemId, ItemDto itemDto, int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) throw new NotFoundException("Item с id=" + itemId + " не найден");
        Item oldItem = itemOpt.get();
        ItemRequest itemRequest = null;
        Integer requestId = itemDto.getRequestId();
        if (requestId != null) {
            Optional<ItemRequest> itemRequestOpt = itemRequestRepository.findById(requestId);
            if (itemRequestOpt.isEmpty())
                throw new NotFoundException("Запрос с id=" + requestId + " не найден");
            itemRequest = itemRequestOpt.get();
        }
        Item itemToUpdate = ItemMapper.toItemReq(itemDto,itemRequest);
        if (oldItem.getOwner().getId() != userId)
            throw new NotFoundException(String.format("У предмета с id=" + itemId + " не совпадает id владельца =" + userId));
        itemToUpdate.setId(itemId);
        if (itemToUpdate.getName() == null) itemToUpdate.setName(oldItem.getName());
        if (itemToUpdate.getDescription() == null) itemToUpdate.setDescription(oldItem.getDescription());
        if (itemToUpdate.getAvailable() == null) itemToUpdate.setAvailable(oldItem.getAvailable());
        itemToUpdate.setOwner(oldItem.getOwner());


        return ItemMapper.toItemDto(itemRepository.save(itemToUpdate));
    }

    public ItemDto getItemById(int itemId, int userId) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) throw new NotFoundException("Item с id=" + itemId + " не найден");
        Item item = itemOpt.get();
        Booking last = null;
        Booking next = null;
        if (item.getOwner().getId() == userId) {
            LocalDateTime now = LocalDateTime.now();
            last = bookingRepository.findFirstByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(
                    item.getId(),
                    BookingStatus.REJECTED,
                    now);
            next = bookingRepository.findFirstByItemIdAndStatusNotAndStartAfterOrderByStartAsc(
                    item.getId(),
                    BookingStatus.REJECTED,
                    now);
        }
        List<CommentDto> comms = commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::commentToDto)
                .collect(Collectors.toList());
        return ItemMapper.toItemDtoComplex(item, last, next, comms);
    }

    public List<ItemDto> getItemsByUser(int userId, int from, int size) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId,getPageable(from,size));
        List<Comment> comms = commentRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();
        List<ItemDto> sortedItems = new ArrayList<>();
        for (Item item : items) {
            sortedItems.add(addLastAndNextBookingAndComments(item, bookings, comms));
        }
        return sortedItems;
    }


    public List<Item> searchItem(String text, int from, int size) {
        if (text.isBlank()) return new ArrayList<>();
        List<ItemDto> itemsFound = itemRepository.search(text, getPageable(from,size)).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        return itemsFound.stream().map(ItemMapper::toItem).collect(Collectors.toList());


    }

    @Override
    public CommentDto addComment(int userId, int itemId, CommentDto commentDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        User author = userOpt.get();
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) throw new NotFoundException("Вещь с id=" + itemId + " не найдена");
        Item item = itemOptional.get();
        if (!bookingRepository.existsByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now()))
            throw new BookingTimeException("Бронирование еще не завершилось");
        Comment comment = commentRepository.save(CommentMapper.commentFromDto(commentDto, item, author));
        return CommentMapper.commentToDto(comment);
    }

    private ItemDto addLastAndNextBookingAndComments(Item item, List<Booking> bookings, List<Comment> comments) {
        Booking last = bookings.stream()
                .filter(booking -> booking.getItem().getId() == item.getId())
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) && !(BookingStatus.REJECTED.equals(booking.getStatus())))
                .min(Booking::compareTo)
                .orElse(null);

        Booking next = bookings.stream()
                .filter(booking -> booking.getItem().getId() == item.getId())
                .filter((booking) -> booking.getStart().isAfter(LocalDateTime.now()) && !(BookingStatus.REJECTED.equals(booking.getStatus())))
                .max(Booking::compareTo)
                .orElse(null);
        List<CommentDto> comms = comments.stream()
                .filter(comment -> item.getId() == (comment.getItem().getId()))
                .map(CommentMapper::commentToDto)
                .collect(Collectors.toList());
        return ItemMapper.toItemDtoComplex(item, last, next, comms);
    }
    private Pageable getPageable(int from, int size) {
        if (from < 0 || size < 1) {
            throw new InvalidDataException("size и from поля должны соответсвовать значениям.");
        }
        int page = from / size;
        return PageRequest.of(page, size);
    }
}

