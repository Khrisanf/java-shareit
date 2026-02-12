package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;
import java.util.List;

public record ItemRequestDto(
        Long id,
        String description,
        LocalDateTime created,
        List<ItemShortDto> items
) {
}
