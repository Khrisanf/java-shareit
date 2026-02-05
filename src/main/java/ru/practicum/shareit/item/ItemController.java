package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentRequestDto;
import ru.practicum.shareit.item.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
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
    private final CommentMapper commentMapper;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @Validated(OnCreate.class) @RequestBody ItemDto item

    ) {
        Item createdItem = itemService.createItem(itemMapper.toEntity(item), ownerId);
        return ResponseEntity.created(URI.create("/items/" + createdItem.getId())).body(itemMapper.toDto(createdItem));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable Long itemId,
                                                         @RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @Valid @RequestBody CommentRequestDto request) {
        Comment commentEntity = itemService.addComment(userId, itemId, request);

        CommentResponseDto responseDto = commentMapper.toDto(commentEntity);

        return ResponseEntity
                .created(URI.create("/items/" + itemId + "/comment/" + responseDto.id()))
                .body(responseDto);
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

    @GetMapping("/{id}")
    public ResponseEntity<ItemWithCommentsDto> getItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable("id") Long itemId
    ) {
        ItemDetailsDto details = itemService.getItemWithComments(userId, itemId);

        return ResponseEntity.ok(itemMapper.toItemWithCommentsDto(details));
    }

    @GetMapping
    public ResponseEntity<List<ItemWithCommentsDto>> getAllItemsFromUser(
            @RequestHeader("X-Sharer-User-Id") Long ownerId
    ) {
        List<ItemDetailsDto> detailsList = itemService.getAllByOwnerWithComments(ownerId);

        return ResponseEntity.ok(itemMapper.toItemWithCommentsDtoList(detailsList));
    }
}

