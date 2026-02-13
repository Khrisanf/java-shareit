package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController implements ItemApi {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ResponseEntity<ItemDto> createItem(Long ownerId, ItemDto item) {
        Item createdItem = itemService.createItem(itemMapper.toEntity(item), ownerId, item.requestId());
        return ResponseEntity.created(URI.create("/items/" + createdItem.getId()))
                .body(itemMapper.toDto(createdItem));
    }

    @Override
    public ResponseEntity<CommentResponseDto> addComment(Long itemId, Long userId, CommentRequestDto request) {
        Comment commentEntity = itemService.addComment(userId, itemId, request);
        CommentResponseDto responseDto = commentMapper.toDto(commentEntity);

        return ResponseEntity
                .created(URI.create("/items/" + itemId + "/comment/" + responseDto.id()))
                .body(responseDto);
    }

    @Override
    public ResponseEntity<ItemDto> updateItem(Long ownerId, Long itemId, ItemDto item) {
        Item updatedItem = itemService.updateItem(ownerId, itemId, itemMapper.toEntity(item));
        return ResponseEntity.ok(itemMapper.toDto(updatedItem));
    }

    @Override
    public ResponseEntity<Void> deleteItem(Long ownerId, Long itemId) {
        itemService.deleteItem(ownerId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ItemDto>> searchItems(Long userId, String text) {
        List<ItemDto> result = itemService.search(text).stream()
                .map(itemMapper::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<ItemWithCommentsDto> getItem(Long userId, Long itemId) {
        ItemDetailsDto details = itemService.getItemWithComments(userId, itemId);
        return ResponseEntity.ok(itemMapper.toItemWithCommentsDto(details));
    }

    @Override
    public ResponseEntity<List<ItemWithCommentsDto>> getAllItemsFromUser(Long ownerId) {
        List<ItemDetailsDto> detailsList = itemService.getAllByOwnerWithComments(ownerId);
        return ResponseEntity.ok(itemMapper.toItemWithCommentsDtoList(detailsList));
    }
}