package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import ru.practicum.shareit.HeaderNames;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

public interface BookingApi {

    @RequestMapping(method = RequestMethod.POST, value = "/bookings")
    ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader(HeaderNames.USER_ID) long bookerId,
            @RequestBody BookingRequestDto bookingRequestDto
    );

    @RequestMapping(method = RequestMethod.PATCH, value = "/bookings/{bookingId}")
    ResponseEntity<BookingResponseDto> approveOrReject(
            @PathVariable("bookingId") long bookingId,
            @RequestHeader(HeaderNames.USER_ID) long ownerId,
            @RequestParam("approved") boolean approved
    );

    @RequestMapping(method = RequestMethod.GET, value = "/bookings/{bookingId}")
    ResponseEntity<BookingResponseDto> getBooking(
            @RequestHeader(HeaderNames.USER_ID) long userId,
            @PathVariable("bookingId") long bookingId
    );

    @RequestMapping(method = RequestMethod.GET, value = "/bookings")
    ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @RequestHeader(HeaderNames.USER_ID) long userId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState state
    );

    @RequestMapping(method = RequestMethod.GET, value = "/bookings/owner")
    ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestHeader(HeaderNames.USER_ID) long userId,
            @RequestParam(name = "state", defaultValue = "ALL") BookingState state
    );
}
