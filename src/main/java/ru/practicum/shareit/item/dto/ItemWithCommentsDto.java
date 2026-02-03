package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ItemWithCommentsDto(
        Long id,
        String name,
        String description,
        Long useCount,

        @JsonProperty("available")
        Boolean isAvailable,

        BookingShort lastBooking,
        BookingShort nextBooking,

        List<CommentDto> comments
) {
    public record BookingShort(Long id, Long bookerId) {
    }
}
