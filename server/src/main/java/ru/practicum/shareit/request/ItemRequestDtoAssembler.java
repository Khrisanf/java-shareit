package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestDtoAssembler {

    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    public List<ItemRequestDto> mapRequestsToDtos(List<ItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemShortDto>> itemsByRequestId = getItemsByRequestId(requestIds);

        return requests.stream()
                .map(r -> itemRequestMapper.toDto(
                        r,
                        itemsByRequestId.getOrDefault(r.getId(), List.of())
                ))
                .toList();
    }

    private Map<Long, List<ItemShortDto>> getItemsByRequestId(List<Long> requestIds) {
        return itemRequestService.findAllItemsByRequestsIds(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getItemRequest().getId(),
                        Collectors.mapping(itemMapper::toItemShortDto, Collectors.toList())
                ));
    }
}
