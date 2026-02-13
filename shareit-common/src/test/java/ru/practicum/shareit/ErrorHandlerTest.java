package ru.practicum.shareit;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.handler.ErrorResponse;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void handleValidation_shouldReturnOriginalMessage() {
        var response = handler.handleValidation(new ValidationException("bad request"));
        assertThat(response).isEqualTo(new ErrorResponse("bad request"));
    }

    @Test
    void handleNotFound_shouldReturnOriginalMessage() {
        var response = handler.handleNotFound(new NotFoundException("not found"));
        assertThat(response).isEqualTo(new ErrorResponse("not found"));
    }

    @Test
    void handleConflict_shouldReturnOriginalMessage() {
        var response = handler.handleConflict(new ConflictException("conflict"));
        assertThat(response).isEqualTo(new ErrorResponse("conflict"));
    }

    @Test
    void handleForbidden_shouldReturnOriginalMessage() {
        var response = handler.handleForbidden(new ForbiddenException("forbidden"));
        assertThat(response).isEqualTo(new ErrorResponse("forbidden"));
    }

    @Test
    void handleSpringBadRequest_shouldReturnGenericMessage() {
        var ex = new ConstraintViolationException("whatever", Set.of());
        var response = handler.handleSpringBadRequest(ex);

        assertThat(response).isEqualTo(new ErrorResponse("Validation failed"));
    }

    @Test
    void handleDataIntegrity_shouldReturnGenericConflictMessage() {
        var response = handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate key"));
        assertThat(response).isEqualTo(new ErrorResponse("Data conflict"));
    }

    @Test
    void handleOther_shouldReturnGenericUnexpectedError() {
        var response = handler.handleOther(new RuntimeException("boom"));
        assertThat(response).isEqualTo(new ErrorResponse("Unexpected error"));
    }

    @Test
    void errorResponse_shouldExposeErrorField() {
        var dto = new ErrorResponse("x");
        assertThat(dto.error()).isEqualTo("x");
    }
}
