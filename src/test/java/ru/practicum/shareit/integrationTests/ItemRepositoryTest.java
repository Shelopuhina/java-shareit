package ru.practicum.shareit.integrationTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Test
    public void shouldFindByText() {
        User user = User.builder()
                .name("user")
                .email("user@email.com")
                .build();

        userRepository.save(user);
        Item item = Item.builder()
                .name("item")
                .available(true)
                .description("description")
                .build();

        itemRepository.save(item);
        List<Item> items = itemRepository.search("item", PageRequest.of(0, 5));
        assertThat(items.size(), equalTo(1));
        assertThat(items.get(0), equalTo(item));
    }
}
