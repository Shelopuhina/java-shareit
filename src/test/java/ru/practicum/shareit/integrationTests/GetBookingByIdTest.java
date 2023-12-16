package ru.practicum.shareit.integrationTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GetBookingByIdTest {
    private final EntityManager em;
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    public void shouldGetBookingsById() {
        ItemDto itemDto = ItemDto.builder()
                .name("item")
                .available(true)
                .description("description")
                .build();

        UserDto userDto = UserDto.builder()
                .name("user")
                .email("user@email.com")
                .build();

        userService.createUser(userDto);
        TypedQuery<User> queryUser = em.createQuery("select u from User u where u.email = :email", User.class);
        User user1 = queryUser.setParameter("email", userDto.getEmail()).getSingleResult();
        int user1Id = user1.getId();
        itemService.createItem(user1Id, itemDto);

        userDto = UserDto.builder()
                .name("user2")
                .email("use2r@email.com")
                .build();

        userService.createUser(userDto);
        queryUser = em.createQuery("select u from User u where u.email = :email", User.class);
        User user2 = queryUser.setParameter("email", userDto.getEmail()).getSingleResult();
        int user2Id = user2.getId();
        TypedQuery<Item> queryItem = em.createQuery("select i from Item i where i.name = :name", Item.class);
        Item item = queryItem.setParameter("name", itemDto.getName()).getSingleResult();
        BookingDtoIn bookingDtoIn = BookingDtoIn.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(item.getId())
                .bookerId(user2Id)
                .status(BookingStatus.WAITING)
                .build();

        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoIn);
        BookingDtoOut booking = bookingService.getBookingById(bookingDtoOut.getId(), user2Id);
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStart(), equalTo(bookingDtoIn.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDtoIn.getEnd()));
        assertThat(booking.getItem().getId(), equalTo(bookingDtoIn.getItemId()));
        assertThat(booking.getBooker().getId(), equalTo(bookingDtoIn.getBookerId()));
        assertThat(booking.getStatus(), equalTo(bookingDtoIn.getStatus()));
    }
}

