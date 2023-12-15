package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOut createBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                       @RequestBody @Valid BookingDtoIn bookingDtoIn) {
        log.info("Выполняется POST-запрос. Добавление нового запроса на бронирование.");
        bookingDtoIn.setBookerId(userId);
        return bookingService.createBooking(bookingDtoIn);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut bookingConfirmation(@RequestHeader("X-Sharer-User-Id") int userId, @PathVariable int bookingId,
                                             @RequestParam Boolean approved) {
        log.info("Выполняется PATCH-запрос. Подтверждение или отклонение запроса на бронирование. ");
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut getBooking(@PathVariable int bookingId, @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Выполняется GET-запрос. Получение данных о конкретном бронировании (включая его статус).");
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping()
    public List<BookingDtoOut> getUserBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                               @RequestParam(defaultValue = "ALL") String state,
                                               @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Выполняется GET-запрос. Получение списка всех бронирований текущего пользователя.");
        return bookingService.getUserBookings(userId, state,from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                @RequestParam(defaultValue = "ALL") String state,
                                                @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Выполняется GET-запрос. Получение списка бронирований для всех вещей текущего пользователя.");
        return bookingService.getOwnerBookings(userId, state, from, size);
    }

}