package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
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

        booking.setBookingId(bookerId);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking setApprovedOrRejected(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        Long realOwerId = booking.getItem().getOwner().getId();
        if (!realOwerId.equals(ownerId)) {
            throw new NotFoundException("Only item owner can approve/reject this booking");
        }
        if (booking.getBooker().getId().equals(realOwerId)) {
            throw new ValidationException("Owner cannot book their own item");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Booking is not waiting status");
        }
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBooking(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        long bookerId = booking.getBooker().getId();

        long ownerId = booking.getItem().getOwner().getId();

        if (userId != ownerId && userId != bookerId) {
            throw new ValidationException("Booking not found");
        }

        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findAllForBooker(userId, state.name(), now);
    }

    @Transactional(readOnly = true)
    public List<Booking> getOwnerBookings(long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findAllForOwner(userId, state.name(), now);
    }
}
