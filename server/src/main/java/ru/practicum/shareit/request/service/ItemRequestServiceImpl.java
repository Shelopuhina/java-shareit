package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.InvalidDataException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service

public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto addItemRequest(int userId, ItemRequestDto itemRequestDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.fromDto(itemRequestDto, userOpt.get()));
        return ItemRequestMapper.toDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getItemRequestsByUserId(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        List<ItemRequestDto> itReqs = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId).stream()
                .peek(itemRequest -> itemRequest.setItems(itemRepository.findByRequestId(itemRequest.getId())))
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
        for (ItemRequestDto itReq : itReqs) {
            List<ItemDto> itD = itReq.getItems().stream().peek(itemDto -> itemDto.setRequestId(itReq.getId())).collect(Collectors.toList());
            itReq.setItems(itD);
        }
        return itReqs;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(int userId, int from, int size) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        if (from < 0 || size < 1) {
            throw new InvalidDataException("size и from поля должны соответсвовать значениям.");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> allItemRequest = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);
        List<ItemRequestDto> newItemReqDto = allItemRequest.stream().map(ItemRequestMapper::toDto).collect(Collectors.toList());
        for (ItemRequestDto itReq : newItemReqDto) {
            List<ItemDto> itD = itReq.getItems().stream().peek(itemDto -> itemDto.setRequestId(itReq.getId())).collect(Collectors.toList());
            itReq.setItems(itD);
        }
        return newItemReqDto;
    }

    @Override
    public ItemRequestDto getItemRequestById(int userId, int requestId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        Optional<ItemRequest> itemRequestOptional = itemRequestRepository.findById(requestId);
        if (itemRequestOptional.isEmpty()) throw new NotFoundException("Запрос с id=" + requestId + " не найден");
        ItemRequestDto itemRequest = ItemRequestMapper.toDto(itemRequestOptional.get());
        List<ItemDto> itD = itemRequest.getItems().stream().peek(itemDto -> itemDto.setRequestId(itemRequest.getId())).collect(Collectors.toList());
        itemRequest.setItems(itD);
        return itemRequest;
    }
}
