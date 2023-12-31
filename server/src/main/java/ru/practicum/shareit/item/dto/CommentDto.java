package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private final Integer id;
    @NotBlank
    private final String text;
    private final String authorName;
    private final LocalDateTime created;
}
