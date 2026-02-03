package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public record BookingResponseDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        String status,
        Booker booker,
        Item item
) {
    public record Booker(Long id) {}
    public record Item(Long id, String name) {}
}

