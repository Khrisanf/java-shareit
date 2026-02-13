package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItGatewayApp.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    ItemClient itemClient;

    @Test
    void createItem_whenValid_thenProxiesToClientAndReturnsCreated() throws Exception {
        long ownerId = 1L;

        ItemDto request = new ItemDto(
                null,
                "Drill",
                "Power drill",
                0L,
                true,
                null
        );

        ItemDto response = new ItemDto(
                10L,
                "Drill",
                "Power drill",
                0L,
                true,
                null
        );

        Mockito.when(itemClient.createItem(eq(ownerId), eq(request)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Power drill")))
                .andExpect(jsonPath("$.available", is(true)));

        Mockito.verify(itemClient).createItem(eq(ownerId), eq(request));
    }

    @Test
    void createItem_whenMissingHeader_then400_andClientNotCalled() throws Exception {
        ItemDto request = new ItemDto(
                null,
                "Drill",
                "Power drill",
                0L,
                true,
                null
        );

        mockMvc.perform(post("/items")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void createItem_whenInvalidBody_then400_andClientNotCalled() throws Exception {
        long ownerId = 1L;

        // name/description/available обязательны на OnCreate — специально ломаем
        ItemDto invalid = new ItemDto(
                null,
                null,
                null,
                0L,
                null,
                null
        );

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void addComment_whenValid_thenProxiesAndReturnsCreated() throws Exception {
        long userId = 2L;
        long itemId = 5L;

        CommentRequestDto request = new CommentRequestDto("Nice item!");
        CommentResponseDto response = new CommentResponseDto(
                100L,
                "Nice item!",
                "userName",
                LocalDateTime.parse("2026-02-12T10:00:00")
        );

        Mockito.when(itemClient.addComment(eq(itemId), eq(userId), eq(request)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.text", is("Nice item!")));

        Mockito.verify(itemClient).addComment(eq(itemId), eq(userId), eq(request));
    }

    @Test
    void updateItem_whenValid_thenProxiesAndReturnsOk() throws Exception {
        long ownerId = 1L;
        long itemId = 10L;

        ItemDto patch = new ItemDto(
                null,
                "New name",
                "New desc",
                0L,
                true,
                null
        );

        ItemDto response = new ItemDto(
                itemId,
                "New name",
                "New desc",
                0L,
                true,
                null
        );

        Mockito.when(itemClient.updateItem(eq(ownerId), eq(itemId), eq(patch)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("New name")));

        Mockito.verify(itemClient).updateItem(eq(ownerId), eq(itemId), eq(patch));
    }

    @Test
    void deleteItem_whenValid_thenProxiesAndReturnsNoContent() throws Exception {
        long ownerId = 1L;
        long itemId = 10L;

        Mockito.when(itemClient.deleteItem(eq(ownerId), eq(itemId)))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isNoContent());

        Mockito.verify(itemClient).deleteItem(eq(ownerId), eq(itemId));
    }

    @Test
    void searchItems_whenValid_thenProxiesAndReturnsList() throws Exception {
        long userId = 1L;
        String text = "drill";

        List<ItemDto> response = List.of(
                new ItemDto(1L, "Drill", "Power drill", 0L, true, null)
        );

        Mockito.when(itemClient.searchItems(eq(userId), eq(text)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));

        Mockito.verify(itemClient).searchItems(eq(userId), eq(text));
    }

    @Test
    void getItem_whenValid_thenProxiesAndReturnsOk() throws Exception {
        long userId = 1L;
        long itemId = 10L;

        ItemWithCommentsDto response = new ItemWithCommentsDto(
                itemId,
                "Drill",
                "Power drill",
                0L,
                true,
                null,
                null,
                List.of()
        );

        Mockito.when(itemClient.getItem(eq(userId), eq(itemId)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.comments", hasSize(0)));

        Mockito.verify(itemClient).getItem(eq(userId), eq(itemId));
    }

    @Test
    void getAllItemsFromUser_whenValid_thenProxiesAndReturnsList() throws Exception {
        long ownerId = 1L;

        List<ItemWithCommentsDto> response = List.of(
                new ItemWithCommentsDto(10L, "Drill", "Power drill", 0L, true, null, null, List.of())
        );

        Mockito.when(itemClient.getAllItemsFromUser(eq(ownerId)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)));

        Mockito.verify(itemClient).getAllItemsFromUser(eq(ownerId));
    }
}
