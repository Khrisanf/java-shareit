package ru.practicum.shareit;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = TestBootConfig.class)
class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestJson;

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    class ValidationTests {

        @Test
        void create_shouldFail_whenDescriptionBlank_onCreateGroup() {
            var dto = new ItemRequestCreateDto("   ");

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("description");
        }

        @Test
        void create_shouldPass_whenDescriptionNotBlank_onCreateGroup() {
            var dto = new ItemRequestCreateDto("need a drill");

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class Json {

        @Test
        void itemRequestDto_shouldSerializeAndDeserialize_withLocalDateTimeAndItems() throws Exception {
            var dto = new ItemRequestDto(
                    10L,
                    "need a drill",
                    LocalDateTime.of(2030, 1, 1, 12, 30),
                    List.of(
                            new ItemShortDto(1L, "Drill", 99L),
                            new ItemShortDto(2L, "Saw", 99L)
                    )
            );

            var json = itemRequestJson.write(dto);
            var back = itemRequestJson.parse(json.getJson()).getObject();

            assertThat(back).isEqualTo(dto);
        }

        @Test
        void itemRequestDto_shouldHaveExpectedJsonPaths() throws Exception {
            var dto = new ItemRequestDto(
                    10L,
                    "need a drill",
                    LocalDateTime.of(2030, 1, 1, 12, 30),
                    List.of(new ItemShortDto(1L, "Drill", 99L))
            );

            var json = itemRequestJson.write(dto);

            assertThat(json).hasJsonPathNumberValue("$.id");
            assertThat(json).hasJsonPathStringValue("$.description");
            assertThat(json).hasJsonPathValue("$.created");
            assertThat(json).hasJsonPathArrayValue("$.items");
        }
    }
}
