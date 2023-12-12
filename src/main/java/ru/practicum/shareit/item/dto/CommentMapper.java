package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static CommentDto commentToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();


    }

    public static Comment commentFromDto(CommentDto commentDto, Item item, User author) {
        return Comment.builder()
                .author(author)
                .item(item)
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .build();
    }
}
