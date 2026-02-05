package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public record ItemDetailsDto(
        Item item,
        Booking lastBooking,
        Booking nextBooking,
        List<Comment> comments
) {
}
