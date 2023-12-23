package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.BaseClient;
import ru.practicum.shareit.booking.model.BookingDtoIn;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exceptions.BookingStateException;

import java.util.Map;

@Service
@Slf4j
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(int userId, BookingDtoIn bookingDtoIn) {
        return post("", userId, bookingDtoIn);
    }

    public ResponseEntity<Object> approveBooking(int userId, int bookingId, boolean approved) {
        return patch("/" + bookingId + "?approved={approved}",
                userId,
                Map.of("approved", approved),
                null
        );
    }

    public ResponseEntity<Object> getBookingById(int bookingId, int userId) {
        return get("/" + bookingId, userId, null);
    }

    public ResponseEntity<Object> getUserBookings(int userId, String state, int from, int size) {
        try {
            BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingStateException("Unknown state: " + state);
        }
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(int userId, String state, int from, int size) {
        try {
            BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingStateException("Unknown state: " + state);
        }
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }
}