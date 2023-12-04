package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.item.model.Item;

import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.dto.BookingMapper.bookingToDtoOut;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    @Transactional//прописать трансы
    public BookingDtoOut createBooking(BookingDtoIn bookingDtoIn) {
        int userId = bookingDtoIn.getBookerId();
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        User user = userOpt.get();
        int itemId = bookingDtoIn.getItemId();
        Optional<Item> itemOpt = itemStorage.findById(itemId);
        if (itemOpt.isEmpty()) throw new NotFoundException("Вещь с id=" + itemId + " не найдена");
        Item item = itemOpt.get();
        if (item.getOwner().getId() == userId)
            throw new NotFoundException("Вещь может забронить любой пользователь, кроме владельца вещи.");
        if (!item.getAvailable())
            throw new UnavailableItemException("На данный момент вещь с id=" + itemId + " недоступна для бронирования.");
        if (!bookingDtoIn.getEnd().isAfter(bookingDtoIn.getStart()))
            throw new BookingTimeException("Окончаниме бронирование может быть только после даты старта бронирования.");
        if (bookingDtoIn.getStart().isEqual(bookingDtoIn.getEnd()))
            throw new BookingTimeException("Окончаниме бронирование не может совпадать с датой старта бронирования.");

        Booking booking = BookingMapper.dtoInToBooking(bookingDtoIn, user, item);

        booking.setItem(item);
        bookingStorage.save(booking);
        log.info("Добавлено бронирование {}", booking);
        return bookingToDtoOut(booking);
    }

    @Override
    @Transactional
    public BookingDtoOut approveBooking(int userId, int bookingId, Boolean approved) {
        Optional<Booking> bookingOptional = bookingStorage.findByIdAndItemOwnerId(bookingId, userId);
        if (bookingOptional.isEmpty())
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        Booking booking = bookingOptional.get();
        Optional<User> userOptOwner = userStorage.findById(booking.getItem().getOwner().getId());
        if (userOptOwner.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        User owner = userOptOwner.get();
        if (!(owner.getId() == userId))
            throw new BookingErrorException("Статус вещи может менять только воладелец вещи.");
        if (!booking.getStatus().equals(BookingStatus.WAITING))
            throw new BookingStatusException("Неверный статус бронирования ");
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingToDtoOut(bookingStorage.save(booking));
    }


    @Override
    @Transactional
    public BookingDtoOut getBookingById(int bookingId, int userId) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        Optional<Booking> bookingOpt = bookingStorage.findById(bookingId);
        if (bookingOpt.isEmpty())
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        Booking booking = bookingOpt.get();
        if (!(userId == (booking.getBooker().getId()) || userId == (booking.getItem().getOwner().getId())))
            throw new NotFoundException("Получить данные о бронировании может только автор брони или владелец вещи.");
        return bookingToDtoOut(booking);
    }

    @Override
    @Transactional
    public List<BookingDtoOut> getUserBookings(int userId, String bookingState) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        BookingState state;
        try {
            state = BookingState.valueOf(bookingState);
        } catch (IllegalArgumentException exc) {
            throw new BookingStateException("Unknown state: " + bookingState);
        }
        List<Booking> bookings;
        switch (state) {
            case PAST:
                bookings = bookingStorage.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingStorage.findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case CURRENT:
                LocalDateTime now = LocalDateTime.now();
                bookings = bookingStorage.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
                break;
            case WAITING:
                bookings = bookingStorage.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingStorage.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingStorage.findByBookerIdOrderByStartDesc(userId);
        }

        return bookings.stream()
                .map(BookingMapper::bookingToDtoOut)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BookingDtoOut> getOwnerBookings(int userId, String bookingState) {
        Optional<User> userOpt = userStorage.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        BookingState state;
        try {
            state = BookingState.valueOf(bookingState);
        } catch (IllegalArgumentException exc) {
            throw new BookingStateException("Unknown state: " + bookingState);
        }
        List<Booking> bookings;

        switch (state) {
            case FUTURE:
                bookings = bookingStorage.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case CURRENT:
                LocalDateTime now = LocalDateTime.now();
                bookings = bookingStorage.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
                break;
            case PAST:
                bookings = bookingStorage.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case WAITING:
                bookings = bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingStorage.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingStorage.findByItemOwnerIdOrderByStartDesc(userId);
        }

        return bookings.stream()
                .map(BookingMapper::bookingToDtoOut)
                .collect(Collectors.toList());

    }
}
