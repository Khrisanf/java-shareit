package ru.practicum.shareit.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import ru.practicum.shareit.HeaderNames;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

public interface ItemRequestApi {

    @RequestMapping(method = RequestMethod.POST, value = "/requests")
    ResponseEntity<ItemRequestDto> create(
            @RequestHeader(HeaderNames.USER_ID) Long userId,
            @RequestBody ItemRequestCreateDto itemRequestCreateDto
    );

    @RequestMapping(method = RequestMethod.GET, value = "/requests/{requestId}")
    ResponseEntity<ItemRequestDto> getOneById(
            @RequestHeader(HeaderNames.USER_ID) Long userId,
            @PathVariable("requestId") Long requestId
    );

    @RequestMapping(method = RequestMethod.GET, value = "/requests")
    ResponseEntity<List<ItemRequestDto>> getAll(
            @RequestHeader(HeaderNames.USER_ID) Long userId
    );

    @RequestMapping(method = RequestMethod.GET, value = "/requests/all")
    ResponseEntity<List<ItemRequestDto>> getAllOther(
            @RequestHeader(HeaderNames.USER_ID) Long userId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size
    );

}
