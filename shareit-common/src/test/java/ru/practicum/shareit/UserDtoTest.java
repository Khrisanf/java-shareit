package ru.practicum.shareit;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.BookerDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    class BookerDtoTests {

        @Test
        void shouldCreateBookerDto() {
            var dto = new BookerDto(1L);
            assertThat(dto.id()).isEqualTo(1L);
        }
    }

    @Nested
    class UserDtoValidationTests {

        @Test
        void create_shouldFail_whenNameBlank() {
            var dto = new UserDto(1L, "   ", "a@b.com");

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("name");
        }

        @Test
        void create_shouldFail_whenEmailBlank() {
            var dto = new UserDto(1L, "name", "   ");

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("email");
        }

        @Test
        void create_shouldFail_whenEmailInvalid() {
            var dto = new UserDto(1L, "name", "not-an-email");

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("email");
        }

        @Test
        void update_shouldFail_whenNameOnlySpaces_dueToPattern() {
            var dto = new UserDto(1L, "   ", "a@b.com");

            var violations = validator.validate(dto, OnUpdate.class);

            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("name");
        }

        @Test
        void update_shouldFail_whenEmailInvalid() {
            var dto = new UserDto(1L, "name", "bad");

            var violations = validator.validate(dto, OnUpdate.class);

            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("email");
        }

        @Test
        void create_shouldPass_whenValid() {
            var dto = new UserDto(1L, "Alice", "alice@example.com");

            var violations = validator.validate(dto, OnCreate.class);

            assertThat(violations).isEmpty();
        }

        @Test
        void update_shouldPass_whenValid() {
            var dto = new UserDto(1L, "Alice", "alice@example.com");

            var violations = validator.validate(dto, OnUpdate.class);

            assertThat(violations).isEmpty();
        }

        @Test
        void withoutGroups_shouldNotValidate_groupedConstraints() {
            var dto = new UserDto(1L, "   ", "bad");

            var violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }
}
