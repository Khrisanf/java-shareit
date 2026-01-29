package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;

public record UserDto(
        Long id,
        String name,
        @Email String email
) {}
