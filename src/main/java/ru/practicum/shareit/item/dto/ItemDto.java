package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import ru.practicum.shareit.user.validate.OnCreate;
import ru.practicum.shareit.user.validate.OnUpdate;

public record ItemDto(
        Long id,

        @NotBlank(groups = OnCreate.class)
        @Pattern(regexp = ".*\\S.*", groups = OnUpdate.class)
        String name,

        @NotBlank(groups = OnCreate.class)
        @Pattern(regexp = ".*\\S.*", message = "description must not be blank.", groups = OnUpdate.class)
        String description,

        @PositiveOrZero(groups = {OnCreate.class, OnUpdate.class})
        Long useCount,

        @JsonProperty("available")
        @NotNull(groups = OnCreate.class)
        Boolean isAvailable,

        @Positive(groups = {OnCreate.class, OnUpdate.class})
        Long requestId
) {
}
