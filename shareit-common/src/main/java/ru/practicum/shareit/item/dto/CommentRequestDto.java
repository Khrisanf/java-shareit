package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDto(
        @NotBlank(message = "comment text must not be blank")
        String text
) {
}