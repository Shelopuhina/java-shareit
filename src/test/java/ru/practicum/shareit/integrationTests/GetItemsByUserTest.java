package ru.practicum.shareit.integrationTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GetItemsByUserTest {

    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    public void shouldGetItemsByUserId() {
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
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();
        int userId = user.getId();
        itemService.createItem(userId, itemDto);
        List<ItemDto> items = itemService.getItemsByUser(userId, 0, 5);
        ItemDto itemDtoOutgoing = items.get(0);
        assertThat(items.size(), equalTo(1));
        assertThat(itemDtoOutgoing.getId(), notNullValue());
        assertThat(itemDtoOutgoing.getName(), equalTo(itemDto.getName()));
        assertThat(itemDtoOutgoing.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemDtoOutgoing.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(itemDtoOutgoing.getComments(), equalTo(Collections.emptyList()));
        assertThat(itemDtoOutgoing.getRequestId(), nullValue());
        assertThat(itemDtoOutgoing.getLastBooking(), nullValue());
        assertThat(itemDtoOutgoing.getNextBooking(), nullValue());
    }
}
