package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.User;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock UserRepository userRepository;
    @Mock ItemRepository itemRepository;

    @InjectMocks
    BookingService bookingService;

    // -------- helpers --------

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@mail.ru");
        return u;
    }

    private static Item item(long id, long ownerId, boolean available) {
        Item it = new Item();
        it.setId(id);
        it.setName("Item" + id);
        it.setDescription("Desc");
        it.setIsAvailable(available);
        it.setOwner(user(ownerId));
        return it;
    }

    private static Booking booking(long id, long itemId, long ownerId, long bookerId, Status status) {
        Booking b = new Booking();
        b.setBookingId(id);

        Item it = item(itemId, ownerId, true);
        b.setItem(it);

        User bk = user(bookerId);
        b.setBooker(bk);

        b.setStatus(status);
        b.setStartTimeBooking(LocalDateTime.now().plusDays(1));
        b.setEndTimeBooking(LocalDateTime.now().plusDays(2));
        return b;
    }

    // -------- createBooking --------

    @Test
    void createBooking_whenItemNotFound_shouldThrowNotFound() {
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        Booking b = new Booking();
        b.setStartTimeBooking(LocalDateTime.now().plusDays(1));
        b.setEndTimeBooking(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(b, 10L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ItemResponseDto not found");

        verify(itemRepository).findById(10L);
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createBooking_whenUserNotFound_shouldThrowNotFound() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, 2L, true)));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Booking b = new Booking();
        b.setStartTimeBooking(LocalDateTime.now().plusDays(1));
        b.setEndTimeBooking(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(b, 10L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(itemRepository).findById(10L);
        verify(userRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void createBooking_whenOwnerBooksOwnItem_shouldThrowNotFound() {
        long ownerId = 1L;
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, ownerId, true)));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));

        Booking b = new Booking();
        b.setStartTimeBooking(LocalDateTime.now().plusDays(1));
        b.setEndTimeBooking(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(b, 10L, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner cannot book own item");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenItemUnavailable_shouldThrowValidation() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, 2L, false)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        Booking b = new Booking();
        b.setStartTimeBooking(LocalDateTime.now().plusDays(1));
        b.setEndTimeBooking(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(b, 10L, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("unavailable");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenDatesMissing_shouldThrowValidation() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, 2L, true)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        Booking b = new Booking();
        b.setStartTimeBooking(null);
        b.setEndTimeBooking(null);

        assertThatThrownBy(() -> bookingService.createBooking(b, 10L, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start/end must be provided");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenStartNotBeforeEnd_shouldThrowValidation() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, 2L, true)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        Booking b = new Booking();
        b.setStartTimeBooking(LocalDateTime.now().plusDays(2));
        b.setEndTimeBooking(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.createBooking(b, 10L, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start must be before end");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenValid_shouldSetFieldsAndSave() {
        long itemId = 10L;
        long ownerId = 2L;
        long bookerId = 1L;

        Item it = item(itemId, ownerId, true);
        User booker = user(bookerId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(it));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking b = new Booking();
        b.setStartTimeBooking(LocalDateTime.now().plusDays(1));
        b.setEndTimeBooking(LocalDateTime.now().plusDays(2));

        Booking saved = bookingService.createBooking(b, itemId, bookerId);

        assertThat(saved.getItem().getId()).isEqualTo(itemId);
        assertThat(saved.getBooker().getId()).isEqualTo(bookerId);
        assertThat(saved.getStatus()).isEqualTo(Status.WAITING);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking toSave = captor.getValue();

        assertThat(toSave.getItem().getId()).isEqualTo(itemId);
        assertThat(toSave.getBooker().getId()).isEqualTo(bookerId);
        assertThat(toSave.getStatus()).isEqualTo(Status.WAITING);
    }

    // -------- setApprovedOrRejected --------

    @Test
    void setApprovedOrRejected_whenBookingNotFound_shouldThrowNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.setApprovedOrRejected(99L, 1L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking not found");

        verify(bookingRepository).findById(99L);
        verifyNoMoreInteractions(bookingRepository, userRepository, itemRepository);
    }

    @Test
    void setApprovedOrRejected_whenNotOwner_shouldThrowForbidden() {
        Booking b = booking(1L, 10L, 2L, 1L, Status.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> bookingService.setApprovedOrRejected(1L, 999L, true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only item owner");

        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setApprovedOrRejected_whenStatusNotWaiting_shouldThrowValidation() {
        Booking b = booking(1L, 10L, 2L, 1L, Status.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> bookingService.setApprovedOrRejected(1L, 2L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must be WAITING");

        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setApprovedOrRejected_whenApprovedTrue_shouldSetApprovedAndSave() {
        Booking b = booking(1L, 10L, 2L, 1L, Status.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking res = bookingService.setApprovedOrRejected(1L, 2L, true);

        assertThat(res.getStatus()).isEqualTo(Status.APPROVED);
        verify(bookingRepository).save(b);
    }

    @Test
    void setApprovedOrRejected_whenApprovedFalse_shouldSetRejectedAndSave() {
        Booking b = booking(1L, 10L, 2L, 1L, Status.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking res = bookingService.setApprovedOrRejected(1L, 2L, false);

        assertThat(res.getStatus()).isEqualTo(Status.REJECTED);
        verify(bookingRepository).save(b);
    }

    // -------- getBooking --------

    @Test
    void getBooking_whenUserDoesNotExist_shouldThrowNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getBooking(10L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).existsById(1L);
        verifyNoMoreInteractions(userRepository, bookingRepository, itemRepository);
    }

    @Test
    void getBooking_whenNotOwnerOrBooker_shouldThrowNotFound() {
        when(userRepository.existsById(999L)).thenReturn(true);

        Booking b = booking(1L, 10L, 2L, 1L, Status.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> bookingService.getBooking(1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not accessable");

        verify(userRepository).existsById(999L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getBooking_whenOwner_shouldReturnBooking() {
        long ownerId = 2L;
        when(userRepository.existsById(ownerId)).thenReturn(true);

        Booking b = booking(1L, 10L, ownerId, 1L, Status.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        Booking res = bookingService.getBooking(1L, ownerId);

        assertThat(res.getBookingId()).isEqualTo(1L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getBooking_whenBooker_shouldReturnBooking() {
        long bookerId = 1L;
        when(userRepository.existsById(bookerId)).thenReturn(true);

        Booking b = booking(1L, 10L, 2L, bookerId, Status.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        Booking res = bookingService.getBooking(1L, bookerId);

        assertThat(res.getBookingId()).isEqualTo(1L);
        verify(bookingRepository).findById(1L);
    }

    // -------- getUserBookings --------

    @Test
    void getUserBookings_whenUserNotExists_shouldThrowNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getUserBookings(1L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).existsById(1L);
        verifyNoMoreInteractions(userRepository, bookingRepository, itemRepository);
    }

    @Test
    void getUserBookings_stateAll_shouldCallFindAll() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByBookerIdOrderByStartTimeBookingDesc(1L)).thenReturn(List.of());

        List<Booking> res = bookingService.getUserBookings(1L, BookingState.ALL);

        assertThat(res).isEmpty();
        verify(bookingRepository).findByBookerIdOrderByStartTimeBookingDesc(1L);
    }

    @Test
    void getUserBookings_stateWaiting_shouldCallFindWaiting() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatusOrderByStartTimeBookingDesc(1L, Status.WAITING))
                .thenReturn(List.of());

        List<Booking> res = bookingService.getUserBookings(1L, BookingState.WAITING);

        assertThat(res).isEmpty();
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartTimeBookingDesc(1L, Status.WAITING);
    }

    @Test
    void getUserBookings_stateCurrent_shouldCallFindCurrentWithNow() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStartTimeBookingLessThanEqualAndEndTimeBookingGreaterThanEqualOrderByStartTimeBookingDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of());

        bookingService.getUserBookings(1L, BookingState.CURRENT);

        verify(bookingRepository)
                .findByBookerIdAndStartTimeBookingLessThanEqualAndEndTimeBookingGreaterThanEqualOrderByStartTimeBookingDesc(
                        eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)
                );
    }

    // -------- getOwnerBookings --------

    @Test
    void getOwnerBookings_stateAll_shouldCallOwnerAll() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdOrderByStartTimeBookingDesc(2L)).thenReturn(List.of());

        List<Booking> res = bookingService.getOwnerBookings(2L, BookingState.ALL);

        assertThat(res).isEmpty();
        verify(bookingRepository).findByItemOwnerIdOrderByStartTimeBookingDesc(2L);
    }

    @Test
    void getOwnerBookings_stateRejected_shouldCallOwnerRejected() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartTimeBookingDesc(2L, Status.REJECTED))
                .thenReturn(List.of());

        bookingService.getOwnerBookings(2L, BookingState.REJECTED);

        verify(bookingRepository).findByItemOwnerIdAndStatusOrderByStartTimeBookingDesc(2L, Status.REJECTED);
    }
}
