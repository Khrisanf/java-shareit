package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingState;
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

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Owner cannot book own item");
        }

        if (!item.getIsAvailable()) {
            throw new ValidationException("Item is unavailable");
        }

        if (booking.getStartBooking() == null || booking.getEndBooking() == null) {
            throw new ValidationException("Start/end must be provided");
        }
        if (!booking.getStartBooking().isBefore(booking.getEndBooking())) {
            throw new ValidationException("Start must be before end");
        }

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking setApprovedOrRejected(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        Long realOwnerId = booking.getItem().getOwner().getId();
        if (!realOwnerId.equals(ownerId)) {
            throw new ForbiddenException("Only item owner can approve/reject this booking");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Booking is not waiting status");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBooking(long bookingId, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();

        if (userId != ownerId && userId != bookerId) {
            throw new NotFoundException("Booking not found");
        }

        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        return bookingRepository.findAllForBooker(userId, state.name(), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Booking> getOwnerBookings(long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        return bookingRepository.findAllForOwner(userId, state.name(), LocalDateTime.now());
    }
}
