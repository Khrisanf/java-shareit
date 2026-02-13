package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingState;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIT {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.ru");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@mail.ru");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Drill");
        item.setDescription("desc");
        item.setOwner(owner);
        item.setIsAvailable(true);
        item = itemRepository.save(item);
    }

    @Test
    void createBooking_persistsAndSetsWaiting() {
        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));

        Booking created = bookingService.createBooking(booking, item.getId(), booker.getId());

        assertNotNull(created.getBookingId());
        assertEquals(Status.WAITING, created.getStatus());
        assertEquals(booker.getId(), created.getBooker().getId());
        assertEquals(item.getId(), created.getItem().getId());

        Booking fromDb = bookingRepository.findById(created.getBookingId()).orElseThrow();
        assertEquals(Status.WAITING, fromDb.getStatus());
        assertEquals(booker.getId(), fromDb.getBooker().getId());
        assertEquals(item.getId(), fromDb.getItem().getId());
    }

    @Test
    void createBooking_ownerCannotBookOwnItem_throwsNotFound() {
        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(booking, item.getId(), owner.getId()));
    }

    @Test
    void createBooking_itemUnavailable_throwsValidation() {
        item.setIsAvailable(false);
        itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(booking, item.getId(), booker.getId()));
    }

    @Test
    void approveBooking_ownerApproves_setsApproved() {
        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));
        Booking created = bookingService.createBooking(booking, item.getId(), booker.getId());

        Booking approved = bookingService.setApprovedOrRejected(created.getBookingId(), owner.getId(), true);

        assertEquals(Status.APPROVED, approved.getStatus());

        Booking fromDb = bookingRepository.findById(created.getBookingId()).orElseThrow();
        assertEquals(Status.APPROVED, fromDb.getStatus());
    }

    @Test
    void approveBooking_notOwner_throwsForbidden() {
        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));
        Booking created = bookingService.createBooking(booking, item.getId(), booker.getId());

        assertThrows(ForbiddenException.class,
                () -> bookingService.setApprovedOrRejected(created.getBookingId(), booker.getId(), true));
    }

    @Test
    void approveBooking_twice_throwsValidation() {
        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));
        Booking created = bookingService.createBooking(booking, item.getId(), booker.getId());

        bookingService.setApprovedOrRejected(created.getBookingId(), owner.getId(), true);

        assertThrows(ValidationException.class,
                () -> bookingService.setApprovedOrRejected(created.getBookingId(), owner.getId(), false));
    }

    @Test
    void getUserBookings_all_returnsCreated() {
        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));
        Booking created = bookingService.createBooking(booking, item.getId(), booker.getId());

        List<Booking> bookings = bookingService.getUserBookings(booker.getId(), BookingState.ALL);

        assertFalse(bookings.isEmpty());
        assertTrue(bookings.stream().anyMatch(b -> b.getBookingId().equals(created.getBookingId())));
    }

    @Test
    void getOwnerBookings_all_returnsCreated() {
        Booking booking = new Booking();
        booking.setStartTimeBooking(LocalDateTime.now().plusHours(1));
        booking.setEndTimeBooking(LocalDateTime.now().plusHours(2));
        Booking created = bookingService.createBooking(booking, item.getId(), booker.getId());

        List<Booking> bookings = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL);

        assertFalse(bookings.isEmpty());
        assertTrue(bookings.stream().anyMatch(b -> b.getBookingId().equals(created.getBookingId())));
    }
}
