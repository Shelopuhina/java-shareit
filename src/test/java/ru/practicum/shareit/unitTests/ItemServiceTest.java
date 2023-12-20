package ru.practicum.shareit.unitTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.BookingTimeException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private User user2;
    private Item item;
    private Comment comment;
    private Booking booking;
    private ItemRequest itemRequest;

    @BeforeEach
    public void beforeEach() {
        user = new User(1, "user", "user@email.com");
        user2 = new User(2, "user2", "use2r@email.com");

        item = Item.builder()
                .id(1)
                .name("item")
                .description("description")
                .available(true)
                .owner(user2)
                .build();
        comment = Comment.builder()
                .id(1)
                .text("comment text")
                .item(item)
                .author(user)
                .created(LocalDateTime.now().plusMinutes(10))
                .build();
        booking = Booking.builder()
                .id(1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();

        itemRequest = ItemRequest.builder()
                .id(1)
                .description("description")
                .requestor(new User(3, "user3", "user3@email.com"))
                .created(LocalDateTime.now().plusMinutes(10))
                .items(Collections.emptyList())
                .build();
        item.setRequest(itemRequest);
    }

    @Test
    public void shouldCreateItem() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(anyInt()))
                .thenReturn(Optional.of(itemRequest));
        Mockito
                .when(itemRepository.save(any(Item.class)))
                .then(returnsFirstArg());


        ItemDto itemDto = itemService.createItem(1, ItemMapper.toItemDtoComplexReqId(item, null, null, null, itemRequest));
        Assertions.assertAll("Проверка создания вещи: ",
                () -> assertEquals(itemDto.getId(), item.getId()),
                () -> assertEquals(itemDto.getName(), item.getName()),
                () -> assertEquals(itemDto.getDescription(), item.getDescription()),
                () -> assertEquals(itemDto.getAvailable(), item.getAvailable()),
                () -> assertEquals(itemDto.getRequestId(), item.getRequest().getId()),
                () -> assertNull(itemDto.getLastBooking()),
                () -> assertNull(itemDto.getNextBooking()),
                () -> assertNull(itemDto.getComments()));
    }

    @Test
    public void shouldNotCreateItemWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.createItem(1, ItemMapper.toItemDtoComplexReqId(item, null, null, null, itemRequest)));

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldNotCreateItemWhenItemRequestNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.createItem(1, ItemMapper.toItemDtoComplexReqId(item, null, null, null, itemRequest)));

        Assertions.assertEquals(e.getMessage(), "Запрос с id=1 не найден");
    }

    @Test
    public void shouldUpdateItem() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(itemRequestRepository.findById(anyInt()))
                .thenReturn(Optional.of(itemRequest));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(itemRepository.save(any(Item.class)))
                .then(returnsFirstArg());

        ItemDto itemDto = itemService.updateItem(1, ItemMapper.toItemDtoComplexReqId(item, null, null, null, itemRequest), 2);

        Assertions.assertAll("Проверка обновления вещи: ",
                () -> assertEquals(itemDto.getId(), item.getId()),
                () -> assertEquals(itemDto.getName(), item.getName()),
                () -> assertEquals(itemDto.getDescription(), item.getDescription()),
                () -> assertEquals(itemDto.getAvailable(), item.getAvailable()),
                () -> assertEquals(itemDto.getRequestId(), item.getRequest().getId()),
                () -> assertNull(itemDto.getLastBooking()),
                () -> assertNull(itemDto.getNextBooking()),
                () -> assertNull(itemDto.getComments()));
    }

    @Test
    public void shouldNotUpdateItemWhenItemNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(1, ItemMapper.toItemDtoComplexReqId(item, null, null, null, itemRequest), 2)
        );

        Assertions.assertEquals(e.getMessage(), "Item с id=1 не найден");
    }

    @Test
    public void shouldNotUpdateItemWhenUserNotOwner() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(anyInt()))
                .thenReturn(Optional.of(itemRequest));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(1, ItemMapper.toItemDtoComplexReqId(item, null, null, null, itemRequest), 1)
        );

        Assertions.assertEquals(e.getMessage(), "У предмета с id=1 не совпадает id владельца =1");
    }

    @Test
    public void shouldNotUpdateItemWhenItemRequestNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(itemRequestRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(1, ItemMapper.toItemDtoComplexReqId(item, null, null, null, itemRequest), 1)
        );

        Assertions.assertEquals(e.getMessage(), "Запрос с id=1 не найден");
    }

    @Test
    public void shouldGetItemById() {
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(commentRepository.findByItemIdOrderByCreatedDesc(anyInt()))
                .thenReturn(List.of(comment));

        ItemDto itemDto = itemService.getItemById(1, 1);
        Assertions.assertAll("Проверка получения вещи по айди: ",
                () -> assertEquals(itemDto.getId(), item.getId()),
                () -> assertEquals(itemDto.getName(), item.getName()),
                () -> assertEquals(itemDto.getDescription(), item.getDescription()),
                () -> assertEquals(itemDto.getAvailable(), item.getAvailable()),
                () -> assertNull(itemDto.getLastBooking()),
                () -> assertNull(itemDto.getNextBooking()),
                () -> assertEquals(itemDto.getComments().size(), 1),
                () -> assertEquals(itemDto.getComments().get(0).getId(), comment.getId()));
    }

    @Test
    public void shouldNotGetItemByIdWhenItemNotFound() {
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(1, 1)
        );

        Assertions.assertEquals(e.getMessage(), "Item с id=1 не найден");
    }

    @Test
    public void shouldGetItemsByUserId() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findAllByOwnerIdOrderByIdAsc(anyInt(), any(Pageable.class)))
                .thenReturn(List.of(item));


        List<ItemDto> items = itemService.getItemsByUser(1, 0, 5);
        ItemDto itemDto = items.get(0);
        Assertions.assertAll("Проверка получения вещей по айди пользователя: ",
                () -> assertEquals(items.size(), 1),
                () -> assertEquals(itemDto.getId(), item.getId()),
                () -> assertEquals(itemDto.getName(), item.getName()),
                () -> assertEquals(itemDto.getDescription(), item.getDescription()),
                () -> assertEquals(itemDto.getAvailable(), item.getAvailable()),
                () -> assertNull(itemDto.getLastBooking()),
                () -> assertNull(itemDto.getNextBooking()));
    }

    @Test
    public void shouldNotGetItemsByUserIdWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getItemsByUser(1, 0, 5)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }


    @Test
    public void shouldSearchItems() {
        Mockito
                .when(itemRepository.search(any(String.class), any(Pageable.class)))
                .thenReturn(List.of(item));

        List<Item> items = itemService.searchItem("text", 0, 5);
        Item itemDto = items.get(0);
        Assertions.assertAll("Проверка поиска вещей : ",
                () -> assertEquals(items.size(), 1),
                () -> assertEquals(itemDto.getId(), item.getId()),
                () -> assertEquals(itemDto.getName(), item.getName()),
                () -> assertEquals(itemDto.getDescription(), item.getDescription()),
                () -> assertEquals(itemDto.getAvailable(), item.getAvailable()));
    }

    @Test
    public void shouldNotSearchItemsWhenBlankText() {
        List<Item> items = itemService.searchItem("", 0, 5);

        Assertions.assertEquals(0, (items.size()));
    }

    @Test
    public void shouldAddComment() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(
                        bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                                anyInt(),
                                anyInt(),
                                any(LocalDateTime.class)
                        )
                )
                .thenReturn(true);
        Mockito
                .when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        CommentDto commentDtoOut = itemService.addComment(
                1,
                1,
                CommentMapper.commentToDto(comment));
        Assertions.assertAll("Проверка добавления комментария : ",
                () -> assertEquals(commentDtoOut.getId(), comment.getId()),
                () -> assertEquals(commentDtoOut.getText(), comment.getText()),
                () -> assertEquals(commentDtoOut.getAuthorName(), comment.getAuthor().getName()),
                () -> assertEquals(commentDtoOut.getCreated(), comment.getCreated()));
    }

    @Test
    public void shouldNotAddCommentWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.addComment(1, 1, CommentMapper.commentToDto(comment)));

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldNotAddCommentWhenItemNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.addComment(1, 1, CommentMapper.commentToDto(comment)));

        Assertions.assertEquals(e.getMessage(), "Вещь с id=1 не найдена");
    }

    @Test
    public void shouldNotAddCommentWhenBookingNotEnded() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(
                        bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                                anyInt(),
                                anyInt(),
                                any(LocalDateTime.class)
                        )
                )
                .thenReturn(false);

        BookingTimeException e = Assertions.assertThrows(
                BookingTimeException.class,
                () -> itemService.addComment(1, 1, CommentMapper.commentToDto(comment)));

        Assertions.assertEquals(e.getMessage(), "Бронирование еще не завершилось");
    }
}