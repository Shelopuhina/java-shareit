package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
public class ItemRequestDto {
    private final Integer id;
    @NotBlank(message = "Описание запроса не может быть пустым")
    private final String description;
    private final LocalDateTime created;
    private List<ItemDto> items;
}
