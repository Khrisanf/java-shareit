package ru.practicum.shareit;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = TestBootConfig.class)
class ItemDtoJsonTest {

    @Autowired private JacksonTester<ItemDto> itemJson;
    @Autowired private JacksonTester<CommentResponseDto> commentResponseJson;

    @Autowired private JacksonTester<CommentRequestDto> commentRequestJson;
    @Autowired private JacksonTester<ItemResponseDto> itemResponseJson;
    @Autowired private JacksonTester<ItemWithCommentsDto> itemWithCommentsJson;

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldSerializeIsAvailableAsAvailable() throws Exception {
        var dto = new ItemDto(1L, "n", "d", 0L, false, 2L);

        var json = itemJson.write(dto);

        assertThat(json).hasJsonPathValue("$.available");
        assertThat(json).extractingJsonPathBooleanValue("$.available").isFalse();
        assertThat(json).doesNotHaveJsonPath("$.isAvailable");
    }

    @Test
    void shouldRoundTripLocalDateTime() throws Exception {
        var dto = new CommentResponseDto(
                10L, "hello", "Petrichor",
                LocalDateTime.of(2030, 1, 1, 12, 30)
        );

        var json = commentResponseJson.write(dto);
        var back = commentResponseJson.parse(json.getJson()).getObject();

        assertThat(back).isEqualTo(dto);
    }

    @Test
    void commentRequestDto_shouldRoundTrip() throws Exception {
        var dto = new CommentRequestDto("text");

        var json = commentRequestJson.write(dto);
        var back = commentRequestJson.parse(json.getJson()).getObject();

        assertThat(back).isEqualTo(dto);
    }

    @Test
    void commentRequestDto_shouldFailValidationWhenBlank() {
        var dto = new CommentRequestDto("   ");

        var violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("text"));
    }

    @Test
    void itemResponseDto_shouldRoundTrip() throws Exception {
        var dto = new ItemResponseDto(5L, "drill");

        var json = itemResponseJson.write(dto);
        var back = itemResponseJson.parse(json.getJson()).getObject();

        assertThat(back).isEqualTo(dto);
    }

    @Test
    void itemWithCommentsDto_shouldSerializeAvailableAndRoundTrip() throws Exception {
        var last = new BookingShortDto(100L, 7L);
        var next = new BookingShortDto(101L, 8L);

        var comments = List.of(
                new CommentResponseDto(
                        10L, "ok", "Petrichor",
                        LocalDateTime.of(2030, 1, 1, 12, 30)
                )
        );

        var dto = new ItemWithCommentsDto(
                1L,
                "name",
                "desc",
                0L,
                true,
                last,
                next,
                comments
        );

        var json = itemWithCommentsJson.write(dto);

        assertThat(json).hasJsonPathValue("$.available");
        assertThat(json).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(json).doesNotHaveJsonPath("$.isAvailable");

        var back = itemWithCommentsJson.parse(json.getJson()).getObject();
        assertThat(back).isEqualTo(dto);
    }
}
