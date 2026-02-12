package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

@RestController
@RequiredArgsConstructor
public class BookingController implements BookingApi {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @Override
    public ResponseEntity<BookingResponseDto> createBooking(long bookerId, BookingRequestDto bookingRequestDto) {
        Booking booking = bookingMapper.toEntity(bookingRequestDto);
        Booking created = bookingService.createBooking(booking, bookingRequestDto.itemId(), bookerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingMapper.toResponseDto(created));
    }

    @Override
    public ResponseEntity<BookingResponseDto> approveOrReject(long bookingId, long ownerId, boolean approved) {
        Booking updated = bookingService.setApprovedOrRejected(bookingId, ownerId, approved);
        return ResponseEntity.ok(bookingMapper.toResponseDto(updated));
    }

    @Override
    public ResponseEntity<BookingResponseDto> getBooking(long userId, long bookingId) {
        Booking booking = bookingService.getBooking(bookingId, userId);
        return ResponseEntity.ok(bookingMapper.toResponseDto(booking));
    }

    @Override
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(long userId, BookingState state) {
        List<BookingResponseDto> result = bookingService.getUserBookings(userId, state).stream()
                .map(bookingMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(long userId, BookingState state) {
        List<BookingResponseDto> result = bookingService.getOwnerBookings(userId, state).stream()
                .map(bookingMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(result);
    }
}
