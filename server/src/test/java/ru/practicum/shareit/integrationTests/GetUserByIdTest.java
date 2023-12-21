package ru.practicum.shareit.integrationTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import static org.hamcrest.Matchers.equalTo;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GetUserByIdTest {
    private final EntityManager em;
    private final UserService userService;

    @Test
    public void shouldGetUserById() {
        UserDto userDto = UserDto.builder()
                .name("user")
                .email("user@email.com")
                .build();
        userService.createUser(userDto);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();
        int userId = user.getId();
        UserDto user2 = userService.getUserById(userId);
        assertThat(user.getId(), equalTo(user.getId()));
        assertThat(user.getEmail(), equalTo(user2.getEmail()));
        assertThat(user.getName(), equalTo(user2.getName()));
    }
}
