package ru.practicum.shareit.itemRequest.dto;


import lombok.Builder;
import lombok.Data;
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
    @NotBlank
    private final String description;
    private final LocalDateTime created;
    private List<ItemDto> items;
}
