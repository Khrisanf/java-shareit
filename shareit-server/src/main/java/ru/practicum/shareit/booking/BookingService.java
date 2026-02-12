package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Booking createBooking(Booking booking, Long itemId, Long bookerId) {
        Item item = getItemOrThrow(itemId);
        User booker = getUserOrThrow(bookerId);

        validateBookingCreation(item, bookerId, booking);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking setApprovedOrRejected(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        assertIsOwner(booking, ownerId);
        validateStatusIsWaiting(booking);

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBooking(long bookingId, long userId) {
        checkUserExists(userId);
        Booking booking = getBookingOrThrow(bookingId);

        assertIsOwnerOrBooker(booking, userId);

        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(long userId, BookingState state) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartTimeBookingDesc(userId);
            case CURRENT -> bookingRepository
                    .findByBookerIdAndStartTimeBookingLessThanEqualAndEndTimeBookingGreaterThanEqualOrderByStartTimeBookingDesc(
                            userId, now, now);
            case PAST -> bookingRepository
                    .findByBookerIdAndEndTimeBookingLessThanOrderByStartTimeBookingDesc(userId, now);
            case FUTURE -> bookingRepository
                    .findByBookerIdAndStartTimeBookingGreaterThanOrderByStartTimeBookingDesc(userId, now);
            case WAITING -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartTimeBookingDesc(userId, Status.WAITING);
            case REJECTED -> bookingRepository
                    .findByBookerIdAndStatusOrderByStartTimeBookingDesc(userId, Status.REJECTED);
        };
    }


    @Transactional(readOnly = true)
    public List<Booking> getOwnerBookings(long userId, BookingState state) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartTimeBookingDesc(userId);
            case CURRENT -> bookingRepository
                    .findByItemOwnerIdAndStartTimeBookingLessThanEqualAndEndTimeBookingGreaterThanEqualOrderByStartTimeBookingDesc(
                            userId, now, now);
            case PAST -> bookingRepository
                    .findByItemOwnerIdAndEndTimeBookingLessThanOrderByStartTimeBookingDesc(userId, now);
            case FUTURE -> bookingRepository
                    .findByItemOwnerIdAndStartTimeBookingGreaterThanOrderByStartTimeBookingDesc(userId, now);
            case WAITING -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartTimeBookingDesc(userId, Status.WAITING);
            case REJECTED -> bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartTimeBookingDesc(userId, Status.REJECTED);
        };
    }


    // --- Helpers ---

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("ItemResponseDto not found: " + itemId));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private void validateBookingCreation(Item item, Long bookerId, Booking booking) {
        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Owner cannot book own item");
        }
        if (!item.getIsAvailable()) {
            throw new ValidationException("ItemResponseDto is unavailable");
        }
        if (booking.getStartTimeBooking() == null || booking.getEndTimeBooking() == null) {
            throw new ValidationException("Start/end must be provided");
        }
        if (!booking.getStartTimeBooking().isBefore(booking.getEndTimeBooking())) {
            throw new ValidationException("Start must be before end");
        }
    }

    private void assertIsOwner(Booking booking, Long userId) {
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only item owner can perform this action");
        }
    }

    private void assertIsOwnerOrBooker(Booking booking, Long userId) {
        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();
        if (userId != ownerId && userId != bookerId) {
            throw new NotFoundException("Booking not accessable for user: " + userId);
        }
    }

    private void validateStatusIsWaiting(Booking booking) {
        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Booking status must be WAITING but was: " + booking.getStatus());
        }
    }
}