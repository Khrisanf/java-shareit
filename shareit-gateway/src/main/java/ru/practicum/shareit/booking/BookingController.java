package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.validate.OnCreate;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController implements BookingApi {

    private final BookingClient bookingClient;

    @Override
    public ResponseEntity<BookingResponseDto> createBooking(
            long bookerId,
            @Validated(OnCreate.class) BookingRequestDto bookingRequestDto
    ) {
        return bookingClient.createBooking(bookerId, bookingRequestDto);
    }

    @Override
    public ResponseEntity<BookingResponseDto> approveOrReject(long bookingId, long ownerId, boolean approved) {
        return bookingClient.approveOrReject(bookingId, ownerId, approved);
    }

    @Override
    public ResponseEntity<BookingResponseDto> getBooking(long userId, long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @Override
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(long userId, BookingState state) {
        return bookingClient.getUserBookings(userId, state);
    }

    @Override
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(long userId, BookingState state) {
        return bookingClient.getOwnerBookings(userId, state);
    }
}
