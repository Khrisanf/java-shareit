package ru.practicum.shareit.item.comment.dto;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        String text,
        String authorName,
        LocalDateTime created
) {
}
