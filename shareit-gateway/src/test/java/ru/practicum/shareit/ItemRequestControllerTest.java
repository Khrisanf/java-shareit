package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@ContextConfiguration(classes = ShareItGatewayApp.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestClient client;

    @Test
    void create_validBody_shouldReturn201_andDelegateToClient() throws Exception {
        long userId = 1L;

        ItemRequestCreateDto req = new ItemRequestCreateDto("Need a drill");
        ItemRequestDto resp = new ItemRequestDto(
                10L,
                "Need a drill",
                LocalDateTime.now().withNano(0),
                List.of()
        );

        when(client.create(eq(userId), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(201).body(resp));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(client, times(1)).create(eq(userId), any(ItemRequestCreateDto.class));
    }

    @Test
    void create_blankDescription_shouldReturn400_andNotCallClient() throws Exception {
        long userId = 1L;

        // @NotBlank на description
        ItemRequestCreateDto req = new ItemRequestCreateDto("   ");

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void getOneById_shouldDelegateToClient() throws Exception {
        long userId = 2L;
        long requestId = 12L;

        ItemRequestDto resp = new ItemRequestDto(
                requestId,
                "Need a ladder",
                LocalDateTime.now().withNano(0),
                List.of()
        );

        when(client.getOneById(userId, requestId)).thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));

        verify(client).getOneById(userId, requestId);
    }

    @Test
    void getAll_shouldDelegateToClient() throws Exception {
        long userId = 3L;

        when(client.getAll(userId)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(client).getAll(userId);
    }

    @Test
    void getAllOther_shouldDelegateToClient() throws Exception {
        long userId = 4L;

        when(client.getAllOther(userId, 0, 10)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(client).getAllOther(userId, 0, 10);
    }
}
