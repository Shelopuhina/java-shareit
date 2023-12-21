package ru.practicum.shareit.integrationTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GetItemRequestsByUserIdTest {
    private final EntityManager em;
    private final ItemRequestService itemRequestService;
    private final UserService userService;

    @Test
    public void shouldGetItemRequestsByUserId() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("description")
                .created(LocalDateTime.now())
                .build();

        UserDto userDto = UserDto.builder()
                .name("user")
                .email("user@email.com")
                .build();
        userService.createUser(userDto);
        TypedQuery<User> query = em.createQuery("select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();
        int userId = user.getId();
        itemRequestService.addItemRequest(userId, itemRequestDto);
        List<ItemRequestDto> itemRequests = itemRequestService.getItemRequestsByUserId(userId);
        ItemRequestDto itemRequestDtoOutgoing = itemRequests.get(0);

        assertThat(1, equalTo(itemRequests.size()));
        assertThat(itemRequestDtoOutgoing.getId(), notNullValue());
        assertThat(itemRequestDtoOutgoing.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequestDtoOutgoing.getItems(), equalTo(Collections.emptyList()));
    }
}