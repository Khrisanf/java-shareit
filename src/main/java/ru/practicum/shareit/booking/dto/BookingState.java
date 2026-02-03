package ru.practicum.shareit.booking.dto;
// правильно ли его поместить сюда?
// это перечисление служит просто фильтром для
// GET /bookings?state=

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED
}
