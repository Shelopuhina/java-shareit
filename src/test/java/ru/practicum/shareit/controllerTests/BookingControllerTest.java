package ru.practicum.shareit.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private final ItemDto itemDto = ItemDto.builder()
            .id(1)
            .name("item")
            .description("description")
            .available(true)
            .build();
    private final BookingDtoIn bookingDtoIn = BookingDtoIn.builder()
            .id(1)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .itemId(1)
            .bookerId(1)
            .status(BookingStatus.WAITING)
            .build();

    private final BookingDtoOut bookingDtoOut = BookingDtoOut.builder()
            .id(1)
            .start(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS))
            .end(LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS))
            .item(itemDto)
            .bookerId(2)
            .status(BookingStatus.WAITING)
            .build();

    @Test
    public void shouldCreateBooking() throws Exception {
        Mockito
                .when(bookingService.createBooking(any(BookingDtoIn.class)))
                .thenReturn(bookingDtoOut);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDtoOut.getId()))
                .andExpect(jsonPath("$.start").value(bookingDtoOut.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDtoOut.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingDtoOut.getStatus().toString()))
                .andExpect(jsonPath("$.item.id").value(bookingDtoOut.getItem().getId()))
                .andExpect(jsonPath("$.bookerId").value(bookingDtoOut.getBookerId()));
    }


    @Test
    public void shouldConfirmBooking() throws Exception {
        Mockito
                .when(bookingService.approveBooking(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(bookingDtoOut);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", String.valueOf(true))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDtoOut.getId()))
                .andExpect(jsonPath("$.start").value(bookingDtoOut.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDtoOut.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingDtoOut.getStatus().toString()))
                .andExpect(jsonPath("$.item.id").value(bookingDtoOut.getItem().getId()))
                .andExpect(jsonPath("$.bookerId").value(bookingDtoOut.getBookerId()));
    }

    @Test
    public void shouldGetBookingById() throws Exception {
        Mockito
                .when(bookingService.getBookingById(anyInt(), anyInt()))
                .thenReturn(bookingDtoOut);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDtoOut.getId()))
                .andExpect(jsonPath("$.start").value(bookingDtoOut.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDtoOut.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingDtoOut.getStatus().toString()))
                .andExpect(jsonPath("$.item.id").value(bookingDtoOut.getItem().getId()))
                .andExpect(jsonPath("$.bookerId").value(bookingDtoOut.getBookerId()));
    }

    @Test
    public void shouldGetUserBookings() throws Exception {
        Mockito
                .when(bookingService.getUserBookings(anyInt(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDtoOut));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(bookingDtoOut.getId()))
                .andExpect(jsonPath("$.[0].start").value(bookingDtoOut.getStart().toString()))
                .andExpect(jsonPath("$.[0].end").value(bookingDtoOut.getEnd().toString()))
                .andExpect(jsonPath("$.[0].status").value(bookingDtoOut.getStatus().toString()))
                .andExpect(jsonPath("$.[0].item.id").value(bookingDtoOut.getItem().getId()))
                .andExpect(jsonPath("$.[0].bookerId").value(bookingDtoOut.getBookerId()));
    }

    @Test
    public void shouldGetOwnerBookings() throws Exception {
        Mockito
                .when(bookingService.getOwnerBookings(anyInt(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDtoOut));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(bookingDtoOut.getId()))
                .andExpect(jsonPath("$.[0].start").value(bookingDtoOut.getStart().toString()))
                .andExpect(jsonPath("$.[0].end").value(bookingDtoOut.getEnd().toString()))
                .andExpect(jsonPath("$.[0].status").value(bookingDtoOut.getStatus().toString()))
                .andExpect(jsonPath("$.[0].item.id").value(bookingDtoOut.getItem().getId()))
                .andExpect(jsonPath("$.[0].bookerId").value(bookingDtoOut.getBookerId()));
    }
}