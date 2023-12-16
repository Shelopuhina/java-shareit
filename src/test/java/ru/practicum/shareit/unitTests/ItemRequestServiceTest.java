package ru.practicum.shareit.unitTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    public void beforeEach() {
        user = new User(1, "user", "user@email.com");

        itemRequestDto = ItemRequestDto.builder()
                .id(1)
                .description("description")
                .items(Collections.emptyList())
                .build();
    }

    @Test
    public void shouldAddItemRequest() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.save(any(ItemRequest.class)))
                .then(returnsFirstArg());

        ItemRequestDto itemRequestDto1 = itemRequestService.addItemRequest(1, itemRequestDto);
        Assertions.assertAll("Проверка создания вещи: ",
                () -> assertEquals(itemRequestDto1.getId(), itemRequestDto.getId()),
                () -> assertEquals(itemRequestDto1.getDescription(), itemRequestDto.getDescription()),
                () -> assertEquals(itemRequestDto1.getItems(), itemRequestDto.getItems()));
    }

    @Test
    public void shouldNotAddItemRequestWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.addItemRequest(1, itemRequestDto)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldGetItemRequestsByUserId() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(anyInt()))
                .thenReturn(List.of(ItemRequestMapper.FromDto(itemRequestDto, user)));
        Mockito
                .when(itemRepository.findByRequestId(anyInt()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> itemRequests = itemRequestService.getItemRequestsByUserId(1);
        ItemRequestDto itemRequestDto1 = itemRequests.get(0);
        Assertions.assertAll("Проверка создания вещи: ",
                () -> assertEquals(itemRequests.size(), 1),
                () -> assertEquals(itemRequestDto1.getId(), itemRequestDto.getId()),
                () -> assertEquals(itemRequestDto1.getDescription(), itemRequestDto.getDescription()),
                () -> assertEquals(itemRequestDto1.getItems(), itemRequestDto.getItems()));
    }

    @Test
    public void shouldNotGetItemRequestsByUserIdWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestsByUserId(1)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldGetAllItemRequests() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(anyInt(), any(Pageable.class)))
                .thenReturn(
                        List.of(ItemRequestMapper.FromDto(itemRequestDto, user)));

        List<ItemRequestDto> itemRequests = itemRequestService.getAllItemRequests(1, 0, 5);
        ItemRequestDto itemRequestDto1 = itemRequests.get(0);
        Assertions.assertAll("Проверка создания вещи: ",
                () -> assertEquals(itemRequests.size(), 1),
                () -> assertEquals(itemRequestDto1.getId(), itemRequestDto.getId()),
                () -> assertEquals(itemRequestDto1.getDescription(), itemRequestDto.getDescription()),
                () -> assertEquals(itemRequestDto1.getItems(), itemRequestDto.getItems()));
    }

    @Test
    public void shouldNotGetAllItemRequestsWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAllItemRequests(1, 0, 5)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldGetItemRequestById() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(anyInt()))
                .thenReturn(
                        Optional.of(ItemRequestMapper.FromDto(itemRequestDto, user))
                );

        ItemRequestDto itemRequestDto1 = itemRequestService.getItemRequestById(1, 1);
        Assertions.assertAll("Проверка создания вещи: ",
                () -> assertEquals(itemRequestDto1.getId(), itemRequestDto.getId()),
                () -> assertEquals(itemRequestDto1.getDescription(), itemRequestDto.getDescription()),
                () -> assertEquals(itemRequestDto1.getItems(), itemRequestDto.getItems()));
    }

    @Test
    public void shouldNotGetItemRequestByIdWhenUserNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(1, 1)
        );

        Assertions.assertEquals(e.getMessage(), "Пользователь с id=1 не найден");
    }

    @Test
    public void shouldNotGetItemRequestByIdWhenItemRequestNotFound() {
        Mockito
                .when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        NotFoundException e = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(1, 1)
        );

        Assertions.assertEquals(e.getMessage(), "Запрос с id=1 не найден");
    }
}
