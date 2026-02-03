package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import ru.practicum.shareit.user.validate.OnCreate;
import ru.practicum.shareit.user.validate.OnUpdate;

public record UserDto(
        Long id,

        @NotBlank(groups = OnCreate.class)
        @Pattern(regexp = ".*\\S.*", message = "name must not be blank", groups = OnUpdate.class)
        String name,

        @NotBlank(groups = OnCreate.class)
        @Email(groups = {OnCreate.class, OnUpdate.class})
        String email
) {
}
