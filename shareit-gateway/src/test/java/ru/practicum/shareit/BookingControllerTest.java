package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = ShareItGatewayApp.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingClient bookingClient;

    @Test
    void createBooking_validBody_shouldReturnResponseAndDelegateToClient() throws Exception {
        long bookerId = 10L;
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = start.plusDays(1);

        BookingRequestDto req = new BookingRequestDto(1L, start, end);

        BookingResponseDto resp = new BookingResponseDto(
                99L, start, end, "WAITING", null, null
        );

        when(bookingClient.createBooking(eq(bookerId), any(BookingRequestDto.class)))
                .thenReturn(ResponseEntity.status(201).body(resp));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.status").value("WAITING"));

        ArgumentCaptor<BookingRequestDto> captor = ArgumentCaptor.forClass(BookingRequestDto.class);
        verify(bookingClient, times(1)).createBooking(eq(bookerId), captor.capture());
        assertThat(captor.getValue().itemId()).isEqualTo(1L);
        assertThat(captor.getValue().start()).isEqualTo(start);
        assertThat(captor.getValue().end()).isEqualTo(end);
    }

    @Test
    void createBooking_invalidDates_shouldReturn400_andNotCallClient() throws Exception {
        long bookerId = 10L;
        LocalDateTime start = LocalDateTime.now().minusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(1).withNano(0);

        BookingRequestDto req = new BookingRequestDto(1L, start, end);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void approveOrReject_shouldDelegateToClient() throws Exception {
        long ownerId = 5L;
        long bookingId = 7L;

        BookingResponseDto resp = new BookingResponseDto(
                bookingId, null, null, "APPROVED", null, null
        );

        when(bookingClient.approveOrReject(bookingId, ownerId, true))
                .thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingClient).approveOrReject(bookingId, ownerId, true);
    }

    @Test
    void getBooking_shouldDelegateToClient() throws Exception {
        long userId = 11L;
        long bookingId = 12L;

        BookingResponseDto resp = new BookingResponseDto(
                bookingId, null, null, "WAITING", null, null
        );

        when(bookingClient.getBooking(userId, bookingId)).thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));

        verify(bookingClient).getBooking(userId, bookingId);
    }

    @Test
    void getUserBookings_shouldDelegateToClient() throws Exception {
        long userId = 20L;

        when(bookingClient.getUserBookings(userId, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingClient).getUserBookings(userId, BookingState.ALL);
    }

    @Test
    void getOwnerBookings_shouldDelegateToClient() throws Exception {
        long ownerId = 21L;

        when(bookingClient.getOwnerBookings(ownerId, BookingState.PAST))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", "PAST"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(ownerId, BookingState.PAST);
    }
}
