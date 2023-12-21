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
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User user2;

    private Item item;
    private BookingDtoIn bookingDtoIn;

    @BeforeEach
    public void setUp() {
        user = new User(1, "user", "user@email.com");
        user2 = new User(2, "user2", "use2r@email.com");

        item = Item.builder()
                .id(1)
                .name("item")
                .description("description")
                .available(true)
                .owner(user2)
                .request(null)
                .build();

        bookingDtoIn = BookingDtoIn.builder()
                .id(1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1)
                .bookerId(1)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    public void shouldCreateBooking() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(bookingRepository.save(any(Booking.class)))
                .then(returnsFirstArg());
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoIn);

        Assertions.assertAll("Проверка добавления бронирования: ",
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));

    }


    @Test
    public void shouldNotCreateBookingWhenItemNotFound() {
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoIn)
        );
        Assertions.assertEquals(e.getMessage(), ("Вещь с id=1 не найдена"));
    }

    @Test
    public void shouldNotCreateBookingByOwner() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));

        item = Item.builder()
                .id(1)
                .name("item")
                .description("description")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoIn)
        );

        Assertions.assertEquals(e.getMessage(), "Вещь может забронить любой пользователь, кроме владельца вещи.");
    }

    @Test
    public void shouldNotCreateBookingWhenItemUnavailable() {
        item = Item.builder()
                .id(1)
                .name("item")
                .description("description")
                .available(false)
                .owner(new User(3, "userr", "userr@email.com"))
                .request(null)
                .build();
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));

        UnavailableItemException e = Assertions.assertThrows(
                UnavailableItemException.class,
                () -> bookingService.createBooking(bookingDtoIn)
        );

        Assertions.assertEquals(e.getMessage(), "На данный момент вещь с id=1 недоступна для бронирования.");
    }

    @Test
    public void shouldNotCreateBookingWhenEndBeforeStart() {
        Mockito
                .when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        bookingDtoIn = BookingDtoIn.builder()
                .id(1)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1)
                .bookerId(1)
                .build();
        BookingTimeException e = Assertions.assertThrows(
                BookingTimeException.class,
                () -> bookingService.createBooking(bookingDtoIn)
        );

        Assertions.assertEquals(e.getMessage(), "Окончаниме бронирование может быть только после даты старта бронирования.");
    }

    @Test
    public void shouldApproveBooking() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(bookingRepository.findByIdAndItemOwnerId(anyInt(), anyInt()))
                .thenReturn(Optional.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        Mockito
                .when(bookingRepository.save(any(Booking.class)))
                .then(returnsFirstArg());
        BookingDtoOut bookingDtoOut = bookingService.approveBooking(2, 1, true);

        Assertions.assertAll("Проверка подтверждения бронирования: ",
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), BookingStatus.APPROVED));
    }

    @Test
    public void shouldRejectBooking() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(bookingRepository.findByIdAndItemOwnerId(anyInt(), anyInt()))
                .thenReturn(Optional.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));
        Mockito
                .when(bookingRepository.save(any(Booking.class)))
                .then(returnsFirstArg());

        BookingDtoOut bookingDtoOut = bookingService.approveBooking(2, 1, false);

        Assertions.assertAll("Проверка отклонения бронирования: ",
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), BookingStatus.REJECTED));
    }

    @Test
    public void shouldNotApproveBookingWhenBookingNotFound() {

        Mockito
                .when(bookingRepository.findByIdAndItemOwnerId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.approveBooking(1, 1, true)
        );

        Assertions.assertEquals(e.getMessage(), "Бронирование с id=1 не найдено");
    }

    @Test
    public void shouldNotApproveBookingWhenBookingStatusNotWaiting() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user2));
        BookingDtoIn bookingDtoIn1 = BookingDtoIn.builder()
                .id(1)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1)
                .bookerId(1)
                .status(BookingStatus.CANCELED)
                .build();
        Mockito
                .when(bookingRepository.findByIdAndItemOwnerId(anyInt(), anyInt()))
                .thenReturn(Optional.of(BookingMapper.dtoInToBooking(bookingDtoIn1, user, item)));

        BookingStatusException e = Assertions.assertThrows(
                BookingStatusException.class,
                () -> bookingService.approveBooking(2, 1, true)
        );

        Assertions.assertEquals(e.getMessage(), "Неверный статус бронирования ");
    }

    @Test
    public void shouldGetBookingById() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        BookingDtoOut bookingDtoOut = bookingService.getBookingById(1, 1);
        Assertions.assertAll("Проверка поолучения бронирования по id: ",
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));
    }

    @Test
    public void shouldNotGetBookingByIdWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingById(1, 1)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldNotGetBookingByIdWhenBookingNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingById(1, 1)
        );

        Assertions.assertEquals(e.getMessage(), "Бронирование с id=1 не найдено");
    }

    @Test
    public void shouldNotGetBookingByIdWhenUserNotOwnerOrBooker() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingById(1, 3)
        );

        Assertions.assertEquals(e.getMessage(), "Получить данные о бронировании может только автор брони или владелец вещи.");
    }

    @Test
    public void shouldGetAllUserBookings() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(bookingRepository.findByBookerIdOrderByStartDesc(
                        anyInt(), any(Pageable.class))
                )
                .thenReturn(List.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        List<BookingDtoOut> bookings = bookingService.getUserBookings(1, "ALL", 0, 5);
        BookingDtoOut bookingDtoOut = bookings.get(0);
        Assertions.assertAll("Проверка получения ALL бронирований: ",
                () -> assertEquals(bookings.size(), 1),
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));
    }

    @Test
    public void shouldGetByStatusWaitingUserBookings() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        anyInt(),
                        any(BookingStatus.class),
                        any(Pageable.class))
                )
                .thenReturn(List.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        List<BookingDtoOut> bookings = bookingService.getUserBookings(1, "WAITING", 0, 5);
        BookingDtoOut bookingDtoOut = bookings.get(0);

        Assertions.assertAll("Проверка получения WAITING бронирований: ",
                () -> assertEquals(bookings.size(), 1),
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));
    }

    @Test
    public void shouldGetByStatusFutureUserBookings() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                        anyInt(),
                        any(LocalDateTime.class),
                        any(Pageable.class))
                )
                .thenReturn(List.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        List<BookingDtoOut> bookings = bookingService.getUserBookings(1, "FUTURE", 0, 5);
        BookingDtoOut bookingDtoOut = bookings.get(0);

        Assertions.assertAll("Проверка получения FUTURE бронирований: ",
                () -> assertEquals(bookings.size(), 1),
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));
    }

    @Test
    public void shouldNotGetUserBookingsWhenUnknownState() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        BookingStateException e = Assertions.assertThrows(
                BookingStateException.class,
                () -> bookingService.getUserBookings(1, "WRONG", 0, 5)
        );

        Assertions.assertEquals(e.getMessage(), "Unknown state: WRONG");
    }

    @Test
    public void shouldNotGetUserBookingsWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getUserBookings(1, "ALL", 0, 5)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldGetAllOwnerBookings() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(bookingRepository.findByItemOwnerIdOrderByStartDesc(
                        anyInt(),
                        any(Pageable.class))
                )
                .thenReturn(List.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        List<BookingDtoOut> bookings = bookingService.getOwnerBookings(2, "ALL", 0, 5);
        BookingDtoOut bookingDtoOut = bookings.get(0);

        Assertions.assertAll("Проверка получения ALL OWNER бронирований: ",
                () -> assertEquals(bookings.size(), 1),
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));
    }

    @Test
    public void shouldGetWaitingOwnerBookings() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        anyInt(),
                        any(BookingStatus.class),
                        any(Pageable.class))
                )
                .thenReturn(List.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        List<BookingDtoOut> bookings = bookingService.getOwnerBookings(1, "WAITING", 0, 5);
        BookingDtoOut bookingDtoOut = bookings.get(0);
        Assertions.assertAll("Проверка получения WAITING OWNER бронирований: ",
                () -> assertEquals(bookings.size(), 1),
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));
    }

    @Test
    public void shouldGetFutureOwnerBookings() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(
                        bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(
                                anyInt(),
                                any(LocalDateTime.class),
                                any(Pageable.class))
                )
                .thenReturn(List.of(BookingMapper.dtoInToBooking(bookingDtoIn, user, item)));

        List<BookingDtoOut> bookings = bookingService.getOwnerBookings(1, "FUTURE", 0, 5);
        BookingDtoOut bookingDtoOut = bookings.get(0);
        Assertions.assertAll("Проверка получения FUTURE OWNER бронирований: ",
                () -> assertEquals(bookings.size(), 1),
                () -> assertEquals(bookingDtoOut.getId(), bookingDtoIn.getId()),
                () -> assertEquals(bookingDtoOut.getStart(), bookingDtoIn.getStart()),
                () -> assertEquals(bookingDtoOut.getEnd(), bookingDtoIn.getEnd()),
                () -> assertEquals(bookingDtoOut.getItem().getId(), bookingDtoIn.getItemId()),
                () -> assertEquals(bookingDtoOut.getBooker().getId(), bookingDtoIn.getBookerId()),
                () -> assertEquals(bookingDtoOut.getStatus(), bookingDtoIn.getStatus()));
    }

    @Test
    public void shouldNotGetOwnerBookingsWhenUnknownState() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        BookingStateException e = Assertions.assertThrows(
                BookingStateException.class,
                () -> bookingService.getOwnerBookings(1, "WRONG", 0, 5)
        );

        Assertions.assertEquals(e.getMessage(), "Unknown state: WRONG");
    }

    @Test
    public void shouldNotGetOwnerBookingsWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getOwnerBookings(1, "ALL", 0, 5)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }
}
