package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

@RestController
@RequiredArgsConstructor
public class ItemController implements ItemApi {

    private final ItemClient itemClient;

    @Override
    public ResponseEntity<ItemDto> createItem(Long ownerId, @Validated(OnCreate.class) ItemDto item) {
        return itemClient.createItem(ownerId, item);
    }

    @Override
    public ResponseEntity<CommentResponseDto> addComment(Long itemId, Long userId, @Valid CommentRequestDto request) {
        return itemClient.addComment(itemId, userId, request);
    }

    @Override
    public ResponseEntity<ItemDto> updateItem(Long ownerId, Long itemId, @Validated(OnUpdate.class) ItemDto item) {
        return itemClient.updateItem(ownerId, itemId, item);
    }

    @Override
    public ResponseEntity<Void> deleteItem(Long ownerId, Long itemId) {
        return itemClient.deleteItem(ownerId, itemId);
    }

    @Override
    public ResponseEntity<List<ItemDto>> searchItems(Long userId, String text) {
        return itemClient.searchItems(userId, text);
    }

    @Override
    public ResponseEntity<ItemWithCommentsDto> getItem(Long userId, Long itemId) {
        return itemClient.getItem(userId, itemId);
    }

    @Override
    public ResponseEntity<List<ItemWithCommentsDto>> getAllItemsFromUser(Long ownerId) {
        return itemClient.getAllItemsFromUser(ownerId);
    }
}
