package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.user.validate.OnCreate;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                 @RequestBody @Validated(OnCreate.class) BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStartBooking(bookingDto.start());
        booking.setEndBooking(bookingDto.end());

        Booking created = bookingService.createBooking(booking, bookingDto.itemId(), bookerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Booking> approveOrReject(@PathVariable("bookingId") long bookingId,
                                                   @RequestHeader("X-Sharer-User-Id") long ownerId,
                                                   @RequestParam boolean approved) {
        Booking updated = bookingService.setApprovedOrRejected(bookingId, ownerId, approved);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable long bookingId) {
        Booking booking = bookingService.getBooking(userId, bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @RequestParam(defaultValue = "ALL") BookingState state) {
        List<Booking> bookings = bookingService.getUserBookings(userId, state);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<Booking>> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                          @RequestParam(defaultValue = "ALL") BookingState state) {
        List<Booking> bookings = bookingService.getOwnerBookings(userId, state);
        return ResponseEntity.ok(bookings);
    }
}
