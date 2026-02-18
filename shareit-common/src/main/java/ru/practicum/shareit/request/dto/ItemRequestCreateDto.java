package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import ru.practicum.shareit.validate.OnCreate;

public record ItemRequestCreateDto(
        @NotBlank(groups = OnCreate.class)
        String description
) {
}
