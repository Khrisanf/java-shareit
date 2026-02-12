package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.net.URI;
import java.util.List;

/*TODO: ПРОВЕСТИ РЕФАКТОРИНГ КОДА*/

@RestController
@RequiredArgsConstructor
public class ItemRequestController implements ItemRequestApi {

    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    private final ItemRequestDtoAssembler itemRequestDtoAssembler;


    @Override
    public ResponseEntity<ItemRequestDto> create(Long userId, ItemRequestCreateDto itemRequestCreateDto) {
        ItemRequest createdItemRequest =
                itemRequestService.create(itemRequestMapper.toEntity(itemRequestCreateDto), userId);

        return ResponseEntity
                .created(URI.create("/requests/" + createdItemRequest.getId()))
                .body(itemRequestMapper.toDto(createdItemRequest, List.of()));
    }

    @Override
    public ResponseEntity<ItemRequestDto> getOneById(Long userId, Long requestId) {
        ItemRequest request = itemRequestService.findById(userId, requestId);

        List<ItemShortDto> items = itemRequestService.findItemsByRequestId(requestId).stream()
                .map(item -> new ItemShortDto(item.getId(), item.getName(), item.getOwner().getId()))
                .toList();

        return ResponseEntity.ok(itemRequestMapper.toDto(request, items));
    }

    @Override
    public ResponseEntity<List<ItemRequestDto>> getAll(Long userId) {
        List<ItemRequest> requests = itemRequestService.findAllByRequestor(userId);
        return ResponseEntity.ok(itemRequestDtoAssembler.mapRequestsToDtos(requests));
    }

    @Override
    public ResponseEntity<List<ItemRequestDto>> getAllOther(Long userId, int from, int size) {
        List<ItemRequest> requests = itemRequestService.findAllOtherRequests(userId, from, size);
        return ResponseEntity.ok(itemRequestDtoAssembler.mapRequestsToDtos(requests));
    }
}
