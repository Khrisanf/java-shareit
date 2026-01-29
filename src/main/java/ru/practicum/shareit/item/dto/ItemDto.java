package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemDto(
        Long id,
        String name,
        String description,
        long useCount,

        @JsonProperty("available")
        Boolean isAvailable
) {}
