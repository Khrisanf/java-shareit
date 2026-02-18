package ru.practicum.shareit.booking;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "shareit-server",
        url = "${shareit-server.url}",
        contextId = "bookingClient"
)
public interface BookingClient extends BookingApi {
}
