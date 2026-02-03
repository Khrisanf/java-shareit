package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.validate.OnCreate;
import ru.practicum.shareit.user.validate.OnUpdate;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @Validated(OnCreate.class) @RequestBody ItemDto item

    ) {
        Item createdItem = itemService.createItem(itemMapper.toEntity(item), ownerId);
        return ResponseEntity.created(URI.create("/items/" + createdItem.getId())).body(itemMapper.toDto(createdItem));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader("X-Sharer-User-Id")
            Long ownerId, @PathVariable("id")
            Long itemId, @Validated(OnUpdate.class) @RequestBody ItemDto item
    ) {
        Item updatedItem = itemService.updateItem(ownerId, itemId, itemMapper.toEntity(item));
        return ResponseEntity.ok(itemMapper.toDto(updatedItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable("id") Long itemId
    ) {
        itemService.deleteItem(ownerId, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItems(
            @RequestHeader("X-Sharer-User-Id")
            Long ownerId, @PathVariable("id") Long itemId
    ) {
        Item item = itemService.findById(ownerId, itemId);
        return ResponseEntity.ok(itemMapper.toDto(item));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsFromUser(
            @RequestHeader("X-Sharer-User-Id") Long ownerId
    ) {
        List<ItemDto> result = itemService.getAllByOwner(ownerId).stream()
                .map(itemMapper::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam("text") String text
    ) {
        List<ItemDto> result = itemService.search(text).stream()
                .map(itemMapper::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }

}

