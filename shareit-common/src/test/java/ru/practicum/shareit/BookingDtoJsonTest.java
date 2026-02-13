package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = TestBootConfig.class)
class BookingDtoJsonTest {

    @Autowired
    ObjectMapper objectMapper;

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
}
