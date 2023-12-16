package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

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
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional//прописать трансы
    public BookingDtoOut createBooking(BookingDtoIn bookingDtoIn) {
        int itemId = bookingDtoIn.getItemId();
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) throw new NotFoundException("Вещь с id=" + itemId + " не найдена");
        Item item = itemOpt.get();
        if (!item.getAvailable())
            throw new UnavailableItemException("На данный момент вещь с id=" + itemId + " недоступна для бронирования.");
        if (!bookingDtoIn.getEnd().isAfter(bookingDtoIn.getStart()))
            throw new BookingTimeException("Окончаниме бронирование может быть только после даты старта бронирования.");
        if (bookingDtoIn.getStart().isEqual(bookingDtoIn.getEnd()))
            throw new BookingTimeException("Окончаниме бронирование не может совпадать с датой старта бронирования.");
        int userId = bookingDtoIn.getBookerId();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        User user = userOpt.get();
        if (item.getOwner().getId() == userId)
            throw new NotFoundException("Вещь может забронить любой пользователь, кроме владельца вещи.");
        Booking booking = BookingMapper.dtoInToBooking(bookingDtoIn, user, item);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        log.info("Добавлено бронирование {}", booking);
        return bookingToDtoOut(booking);
    }

    @Override
    @Transactional
    public BookingDtoOut approveBooking(int userId, int bookingId, Boolean approved) {
        Optional<Booking> bookingOptional = bookingRepository.findByIdAndItemOwnerId(bookingId, userId);
        if (bookingOptional.isEmpty())
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        Booking booking = bookingOptional.get();
        Optional<User> userOptOwner = userRepository.findById(booking.getItem().getOwner().getId());
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
        return bookingToDtoOut(bookingRepository.save(booking));
    }


    @Override
    @Transactional
    public BookingDtoOut getBookingById(int bookingId, int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty())
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        Booking booking = bookingOpt.get();
        if (!(userId == (booking.getBooker().getId()) || userId == (booking.getItem().getOwner().getId())))
            throw new NotFoundException("Получить данные о бронировании может только автор брони или владелец вещи.");
        return bookingToDtoOut(booking);
    }

    @Override
    @Transactional
    public List<BookingDtoOut> getUserBookings(int userId, String bookingState, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        if (from < 0 || size < 1) {
            throw new InvalidDataException("size и from поля должны соответсвовать значениям.");
        }
        BookingState state;
        try {
            state = BookingState.valueOf(bookingState);
        } catch (IllegalArgumentException exc) {
            throw new BookingStateException("Unknown state: " + bookingState);
        }
        List<Booking> bookings;
        switch (state) {
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), getPageable(from, size));
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), getPageable(from, size));
                break;
            case CURRENT:
                LocalDateTime now = LocalDateTime.now();
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now, getPageable(from, size));
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, getPageable(from, size));
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, getPageable(from, size));
                break;
            default:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, getPageable(from, size));
        }

        return bookings.stream()
                .map(BookingMapper::bookingToDtoOut)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BookingDtoOut> getOwnerBookings(int userId, String bookingState, int from, int size) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        if (from < 0 || size < 1) {
            throw new InvalidDataException("size и from поля должны соответсвовать значениям.");
        }
        BookingState state;
        try {
            state = BookingState.valueOf(bookingState);
        } catch (IllegalArgumentException exc) {
            throw new BookingStateException("Unknown state: " + bookingState);
        }
        List<Booking> bookings;

        switch (state) {
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(), getPageable(from, size));
                break;
            case CURRENT:
                LocalDateTime now = LocalDateTime.now();
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now, getPageable(from, size));
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(), getPageable(from, size));
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, getPageable(from, size));
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, getPageable(from, size));
                break;
            default:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, getPageable(from, size));
        }

        return bookings.stream()
                .map(BookingMapper::bookingToDtoOut)
                .collect(Collectors.toList());

    }

    private Pageable getPageable(int from, int size) {
        if (from < 0 || size < 1) {
            throw new InvalidDataException("size и from поля должны соответсвовать значениям.");
        }
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
