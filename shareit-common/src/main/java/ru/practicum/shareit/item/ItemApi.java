package ru.practicum.shareit.item;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import ru.practicum.shareit.HeaderNames;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;

public interface ItemApi {

    @RequestMapping(method = RequestMethod.POST, value = "/items")
    ResponseEntity<ItemDto> createItem(
            @RequestHeader(HeaderNames.USER_ID) Long ownerId,
            @RequestBody ItemDto item
    );

    @RequestMapping(method = RequestMethod.POST, value = "/items/{itemId}/comment")
    ResponseEntity<CommentResponseDto> addComment(
            @PathVariable("itemId") Long itemId,
            @RequestHeader(HeaderNames.USER_ID) Long userId,
            @RequestBody CommentRequestDto request
    );

    @RequestMapping(method = RequestMethod.PATCH, value = "/items/{id}")
    ResponseEntity<ItemDto> updateItem(
            @RequestHeader(HeaderNames.USER_ID) Long ownerId,
            @PathVariable("id") Long itemId,
            @RequestBody ItemDto item
    );

    @RequestMapping(method = RequestMethod.DELETE, value = "/items/{id}")
    ResponseEntity<Void> deleteItem(
            @RequestHeader(HeaderNames.USER_ID) Long ownerId,
            @PathVariable("id") Long itemId
    );

    @RequestMapping(method = RequestMethod.GET, value = "/items/search")
    ResponseEntity<List<ItemDto>> searchItems(
            @RequestHeader(HeaderNames.USER_ID) Long userId,
            @RequestParam("text") String text
    );

    @RequestMapping(method = RequestMethod.GET, value = "/items/{id}")
    ResponseEntity<ItemWithCommentsDto> getItem(
            @RequestHeader(HeaderNames.USER_ID) Long userId,
            @PathVariable("id") Long itemId
    );

    @RequestMapping(method = RequestMethod.GET, value = "/items")
    ResponseEntity<List<ItemWithCommentsDto>> getAllItemsFromUser(
            @RequestHeader(HeaderNames.USER_ID) Long ownerId
    );
}
