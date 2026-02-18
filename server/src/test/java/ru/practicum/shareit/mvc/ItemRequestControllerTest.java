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
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
@ContextConfiguration(classes = ShareItServerApp.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestService itemRequestService;
    @MockBean
    ItemRequestMapper itemRequestMapper;
    @MockBean
    ItemMapper itemMapper;
    @MockBean
    ItemRequestDtoAssembler itemRequestDtoAssembler;

    @Test
    @DisplayName("POST /requests -> 201 + Location + body")
    void create_ok() throws Exception {
        long userId = 10L;

        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");
        ItemRequest entity = new ItemRequest();

        ItemRequest saved = new ItemRequest();
        saved.setId(1L);
        saved.setCreated(LocalDateTime.of(2030, 1, 1, 10, 0));
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Нужна дрель", saved.getCreated(), List.of());

        when(itemRequestMapper.toEntity(createDto)).thenReturn(entity);
        when(itemRequestService.create(entity, userId)).thenReturn(saved);
        when(itemRequestMapper.toDto(saved, List.of())).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/requests/1"))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));

        verify(itemRequestService).create(entity, userId);
        verify(itemRequestMapper).toEntity(createDto);
        verify(itemRequestMapper).toDto(saved, List.of());
    }

    @Test
    @DisplayName("GET /requests/{id} -> 200 + body (items маппятся в ItemShortDto)")
    void getOneById_ok() throws Exception {
        long userId = 10L;
        long requestId = 55L;

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setCreated(LocalDateTime.of(2030, 1, 2, 12, 0));

        Item item = mock(Item.class);
        User owner = mock(User.class);
        when(item.getId()).thenReturn(777L);
        when(item.getName()).thenReturn("Дрель");
        when(item.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(10L);

        when(itemRequestService.findById(userId, requestId)).thenReturn(request);
        when(itemRequestService.findItemsByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestDto dto = new ItemRequestDto(
                requestId,
                "Нужна дрель",
                request.getCreated(),
                List.of(new ItemShortDto(777L, "Дрель", 10L))
        );

        when(itemRequestMapper.toDto(eq(request), anyList())).thenReturn(dto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(777))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"))
                .andExpect(jsonPath("$.items[0].ownerId").value(10));

        verify(itemRequestService).findById(userId, requestId);
        verify(itemRequestService).findItemsByRequestId(requestId);
        verify(itemRequestMapper).toDto(eq(request), anyList());
    }

    @Test
    @DisplayName("GET /requests -> 200 + list (через assembler)")
    void getAll_ok() throws Exception {
        long userId = 10L;

        ItemRequest r1 = new ItemRequest();
        r1.setId(1L);
        ItemRequest r2 = new ItemRequest();
        r2.setId(2L);

        ItemRequestDto d1 = new ItemRequestDto(1L, "A", null, List.of());
        ItemRequestDto d2 = new ItemRequestDto(2L, "B", null, List.of());

        when(itemRequestService.findAllByRequestor(userId)).thenReturn(List.of(r1, r2));
        when(itemRequestDtoAssembler.mapRequestsToDtos(List.of(r1, r2))).thenReturn(List.of(d1, d2));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemRequestService).findAllByRequestor(userId);
        verify(itemRequestDtoAssembler).mapRequestsToDtos(List.of(r1, r2));
    }

    @Test
    @DisplayName("GET /requests/all?from=0&size=10 -> 200 + list (через assembler)")
    void getAllOther_ok() throws Exception {
        long userId = 10L;

        ItemRequest r1 = new ItemRequest();
        r1.setId(100L);
        ItemRequestDto d1 = new ItemRequestDto(100L, "X", null, List.of());

        when(itemRequestService.findAllOtherRequests(userId, 0, 10)).thenReturn(List.of(r1));
        when(itemRequestDtoAssembler.mapRequestsToDtos(List.of(r1))).thenReturn(List.of(d1));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100));

        verify(itemRequestService).findAllOtherRequests(userId, 0, 10);
        verify(itemRequestDtoAssembler).mapRequestsToDtos(List.of(r1));
    }

    @Test
    @DisplayName("GET /requests/{id} -> 404 если service кидает NotFoundException")
    void getOneById_notFound() throws Exception {
        long userId = 10L;
        long requestId = 999L;

        when(itemRequestService.findById(userId, requestId))
                .thenThrow(new NotFoundException("Request not found: " + requestId));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Request not found")));
    }
}
