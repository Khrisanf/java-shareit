package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = TestBootConfig.class)
class BookingDtoTest {

    @Autowired ObjectMapper objectMapper;

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ---------------- JSON ----------------

    @Test
    void shouldSerializeAndDeserializeBookingRequestDto() throws Exception {
        var dto = new BookingRequestDto(
                1L,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 2, 12, 0)
        );

        String json = objectMapper.writeValueAsString(dto);
        var back = objectMapper.readValue(json, BookingRequestDto.class);

        assertThat(back).isEqualTo(dto);
    }

    @Test
    void shouldSerializeAndDeserializeBookingShortDto() throws Exception {
        var dto = new BookingShortDto(10L, 99L);

        String json = objectMapper.writeValueAsString(dto);
        var back = objectMapper.readValue(json, BookingShortDto.class);

        assertThat(back).isEqualTo(dto);
    }

    @Test
    void shouldSerializeAndDeserializeBookingResponseDto_withNullNested() throws Exception {
        // Вложенные DTO можно пока оставить null, если они неудобны/не готовы.
        // Это всё равно валидный тест на контракт JSON для самого BookingResponseDto.
        var dto = new BookingResponseDto(
                10L,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 2, 12, 0),
                "APPROVED",
                null,
                null
        );

        String json = objectMapper.writeValueAsString(dto);
        var back = objectMapper.readValue(json, BookingResponseDto.class);

        assertThat(back).isEqualTo(dto);
    }

    // ---------------- Validation (OnCreate) ----------------

    @Nested
    class BookingRequestDtoValidation {

        @Test
        void onCreate_shouldRejectNullItemId() {
            var dto = new BookingRequestDto(
                    null,
                    LocalDateTime.of(2030, 1, 1, 12, 0),
                    LocalDateTime.of(2030, 1, 2, 12, 0)
            );

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("itemId"));
        }

        @Test
        void onCreate_shouldRejectStartInPast() {
            var dto = new BookingRequestDto(
                    1L,
                    LocalDateTime.of(2020, 1, 1, 12, 0),
                    LocalDateTime.of(2030, 1, 2, 12, 0)
            );

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("start"));
        }

        @Test
        void onCreate_shouldRejectEndInPast() {
            var now = LocalDateTime.now();

            var dto = new BookingRequestDto(
                    1L,
                    now.plusDays(2),
                    now.minusDays(1)
            );

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("end"));
        }

        @Test
        void onCreate_shouldAcceptValidDto() {
            var dto = new BookingRequestDto(
                    1L,
                    LocalDateTime.of(2030, 1, 1, 12, 0),
                    LocalDateTime.of(2030, 1, 2, 12, 0)
            );

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).isEmpty();
        }
    }

    // ---------------- Enums ----------------

    @Test
    void bookingState_shouldHaveExpectedConstants() {
        assertThat(BookingState.values())
                .containsExactly(
                        BookingState.ALL,
                        BookingState.CURRENT,
                        BookingState.PAST,
                        BookingState.FUTURE,
                        BookingState.WAITING,
                        BookingState.REJECTED
                );

        for (var v : BookingState.values()) {
            assertThat(BookingState.valueOf(v.name())).isSameAs(v);
        }
    }

    @Test
    void status_shouldHaveExpectedConstants() {
        assertThat(Status.values())
                .containsExactly(Status.WAITING, Status.APPROVED, Status.REJECTED);

        for (var v : Status.values()) {
            assertThat(Status.valueOf(v.name())).isSameAs(v);
        }
    }
}
