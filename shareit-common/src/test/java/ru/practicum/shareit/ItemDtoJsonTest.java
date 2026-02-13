package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = TestBootConfig.class)
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> itemJson;

    @Autowired
    private JacksonTester<CommentResponseDto> commentJson;

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

        var json = commentJson.write(dto);
        var back = commentJson.parse(json.getJson()).getObject();

        assertThat(back).isEqualTo(dto);
    }
}
