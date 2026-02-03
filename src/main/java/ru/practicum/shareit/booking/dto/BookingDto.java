package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import ru.practicum.shareit.user.validate.OnCreate;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
public record BookingDto(
        @NotNull(groups = OnCreate.class)
        Long itemId,

        @NotNull(groups = OnCreate.class)
        @FutureOrPresent(groups = OnCreate.class)
        LocalDateTime start,

        @NotNull(groups = OnCreate.class)
        @Future(groups = OnCreate.class)
        LocalDateTime end


) {

}
