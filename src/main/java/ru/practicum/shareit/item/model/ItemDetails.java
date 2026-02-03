package ru.practicum.shareit.item.model;

import ru.practicum.shareit.booking.Booking;

import java.util.List;

public record ItemDetails(
        Item item,
        Booking lastBooking,
        Booking nextBooking,
        List<Comment> comments
) {
}
