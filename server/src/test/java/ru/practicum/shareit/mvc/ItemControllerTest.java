package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServerApp;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemService;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.Comment;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItServerApp.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ItemService itemService;
    @MockBean ItemMapper itemMapper;
    @MockBean CommentMapper commentMapper;

    @Test
    @DisplayName("POST /items -> 201, Location, body")
    void createItem_ok() throws Exception {
        long ownerId = 10L;

        ItemDto requestDto = new ItemDto(
                null,
                "Drill",
                "Good drill",
                0L,
                true,
                null
        );

        Item entity = new Item();
        entity.setId(null);

        Item saved = new Item();
        saved.setId(1L);

        ItemDto responseDto = new ItemDto(
                1L,
                "Drill",
                "Good drill",
                0L,
                true,
                null
        );

        when(itemMapper.toEntity(any(ItemDto.class))).thenReturn(entity);
        when(itemService.createItem(any(Item.class), eq(ownerId), isNull())).thenReturn(saved);
        when(itemMapper.toDto(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, endsWith("/items/1")))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Good drill"))
                .andExpect(jsonPath("$.useCount").value(0))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService).createItem(any(Item.class), eq(ownerId), isNull());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    @DisplayName("PATCH /items/{id} -> 200 + body")
    void updateItem_ok() throws Exception {
        long ownerId = 10L;
        long itemId = 2L;

        ItemDto patchDto = new ItemDto(
                null,
                "New name",
                "New desc",
                1L,
                false,
                null
        );

        Item patchEntity = new Item();
        Item updated = new Item();
        updated.setId(itemId);

        ItemDto responseDto = new ItemDto(
                itemId,
                "New name",
                "New desc",
                1L,
                false,
                null
        );

        when(itemMapper.toEntity(any(ItemDto.class))).thenReturn(patchEntity);
        when(itemService.updateItem(eq(ownerId), eq(itemId), any(Item.class))).thenReturn(updated);
        when(itemMapper.toDto(updated)).thenReturn(responseDto);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.description").value("New desc"))
                .andExpect(jsonPath("$.useCount").value(1))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("DELETE /items/{id} -> 204")
    void deleteItem_ok() throws Exception {
        long ownerId = 10L;
        long itemId = 2L;

        doNothing().when(itemService).deleteItem(ownerId, itemId);

        mockMvc.perform(delete("/items/{id}", itemId)
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(itemService).deleteItem(ownerId, itemId);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    @DisplayName("GET /items/search?text=... -> 200 + list")
    void searchItems_ok() throws Exception {
        long userId = 99L;

        Item i1 = new Item(); i1.setId(1L);
        Item i2 = new Item(); i2.setId(2L);

        ItemDto d1 = new ItemDto(1L, "A", "DA", 0L, true, null);
        ItemDto d2 = new ItemDto(2L, "B", "DB", 0L, true, null);

        when(itemService.search("drill")).thenReturn(List.of(i1, i2));
        when(itemMapper.toDto(i1)).thenReturn(d1);
        when(itemMapper.toDto(i2)).thenReturn(d2);

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, userId)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /items/{id} -> 200 + ItemWithCommentsDto")
    void getItem_ok() throws Exception {
        long userId = 5L;
        long itemId = 7L;

        ItemDetailsDto details = new ItemDetailsDto(null, null, null, List.of());
        ItemWithCommentsDto responseDto = new ItemWithCommentsDto(
                itemId, "Name", "Desc", 0L, true,
                null, null,
                List.of()
        );

        when(itemService.getItemWithComments(userId, itemId)).thenReturn(details);
        when(itemMapper.toItemWithCommentsDto(details)).thenReturn(responseDto);

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Desc"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("GET /items -> 200 + list ItemWithCommentsDto")
    void getAllItemsFromUser_ok() throws Exception {
        long ownerId = 10L;

        List<ItemDetailsDto> detailsList = List.of(
                new ItemDetailsDto(null, null, null, List.of()),
                new ItemDetailsDto(null, null, null, List.of())
        );

        List<ItemWithCommentsDto> response = List.of(
                new ItemWithCommentsDto(1L, "A", "DA", 0L, true, null, null, List.of()),
                new ItemWithCommentsDto(2L, "B", "DB", 0L, true, null, null, List.of())
        );

        when(itemService.getAllByOwnerWithComments(ownerId)).thenReturn(detailsList);
        when(itemMapper.toItemWithCommentsDtoList(detailsList)).thenReturn(response);

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment -> 201, Location, body")
    void addComment_ok() throws Exception {
        long itemId = 3L;
        long userId = 12L;

        CommentRequestDto request = new CommentRequestDto("Nice!");
        Comment entity = new Comment();

        CommentResponseDto response = new CommentResponseDto(100L, "Nice!", "user", null);

        when(itemService.addComment(userId, itemId, request)).thenReturn(entity);
        when(commentMapper.toDto(entity)).thenReturn(response);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, endsWith("/items/3/comment/100")))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.text").value("Nice!"));
    }

    @Test
    @DisplayName("GET /items/{id} -> 404 when service throws NotFoundException")
    void getItem_notFound() throws Exception {
        long userId = 1L;
        long itemId = 999L;

        when(itemService.getItemWithComments(userId, itemId))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Item not found")));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment -> 400 when ValidationException")
    void addComment_validationError() throws Exception {
        long itemId = 3L;
        long userId = 12L;

        CommentRequestDto request = new CommentRequestDto("Nice!");

        when(itemService.addComment(userId, itemId, request))
                .thenThrow(new ValidationException("User has not finished approved booking"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("User has not finished approved booking")));
    }
}
