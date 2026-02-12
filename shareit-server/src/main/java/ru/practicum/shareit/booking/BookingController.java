package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.validate.OnCreate;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader("X-Sharer-User-Id") long bookerId,
            @RequestBody @Validated(OnCreate.class) BookingRequestDto bookingRequestDto) {
        Booking booking = bookingMapper.toEntity(bookingRequestDto);
        Booking created = bookingService.createBooking(booking, bookingRequestDto.itemId(), bookerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingMapper.toResponseDto(created));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> approveOrReject(
            @PathVariable("bookingId") long bookingId,
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestParam boolean approved) {
        Booking updated = bookingService.setApprovedOrRejected(bookingId, ownerId, approved);
        return ResponseEntity.ok(bookingMapper.toResponseDto(updated));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long bookingId
    ) {
        Booking booking = bookingService.getBooking(bookingId, userId);
        return ResponseEntity.ok(bookingMapper.toResponseDto(booking));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "ALL") BookingState state
    ) {
        List<BookingResponseDto> result = bookingService.getUserBookings(userId, state).stream()
                .map(bookingMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "ALL") BookingState state
    ) {
        List<BookingResponseDto> result = bookingService.getOwnerBookings(userId, state).stream()
                .map(bookingMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(result);
    }
}
