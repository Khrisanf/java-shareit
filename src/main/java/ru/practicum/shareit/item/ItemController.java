package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.net.URI;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public ItemController(final ItemService itemService, final ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @Valid @RequestBody ItemDto item

    ) {
        Item createdItem = itemService.createItem(itemMapper.toEntity(item), ownerId);
        return ResponseEntity.created(URI.create("/items/" + createdItem.getId())).body(itemMapper.toDto(createdItem));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader("X-Sharer-User-Id")
            Long ownerId, @PathVariable("id")
            Long itemId, @Valid @RequestBody ItemDto item
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
}

