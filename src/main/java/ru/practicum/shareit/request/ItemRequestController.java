package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.validate.OnCreate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @PostMapping
    public ResponseEntity<ItemRequestDto> create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Validated(OnCreate.class) @RequestBody ItemRequestCreateDto itemRequestCreateDto
    ) {
        ItemRequest createdItemRequest = itemRequestService.create(itemRequestMapper.toEntity(itemRequestCreateDto), userId);
        return ResponseEntity.created(URI.create("/requests/" + createdItemRequest.getId())).body(itemRequestMapper.toDto(createdItemRequest, List.of()));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getOneById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId
    ) {
        ItemRequest request = itemRequestService.findById(userId, requestId);
        List<ItemShortDto> items = itemRequestService.findItemsByRequestId(requestId).stream()
                .map(item -> new ItemShortDto(item.getId(), item.getName(), item.getOwner().getId()))
                .toList();
        return ResponseEntity.ok(itemRequestMapper.toDto(request, items));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getAll(
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        List<ItemRequest> requests = itemRequestService.findAllByRequestor(userId);
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();
        List<Item> items = itemRequestService.findAllItemsByRequestsIds(requestIds);
        Map<Long, List<ItemShortDto>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getItemRequest().getId(),
                        Collectors.mapping(itemMapper::toItemShortDto, Collectors.toList())
                ));
        return ResponseEntity.ok(requests.stream()
                .map(r -> itemRequestMapper.toDto(r, itemsByRequestId.getOrDefault(r.getId(), List.of())))
                .toList());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllOther(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<ItemRequest> requests = itemRequestService.findAllOtherRequests(userId, from, size);
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId).toList();
        List<Item> items = itemRequestService.findAllItemsByRequestsIds(requestIds);
        Map<Long, List<ItemShortDto>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getItemRequest().getId(),
                        Collectors.mapping(itemMapper::toItemShortDto, Collectors.toList())
                ));
        return ResponseEntity.ok(requests.stream()
                .map(r -> itemRequestMapper.toDto(r, itemsByRequestId.getOrDefault(r.getId(), List.of())))
                .toList());
    }
}
