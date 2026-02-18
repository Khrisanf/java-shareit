package ru.practicum.shareit.mvc;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServerApp;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.handler.ErrorHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = ShareItServerApp.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingService bookingService;
    @MockBean
    BookingMapper bookingMapper;

    @Test
    @DisplayName("POST /bookings -> 201 + body")
    void createBooking_ok() throws Exception {
        long bookerId = 11L;

        LocalDateTime start = LocalDateTime.of(2030, 1, 10, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 11, 10, 0, 0);

        BookingRequestDto requestDto = new BookingRequestDto(99L, start, end);

        Booking entity = new Booking();
        Booking created = new Booking();
        BookingResponseDto responseDto = new BookingResponseDto(1L, start, end, "WAITING", null, null);

        when(bookingMapper.toEntity(any(BookingRequestDto.class))).thenReturn(entity);
        when(bookingService.createBooking(any(Booking.class), eq(99L), eq(bookerId))).thenReturn(created);
        when(bookingMapper.toResponseDto(created)).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value("2030-01-10T10:00:00"))
                .andExpect(jsonPath("$.end").value("2030-01-11T10:00:00"));

        verify(bookingService).createBooking(any(Booking.class), eq(99L), eq(bookerId));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=true -> 200 + body")
    void approveOrReject_ok() throws Exception {
        long ownerId = 5L;
        long bookingId = 7L;

        Booking updated = new Booking();
        BookingResponseDto responseDto = new BookingResponseDto(
                bookingId,
                LocalDateTime.of(2030, 1, 10, 10, 0, 0),
                LocalDateTime.of(2030, 1, 11, 10, 0, 0),
                "APPROVED",
                null,
                null
        );

        when(bookingService.setApprovedOrRejected(bookingId, ownerId, true)).thenReturn(updated);
        when(bookingMapper.toResponseDto(updated)).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("GET /bookings/{id} -> 200 + body (важно: порядок аргументов в service)")
    void getBooking_ok() throws Exception {
        long userId = 9L;
        long bookingId = 100L;

        Booking booking = new Booking();
        BookingResponseDto responseDto = new BookingResponseDto(
                bookingId,
                LocalDateTime.of(2030, 1, 10, 10, 0, 0),
                LocalDateTime.of(2030, 1, 11, 10, 0, 0),
                "WAITING",
                null,
                null
        );

        when(bookingService.getBooking(bookingId, userId)).thenReturn(booking);
        when(bookingMapper.toResponseDto(booking)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100));
        verify(bookingService).getBooking(bookingId, userId);
    }

    @Test
    @DisplayName("GET /bookings?state=ALL -> 200 + list")
    void getUserBookings_ok() throws Exception {
        long userId = 9L;

        Booking b1 = new Booking();
        Booking b2 = new Booking();

        BookingResponseDto d1 = new BookingResponseDto(1L, null, null, "WAITING", null, null);
        BookingResponseDto d2 = new BookingResponseDto(2L, null, null, "APPROVED", null, null);

        when(bookingService.getUserBookings(userId, BookingState.ALL)).thenReturn(List.of(b1, b2));
        when(bookingMapper.toResponseDto(b1)).thenReturn(d1);
        when(bookingMapper.toResponseDto(b2)).thenReturn(d2);

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /bookings/owner?state=ALL -> 200 + list")
    void getOwnerBookings_ok() throws Exception {
        long ownerId = 9L;

        Booking b1 = new Booking();
        BookingResponseDto d1 = new BookingResponseDto(1L, null, null, "WAITING", null, null);

        when(bookingService.getOwnerBookings(ownerId, BookingState.ALL)).thenReturn(List.of(b1));
        when(bookingMapper.toResponseDto(b1)).thenReturn(d1);

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /bookings/{id} -> 404 если NotFoundException")
    void getBooking_notFound() throws Exception {
        long userId = 9L;
        long bookingId = 999L;

        when(bookingService.getBooking(bookingId, userId))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Booking not found")));
    }

    @Test
    @DisplayName("PATCH /bookings/{id} -> 400 если ValidationException")
    void approve_validationError() throws Exception {
        long ownerId = 5L;
        long bookingId = 7L;

        when(bookingService.setApprovedOrRejected(bookingId, ownerId, true))
                .thenThrow(new ValidationException("Booking status must be WAITING"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Booking status must be WAITING")));
    }
}
