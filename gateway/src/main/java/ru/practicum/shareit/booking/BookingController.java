package ru.practicum.shareit.booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.BookingDtoIn;
import ru.practicum.shareit.booking.model.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


/**
 * TODO Sprint add-bookings.
 */
@RestController
@Slf4j
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;
   @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                       @RequestBody @Valid BookingDtoIn bookingDtoIn) {
        log.info("Выполняется POST-запрос. Добавление нового запроса на бронирование.");
        //bookingDtoIn.setBookerId(userId);
        return bookingClient.createBooking(userId, bookingDtoIn);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> bookingConfirmation(@RequestHeader("X-Sharer-User-Id") int userId, @PathVariable int bookingId,
                                             @RequestParam Boolean approved) {
        log.info("Выполняется PATCH-запрос. Подтверждение или отклонение запроса на бронирование. ");
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@PathVariable int bookingId, @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Выполняется GET-запрос. Получение данных о конкретном бронировании (включая его статус).");
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {

        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object>    getOwnerBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                @RequestParam(defaultValue = "ALL") String state,
                                                @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Выполняется GET-запрос. Получение списка бронирований для всех вещей текущего пользователя.");
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

}