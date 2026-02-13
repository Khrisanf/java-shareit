package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.validate.OnCreate;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class ItemRequestController implements ItemRequestApi {

    private final ItemRequestClient client;

    @Override
    public ResponseEntity<ItemRequestDto> create(
            Long userId,
            @Validated(OnCreate.class) ItemRequestCreateDto itemRequestCreateDto
    ) {
        return client.create(userId, itemRequestCreateDto);
    }

    @Override
    public ResponseEntity<ItemRequestDto> getOneById(Long userId, Long requestId) {
        return client.getOneById(userId, requestId);
    }

    @Override
    public ResponseEntity<List<ItemRequestDto>> getAll(Long userId) {
        return client.getAll(userId);
    }

    @Override
    public ResponseEntity<List<ItemRequestDto>> getAllOther(Long userId, int from, int size) {
        return client.getAllOther(userId, from, size);
    }
}
