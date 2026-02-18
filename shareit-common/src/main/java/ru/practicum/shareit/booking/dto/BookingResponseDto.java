package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.BookerDto;

import java.time.LocalDateTime;

public record BookingResponseDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        String status,
        BookerDto booker,
        ItemResponseDto item
) {
}

